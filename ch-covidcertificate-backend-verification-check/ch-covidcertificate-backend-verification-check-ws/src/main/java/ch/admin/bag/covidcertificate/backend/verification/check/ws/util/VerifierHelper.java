package ch.admin.bag.covidcertificate.backend.verification.check.ws.util;

import ch.admin.bag.covidcertificate.backend.verification.check.ws.model.IntermediateRuleSet;
import ch.admin.bag.covidcertificate.backend.verification.check.ws.model.TrustListConfig;
import ch.admin.bag.covidcertificate.sdk.core.models.trustlist.Jwks;
import ch.admin.bag.covidcertificate.sdk.core.models.trustlist.RevokedCertificates;
import ch.admin.bag.covidcertificate.sdk.core.models.trustlist.Rule;
import ch.admin.bag.covidcertificate.sdk.core.models.trustlist.RuleSet;
import ch.admin.bag.covidcertificate.sdk.core.models.trustlist.TrustList;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.kotlin.KotlinModule;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
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
    private static final ObjectMapper objectMapper =
            new ObjectMapper().registerModule(new KotlinModule());
    private static final HttpClient httpClient = HttpClient.newHttpClient();

    private final TrustListConfig trustListConfig;
    private final String verifierBaseUrl;
    private final String dscEndpoint;
    private final String revocationEndpoint;
    private final String rulesEndpoint;
    private final String valueSetsEndpoint;

    private final Map<String, String> etagMap = new HashMap<>();

    public VerifierHelper(
            TrustListConfig trustListConfig,
            String verifierBaseUrl,
            String dscEndpoint,
            String revocationEndpoint,
            String rulesEndpoint,
            String valueSetsEndpoint) {
        this.trustListConfig = trustListConfig;
        this.verifierBaseUrl = verifierBaseUrl;
        this.dscEndpoint = dscEndpoint;
        this.revocationEndpoint = revocationEndpoint;
        this.rulesEndpoint = rulesEndpoint;
        this.valueSetsEndpoint = valueSetsEndpoint;
    }

    public void updateTrustListConfig() {
        final var jwks = getDSCs();
        final var revokedCerts = getRevokedCerts();
        final var nationalRules = getNationalRules();
        var trustList = new TrustList(jwks, revokedCerts, nationalRules);
        trustListConfig.setTrustList(trustList);
    }

    /**
     * Sends a request to the VerifierService to obtain an up-to-date list of DSCs
     *
     * @return a JWKs object as required by the SDK-core mapped from a list of ClientCerts
     */
    private Jwks getDSCs() {
        logger.info("Updating list of DSCs");
        final var params = new HashMap<String, String>();
        params.put("certFormat", "ANDROID");
        try {
            String response = getResponse(dscEndpoint, params);
            if (response.isBlank()) {
                logger.info("ETag hasn't changed - No need to update");
                return trustListConfig.getTrustList().getSignatures();
            } else {
                return objectMapper.readValue(response, Jwks.class);
            }
        } catch (URISyntaxException | IOException | InterruptedException e) {
            logger.error(
                    "An error occurred while downloading the list of DSCs: {}", e.getMessage());
            return new Jwks(new ArrayList<>());
        }
    }

    /**
     * Sends a request to the VerifierService to obtain an up-to-date list of revoked certificate
     * KIDs
     *
     * @return RevokedCertificates object as required by the SDK-core
     */
    private RevokedCertificates getRevokedCerts() {
        logger.info("Updating list of revoked certificates");
        try {
            final String response = getResponse(revocationEndpoint, new HashMap<>());
            if (response.isBlank()) {
                logger.info("ETag hasn't changed - No need to update");
                return trustListConfig.getTrustList().getRevokedCertificates();
            } else {
                return objectMapper.readValue(response, RevokedCertificates.class);
            }
        } catch (URISyntaxException | IOException | InterruptedException e) {
            logger.error(
                    "An error occurred while downloading the list of revoked certificates: {}",
                    e.getMessage());
            return new RevokedCertificates(new ArrayList<>(), 0);
        }
    }

    /**
     * Sends a request to the VerifierService to obtain an up-to-date list of national rules
     *
     * @return RuleSet object as required by the SDK-core
     */
    private RuleSet getNationalRules() {
        logger.info("Updating list of revoked certificates");
        try {
            final String response = getResponse(rulesEndpoint, new HashMap<>());
            if (response.isBlank()) {
                logger.info("ETag hasn't changed - No need to update");
                return trustListConfig.getTrustList().getRuleSet();
            } else {
                final var intermediateRules =
                        objectMapper.readValue(response, IntermediateRuleSet.class);
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
                        rules,
                        intermediateRules.getValueSets(),
                        intermediateRules.getValidDuration());
            }
        } catch (URISyntaxException | IOException | InterruptedException e) {
            logger.error(
                    "An error occurred while downloading the list of verification rules: {}",
                    e.getMessage());
            return new RuleSet(new ArrayList<>(), null, 0);
        }
    }

    private String getResponse(String endpoint, Map<String, String> params)
            throws URISyntaxException, IOException, InterruptedException {
        final var strings = verifierBaseUrl.split("://");
        final var builder =
                UriComponentsBuilder.newInstance()
                        .scheme(strings[0])
                        .host(strings[1])
                        .path(endpoint);
        for (var entry : params.entrySet()) {
            builder.queryParam(entry.getKey(), entry.getValue());
        }
        final var uri = builder.build().toUriString();
        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(new URI(uri))
                        .GET()
                        .header("ETag", etagMap.getOrDefault(endpoint, ""))
                        .build();
        final var response = httpClient.send(request, BodyHandlers.ofString());
        if (response.statusCode() == 304) {
            return "";
        } else {
            final var eTag = response.headers().firstValue("ETag");
            eTag.ifPresent(tag -> etagMap.put(endpoint, tag));
            return response.body();
        }
    }
}
