package ch.admin.bag.covidcertificate.backend.verification.check.ws.verification;

import ch.admin.bag.covidcertificate.backend.verification.check.model.HCertPayload;
import ch.admin.bag.covidcertificate.backend.verification.check.model.cert.CertFormat;
import ch.admin.bag.covidcertificate.backend.verification.check.ws.model.IntermediateRuleSet;
import ch.admin.bag.covidcertificate.backend.verification.check.ws.model.TrustListConfig;
import ch.admin.bag.covidcertificate.sdk.core.decoder.CertificateDecoder;
import ch.admin.bag.covidcertificate.sdk.core.models.healthcert.CertificateHolder;
import ch.admin.bag.covidcertificate.sdk.core.models.state.DecodeState;
import ch.admin.bag.covidcertificate.sdk.core.models.state.VerificationState;
import ch.admin.bag.covidcertificate.sdk.core.models.trustlist.Jwk;
import ch.admin.bag.covidcertificate.sdk.core.models.trustlist.Jwks;
import ch.admin.bag.covidcertificate.sdk.core.models.trustlist.RevokedCertificates;
import ch.admin.bag.covidcertificate.sdk.core.models.trustlist.Rule;
import ch.admin.bag.covidcertificate.sdk.core.models.trustlist.RuleSet;
import ch.admin.bag.covidcertificate.sdk.core.models.trustlist.TrustList;
import ch.admin.bag.covidcertificate.sdk.core.verifier.CertificateVerifier;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

public class VerificationService {

    private static final Logger logger = LoggerFactory.getLogger(VerificationService.class);

    private static final String UP_TO_DATE_HEADER = "up-to-date";
    private static final String NEXT_SINCE_HEADER = "X-Next-Since";
    private static final String SINCE_PARAM = "since";
    private static final String CERT_FORMAT_PARAM = "certFormat";
    private static final String UP_TO_PARAM = "upTo";

    private final String verifierBaseUrl;
    private final String dscEndpoint;
    private final String revocationEndpoint;
    private final String rulesEndpoint;
    private final String apiKey;
    private final RestTemplate rt = new RestTemplate();
    private final TrustListConfig trustListConfig = new TrustListConfig();
    private CertificateVerifier certificateVerifier = new CertificateVerifier();

    public VerificationService(
            String verifierBaseUrl,
            String dscEndpoint,
            String revocationEndpoint,
            String rulesEndpoint,
            String apiKey) {
        this.verifierBaseUrl = verifierBaseUrl;
        this.dscEndpoint = dscEndpoint;
        this.revocationEndpoint = revocationEndpoint;
        this.rulesEndpoint = rulesEndpoint;
        this.apiKey = apiKey;
        updateTrustListConfig();
    }

    public void updateTrustListConfig() {
        try {
            logger.info("updating trust list config");
            Jwks jwks = getDSCs();
            RevokedCertificates revokedCerts = getRevokedCerts();
            RuleSet nationalRules = getNationalRules();
            this.trustListConfig.setTrustList(new TrustList(jwks, revokedCerts, nationalRules));
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
        int MAX_REQUESTS = 1000;
        do {
            ResponseEntity<Jwks> response =
                    rt.exchange(getRequestEntity(dscEndpoint, params), Jwks.class);
            jwkList.addAll(response.getBody().getCerts());
            params.put(SINCE_PARAM, response.getHeaders().get(NEXT_SINCE_HEADER).get(0));
            done = response.getHeaders().get(UP_TO_DATE_HEADER) != null;
            it++;
        } while (!done && it < MAX_REQUESTS);
        return new Jwks(jwkList);
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
    private RevokedCertificates getRevokedCerts() throws URISyntaxException {
        logger.info("Updating list of revoked certificates");
        return rt.exchange(
                        getRequestEntity(revocationEndpoint, new HashMap<>()),
                        RevokedCertificates.class)
                .getBody();
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
                                                rule.getId(),
                                                rule.getBusinessDescription(),
                                                rule.getDescription(),
                                                rule.getInputParameter(),
                                                rule.getLogic()))
                        .collect(Collectors.toList());
        return new RuleSet(
                rules, intermediateRuleSet.getValueSets(), intermediateRuleSet.getValidDuration());
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

    public DecodeState decodeHCert(HCertPayload hCertPayload) {
        return CertificateDecoder.decode(hCertPayload.getHcert());
    }

    public VerificationState verifyDcc(CertificateHolder certificateHolder) {
        return VerifyWrapper.verify(
                certificateVerifier, certificateHolder, trustListConfig.getTrustList());
    }
}
