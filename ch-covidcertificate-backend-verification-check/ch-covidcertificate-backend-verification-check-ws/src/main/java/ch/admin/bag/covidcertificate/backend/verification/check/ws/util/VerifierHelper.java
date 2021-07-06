package ch.admin.bag.covidcertificate.backend.verification.check.ws.util;

import ch.admin.bag.covidcertificate.backend.verification.check.ws.model.IntermediateRuleSet;
import ch.admin.bag.covidcertificate.backend.verification.check.ws.model.TrustListConfig;
import ch.admin.bag.covidcertificate.sdk.core.models.trustlist.Jwk;
import ch.admin.bag.covidcertificate.sdk.core.models.trustlist.Jwks;
import ch.admin.bag.covidcertificate.sdk.core.models.trustlist.RevokedCertificates;
import ch.admin.bag.covidcertificate.sdk.core.models.trustlist.Rule;
import ch.admin.bag.covidcertificate.sdk.core.models.trustlist.RuleSet;
import ch.admin.bag.covidcertificate.sdk.core.models.trustlist.TrustList;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UriComponentsBuilder;

public class VerifierHelper {

    private static final Logger logger = LoggerFactory.getLogger(VerifierHelper.class);
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private final TrustListConfig trustListConfig;
    private final String verifierBaseUrl;
    private final String dscEndpoint;
    private final String revocationEndpoint;
    private final String rulesEndpoint;
    private final ObjectMapper objectMapper;

    public VerifierHelper(
        TrustListConfig trustListConfig,
        String verifierBaseUrl,
        String dscEndpoint,
        String revocationEndpoint,
        String rulesEndpoint, ObjectMapper objectMapper) {
        this.trustListConfig = trustListConfig;
        this.verifierBaseUrl = verifierBaseUrl;
        this.dscEndpoint = dscEndpoint;
        this.revocationEndpoint = revocationEndpoint;
        this.rulesEndpoint = rulesEndpoint;
        this.objectMapper = objectMapper;
    }

    // TODO: How to handle response failures?
    public void updateTrustListConfig() throws InterruptedException {
        Jwks jwks = null;
        RevokedCertificates revokedCerts = null;
        RuleSet nationalRules = null;
        try {
            jwks = getDSCs();
            revokedCerts = getRevokedCerts();
            nationalRules = getNationalRules();
        } catch (URISyntaxException | IOException e) {
            logger.error("TrustList update failed: {}", e.getMessage());
        } catch (InterruptedException e) {
            logger.error("TrustList update failed: {}", e.getMessage());
            throw e;
        }
        if (jwks != null && revokedCerts != null && nationalRules != null) {
            var trustList = new TrustList(jwks, revokedCerts, nationalRules);
            trustListConfig.setTrustList(trustList);
        }
    }

    /**
     * Sends a request to the VerifierService to obtain an up-to-date list of DSCs
     *
     * @return a JWKs object as required by the SDK-core mapped from a list of ClientCerts
     */
    private Jwks getDSCs() throws URISyntaxException, IOException, InterruptedException {
        logger.info("Updating list of DSCs");
        final var params = new HashMap<String, String>();
        params.put("certFormat", "ANDROID");
        List<Jwk> jwkList = new ArrayList<>();
        HttpResponse<String> response;
        do {
            response = getResponse(dscEndpoint, params);
            jwkList.addAll(objectMapper.readValue(response.body(), Jwks.class).getCerts());
            final var nextSince = response.headers().firstValue("X-Next-Since");
            nextSince.ifPresent(next -> params.put("since", next));
        } while (response.headers().firstValue("up-to-date").isEmpty());
        return new Jwks(jwkList);
    }

    /**
     * Sends a request to the VerifierService to obtain an up-to-date list of revoked certificate
     * KIDs
     *
     * @return RevokedCertificates object as required by the SDK-core
     */
    private RevokedCertificates getRevokedCerts()
            throws URISyntaxException, IOException, InterruptedException {
        logger.info("Updating list of revoked certificates");
        final String response = getResponse(revocationEndpoint, new HashMap<>()).body();
        return objectMapper.readValue(response, RevokedCertificates.class);
    }

    /**
     * Sends a request to the VerifierService to obtain an up-to-date list of national rules
     *
     * @return RuleSet object as required by the SDK-core
     */
    private RuleSet getNationalRules()
            throws URISyntaxException, IOException, InterruptedException {
        logger.info("Updating list of revoked certificates");
        final String response = getResponse(rulesEndpoint, new HashMap<>()).body();
        final var intermediateRules = objectMapper.readValue(response, IntermediateRuleSet.class);
        List<Rule> rules = new ArrayList<>();
        for (var rule : intermediateRules.getRules()) {
            rules.add(
                    new Rule(
                            rule.getId(),
                            rule.getBusinessDescription(),
                            rule.getDescription(),
                            rule.getInputParameter(),
                            rule.getLogic()));
        }
        return new RuleSet(
                rules, intermediateRules.getValueSets(), intermediateRules.getValidDuration());
    }

    private HttpResponse<String> getResponse(String endpoint, Map<String, String> params)
            throws URISyntaxException, IOException, InterruptedException {
        final var urlStrings = verifierBaseUrl.split("://");
        final var builder =
                UriComponentsBuilder.newInstance()
                        .scheme(urlStrings[0])
                        .host(urlStrings[1])
                        .path(endpoint);
        for (var entry : params.entrySet()) {
            builder.queryParam(entry.getKey(), entry.getValue());
        }
        final var uri = builder.build().toUriString();
        HttpRequest request = HttpRequest.newBuilder().uri(new URI(uri)).GET().build();
        return httpClient.send(request, BodyHandlers.ofString());
    }
}
