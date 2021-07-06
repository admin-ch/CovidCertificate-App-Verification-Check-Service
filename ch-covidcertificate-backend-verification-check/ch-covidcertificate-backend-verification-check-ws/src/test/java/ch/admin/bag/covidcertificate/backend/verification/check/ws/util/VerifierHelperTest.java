package ch.admin.bag.covidcertificate.backend.verification.check.ws.util;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import ch.admin.bag.covidcertificate.backend.verification.check.ws.model.IntermediateRuleSet;
import ch.admin.bag.covidcertificate.sdk.core.models.trustlist.Jwks;
import ch.admin.bag.covidcertificate.sdk.core.models.trustlist.Rule;
import ch.admin.bag.covidcertificate.sdk.core.models.trustlist.RuleSet;
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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.UriComponentsBuilder;

class VerifierHelperTest {

    private static final Logger logger = LoggerFactory.getLogger(VerifierHelperTest.class);
    private static final ObjectMapper objectMapper =
            new ObjectMapper().registerModule(new KotlinModule());
    private static final HttpClient httpClient = HttpClient.newHttpClient();

    private static final String verifierBaseUrl = "http://localhost:8080";
    private static final String dscEndpoint = "/trust/v1/keys/updates";
    private static final String rulesEndpoint = "/trust/v1/verificationRules";
    private static Map<String, String> etagMap = new HashMap<>();

    @Test
    @Disabled("Need to mock Verifier Service endpoint")
    void getDSCsTest() throws IOException, URISyntaxException, InterruptedException {
        logger.info("Updating list of DSCs");
        final var params = new HashMap<String, String>();
        params.put("certFormat", "ANDROID");
        String response = getResponse(dscEndpoint, params);
        if (response.isEmpty()) {
            logger.info("ETag hasn't changed - No need to update");
        } else {
            final var jwks = objectMapper.readValue(response, Jwks.class);
            assertNotNull(jwks);
        }
    }

    @Test
    @Disabled("Need to mock Verifier Service endpoint")
    void getRulesTest() throws URISyntaxException, IOException, InterruptedException {
        final String response = getResponse(rulesEndpoint, new HashMap<>());
        if (response.isBlank()) {
            logger.info("ETag hasn't changed - No need to update");
        } else {
            final var rules = objectMapper.readValue(response, IntermediateRuleSet.class);
            assertNotNull(rules);
            List<Rule> rulesList = new ArrayList<>();
            for (var rule : rules.getRules()) {
                rulesList.add(
                        new Rule(
                                rule.getId(),
                                rule.getBusinessDescription(),
                                rule.getDescription(),
                                rule.getInputParameter(),
                                rule.getLogic()));
            }
            final var ruleSet =
                    new RuleSet(rulesList, rules.getValueSets(), rules.getValidDuration());
            assertNotNull(ruleSet);
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
