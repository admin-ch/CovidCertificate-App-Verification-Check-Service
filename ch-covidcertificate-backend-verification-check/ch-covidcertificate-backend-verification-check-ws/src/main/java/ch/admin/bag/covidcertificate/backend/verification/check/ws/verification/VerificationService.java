package ch.admin.bag.covidcertificate.backend.verification.check.ws.verification;

import ch.admin.bag.covidcertificate.backend.verification.check.model.HCertPayload;
import ch.admin.bag.covidcertificate.backend.verification.check.model.cert.CertFormat;
import ch.admin.bag.covidcertificate.backend.verification.check.ws.model.DecodingException;
// import ch.admin.bag.covidcertificate.backend.verification.check.ws.model.IntermediateRuleSet;
import ch.admin.bag.covidcertificate.backend.verification.check.ws.model.IntermediateRuleSet;
import ch.admin.bag.covidcertificate.backend.verification.check.ws.model.IntermediateRuleSet.ModeRules;
import ch.admin.bag.covidcertificate.backend.verification.check.ws.model.RevokedCertificatesRepository;
import ch.admin.bag.covidcertificate.backend.verification.check.ws.model.RevokedCertificatesRepository.RevokedCertificates;
import ch.admin.bag.covidcertificate.backend.verification.check.ws.model.TrustListConfig;
import ch.admin.bag.covidcertificate.sdk.core.decoder.CertificateDecoder;
import ch.admin.bag.covidcertificate.sdk.core.models.healthcert.CertificateHolder;
import ch.admin.bag.covidcertificate.sdk.core.models.state.CheckNationalRulesState;
import ch.admin.bag.covidcertificate.sdk.core.models.state.CheckRevocationState;
import ch.admin.bag.covidcertificate.sdk.core.models.state.CheckSignatureState;
import ch.admin.bag.covidcertificate.sdk.core.models.state.DecodeState;
import ch.admin.bag.covidcertificate.sdk.core.models.state.VerificationState;
import ch.admin.bag.covidcertificate.sdk.core.models.state.VerificationState.INVALID;
import ch.admin.bag.covidcertificate.sdk.core.models.state.VerificationState.SUCCESS;
import ch.admin.bag.covidcertificate.sdk.core.models.trustlist.DisplayRule;
import ch.admin.bag.covidcertificate.sdk.core.models.trustlist.Jwk;
import ch.admin.bag.covidcertificate.sdk.core.models.trustlist.Jwks;
import ch.admin.bag.covidcertificate.sdk.core.models.trustlist.Rule;
import ch.admin.bag.covidcertificate.sdk.core.models.trustlist.RuleSet;
import ch.admin.bag.covidcertificate.sdk.core.models.trustlist.TrustList;
import ch.admin.bag.covidcertificate.sdk.core.verifier.CertificateVerifier;
import ch.admin.bag.covidcertificate.sdk.core.verifier.nationalrules.NationalRulesError;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class VerificationService {

    private static final Logger logger = LoggerFactory.getLogger(VerificationService.class);

    private static final String UP_TO_DATE_HEADER = "up-to-date";
    private static final String NEXT_SINCE_HEADER = "X-Next-Since";
    private static final String SINCE_PARAM = "since";
    private static final String CERT_FORMAT_PARAM = "certFormat";
    private static final String UP_TO_PARAM = "upTo";

    private static final int MAX_REQUESTS = 1000;

    private final TrustListConfig trustListConfig = new TrustListConfig();
    private final CertificateVerifier certificateVerifier = new CertificateVerifier();

    private final RestTemplate rt;

    @Value("${verifier.baseurl}")
    private String verifierBaseUrl;

    @Value("${verifier.dsc.endpoint:/trust/v2/keys/updates}")
    private String dscEndpoint;

    @Value("${verifier.revocation.endpoint:/trust/v2/revocationList}")
    private String revocationEndpoint;

    @Value("${verifier.rules.endpoint:/trust/v2/verificationRules}")
    private String rulesEndpoint;

    @Value("${verifier.api-key:}")
    private String apiKey;

    public VerificationService(RestTemplate rt) {
        this.rt = rt;
    }

    @PostConstruct
    public void init() {
        updateTrustListConfig();
    }

    public void updateTrustListConfig() {
        try {
            logger.info("updating trust list config");
            Jwks jwks = getDSCs();
            RevokedCertificatesRepository revokedCerts = getRevokedCerts();
            RuleSet nationalRules = getNationalRules();
            this.trustListConfig.setTrustList(new TrustList(jwks, revokedCerts, nationalRules));
            this.trustListConfig.setRevokedCertificatesRepository(revokedCerts);
            this.trustListConfig.setLastSync(Instant.now());
            logger.info("done updating trust list config");
        } catch (Exception e) {
            logger.error("failed to update trust list config", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Sends a request to the VerifierService to obtain an up-to-date list of DSCs
     *
     * @return a JWKs object as required by the SDK-core mapped from a list of ClientCerts
     */
    private Jwks getDSCs() throws URISyntaxException {
        logger.info("Updating list of DSCs");
        Map<String, String> params = getKeyUpdatesParams();
        List<Jwk> jwkList = new ArrayList<>();
        boolean done = false;
        int it = 0;
        do {
            ResponseEntity<Jwks> response =
                    rt.exchange(getRequestEntity(dscEndpoint, params), Jwks.class);
            jwkList.addAll(response.getBody().getCerts());

            HttpHeaders headers = response.getHeaders();
            List<String> nextSince = headers.get(NEXT_SINCE_HEADER);
            if (nextSince != null && !nextSince.isEmpty()) {
                params.put(SINCE_PARAM, nextSince.get(0));
                done = upToDateHeaderIsTrue(headers);
            } else { // fallback. exit loop if no next since header sent
                done = true;
            }

            it++;
        } while (!done && it < MAX_REQUESTS);
        logger.info("downloaded {} DSCs", jwkList.size());
        return new Jwks(jwkList);
    }

    private boolean upToDateHeaderIsTrue(HttpHeaders headers) {
        List<String> upToDateHeaders = headers.get(UP_TO_DATE_HEADER);
        if (upToDateHeaders != null) {
            for (String upToDateHeader : upToDateHeaders) {
                if (Boolean.TRUE.toString().equals(upToDateHeader)) {
                    return true;
                }
            }
        }
        return false;
    }

    private Map<String, String> getKeyUpdatesParams() {
        final var params = new HashMap<String, String>();
        params.put(CERT_FORMAT_PARAM, CertFormat.ANDROID.name());
        params.put(UP_TO_PARAM, String.valueOf(Long.MAX_VALUE));
        return params;
    }

    /**
     * Sends a request to the VerifierService to obtain an up-to-date list of revoked certificate
     * KIDs
     *
     * @return RevokedCertificates object as required by the SDK-core
     */
    private RevokedCertificatesRepository getRevokedCerts() throws URISyntaxException {
        logger.info("Updating list of revoked certificates");
        Map<String, String> params = new HashMap<>();
        ResponseEntity<RevokedCertificates> response =
                rt.exchange(
                        getRequestEntity(revocationEndpoint, params), RevokedCertificates.class);
        RevokedCertificates revokedCerts = response.getBody();
        RevokedCertificatesRepository repo = new RevokedCertificatesRepository(revokedCerts);
        boolean done = upToDateHeaderIsTrue(response.getHeaders());
        int it = 1;
        while (!done && it < MAX_REQUESTS) {
            HttpHeaders headers = response.getHeaders();
            List<String> nextSince = headers.get(NEXT_SINCE_HEADER);
            if (nextSince != null && !nextSince.isEmpty()) {
                params.put(SINCE_PARAM, nextSince.get(0));
                response =
                        rt.exchange(
                                getRequestEntity(revocationEndpoint, params),
                                RevokedCertificates.class);
                if(response.getBody() != null) {
                    repo.addCertificates(response.getBody().getRevokedCerts());
                }
                done = upToDateHeaderIsTrue(headers);
            } else { // fallback. exit loop if no next since header sent
                done = true;
            }

            it++;
        }
        logger.info("downloaded {} revoked certificates", revokedCerts.getRevokedCerts().size());
        return repo;
    }

    /**
     * Sends a request to the VerifierService to obtain an up-to-date list of national rules
     *
     * @return RuleSet object as required by the SDK-core
     */
    private RuleSet getNationalRules() throws URISyntaxException {
        logger.info("Updating national rules");
        IntermediateRuleSet intermediateRuleSet =
                rt.exchange(
                                getRequestEntity(rulesEndpoint, new HashMap<>()),
                                IntermediateRuleSet.class)
                        .getBody();
        List<Rule> rules =
                intermediateRuleSet.getRules().stream()
                        .map(
                                rule ->
                                        new Rule(
                                                rule.getAffectedFields(),
                                                rule.getCertificateType(),
                                                rule.getCountry(),
                                                rule.getDescription(),
                                                rule.getEngine(),
                                                rule.getEngineVersion(),
                                                rule.getIdentifier(),
                                                rule.getLogic(),
                                                rule.getSchemaVersion(),
                                                rule.getType(),
                                                rule.getValidFrom(),
                                                rule.getValidTo(),
                                                rule.getVersion()))
                        .collect(Collectors.toList());
        List<DisplayRule> displayRules =
                intermediateRuleSet.getDisplayRules().stream()
                        .map(rule -> new DisplayRule(rule.getId(), rule.getLogic()))
                        .collect(Collectors.toList());
        ModeRules intermediateModeRules = intermediateRuleSet.getModeRules();
        ch.admin.bag.covidcertificate.sdk.core.models.trustlist.ModeRules sdkModeRules = new ch.admin.bag.covidcertificate.sdk.core.models.trustlist.ModeRules(intermediateModeRules.getActiveModes(), intermediateModeRules.getLogic());
        logger.info("downloaded {} rules", rules.size());

        return new RuleSet(
                displayRules,
                rules,
                sdkModeRules,
                intermediateRuleSet.getValueSets(),
                intermediateRuleSet.getValidDuration());
    }

    private RequestEntity<Void> getRequestEntity(String endpoint, Map<String, String> params)
            throws URISyntaxException {
        URI uri = new URI(verifierBaseUrl);
        final var builder =
                UriComponentsBuilder.newInstance()
                        .scheme(uri.getScheme())
                        .host(uri.getHost())
                        .port(uri.getPort())
                        .path(endpoint);
        for (var entry : params.entrySet()) {
            builder.queryParam(entry.getKey(), entry.getValue());
        }
        return RequestEntity.get(new URI(builder.build().toUriString()))
                .headers(createHeaders())
                .build();
    }

    private HttpHeaders createHeaders() {
        var headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey);
        return headers;
    }

    public CertificateHolder decodeHCert(HCertPayload hCertPayload) {
        final var decodeState = CertificateDecoder.decode(hCertPayload.getHcert());
        if (decodeState instanceof DecodeState.SUCCESS) {
            return ((DecodeState.SUCCESS) decodeState).getCertificateHolder();
        } else {
            throw new DecodingException(
                    "Couldn't decode hcert: "
                            + ((DecodeState.ERROR) decodeState).getError().getMessage());
        }
    }

    public VerificationState verifyDccAllModes(CertificateHolder certificateHolder) {
        TrustList trustList = trustListConfig.getTrustList();
        VerificationState state =
                VerifyWrapper.verifyWallet(certificateVerifier, certificateHolder, trustList);
        return !trustListConfig.isOutdated() || state instanceof VerificationState.ERROR
                ? state
                : getOutdatedTrustListState(state);
    }

    public VerificationState verifyDccSingleMode(CertificateHolder certificateHolder, String mode){
        TrustList trustList = trustListConfig.getTrustList();
        VerificationState state =
                VerifyWrapper.verifyVerifier(certificateVerifier, certificateHolder, trustList, mode);
        return !trustListConfig.isOutdated() || state instanceof VerificationState.ERROR
                ? state
                : getOutdatedTrustListState(state);
    }

    /**
     * returns an invalid response for outdated trust list. signature validation is also allowed
     * with outdated trust list data (significant for pdf export)
     *
     * @param originalState
     * @return
     */
    private VerificationState getOutdatedTrustListState(VerificationState originalState) {
        logger.error(
                "trust list is outdated. last successful sync at {}",
                Date.from(trustListConfig.getLastSync()));
        boolean signatureValid =
                originalState instanceof SUCCESS
                        || (originalState instanceof INVALID
                                && ((INVALID) originalState).getSignatureState()
                                        instanceof CheckSignatureState.SUCCESS);
        CheckSignatureState signatureState;
        if (!signatureValid) {
            if (originalState instanceof VerificationState.INVALID) {
                signatureState = ((VerificationState.INVALID) originalState).getSignatureState();
            } else {
                signatureState = new CheckSignatureState.INVALID("TRUST_LIST_OUTDATED");
            }
        } else {
            signatureState = CheckSignatureState.SUCCESS.INSTANCE;
        }

        return new VerificationState.INVALID(
                signatureState,
                new CheckRevocationState.INVALID("TRUST_LIST_OUTDATED"),
                new CheckNationalRulesState.INVALID(
                        NationalRulesError.UNKNOWN_RULE_FAILED, "TRUST_LIST_OUTDATED"),
                null);
    }
}
