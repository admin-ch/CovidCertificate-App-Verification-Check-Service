package ch.admin.bag.covidcertificate.backend.verification.check.ws.util;

import static org.junit.jupiter.api.Assertions.assertNotNull;

//import ch.admin.bag.covidcertificate.backend.verification.check.ws.model.IntermediateRuleSet;
import ch.admin.bag.covidcertificate.backend.verification.check.ws.model.IntermediateRuleSet;
import ch.admin.bag.covidcertificate.backend.verification.check.ws.model.IntermediateRuleSet.ModeRules;
import ch.admin.bag.covidcertificate.sdk.core.models.trustlist.DisplayRule;
import ch.admin.bag.covidcertificate.sdk.core.models.trustlist.Jwks;
import ch.admin.bag.covidcertificate.sdk.core.models.trustlist.Rule;
import ch.admin.bag.covidcertificate.sdk.core.models.trustlist.RuleSet;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.util.UriComponentsBuilder;

@ExtendWith(SpringExtension.class)
@ActiveProfiles({"test"})
@TestPropertySource("classpath:application-test.properties")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class VerificationServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(VerificationServiceTest.class);
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final String verifierBaseUrl = "verifier.test.ch";
    private static final String dscEndpoint = "/trust/v1/keys/updates";
    private static final String rulesEndpoint = "/trust/v2/verificationRules";
    private static final String revocationEndpoint = "/trust/v2/revocationList";
    private static Map<String, String> etagMap = new HashMap<>();
    @Autowired ObjectMapper objectMapper;

    @Test
    @Disabled("Need to mock Verifier Service endpoint")
    void getDSCsTest() throws IOException, URISyntaxException, InterruptedException {
        logger.info("Updating list of DSCs");
        final var params = new HashMap<String, String>();
        params.put("certFormat", "ANDROID");
        Jwks jwks;
        HttpResponse<String> response;
        do {
            logger.info("Sending request to Verifier Service");
            response = getResponse(dscEndpoint, params);
            jwks = objectMapper.readValue(response.body(), Jwks.class);
            assertNotNull(jwks);
            final var nextSince = response.headers().firstValue("X-Next-Since");
            nextSince.ifPresent(next -> params.put("since", next));
        } while (response.headers().firstValue("up-to-date").isEmpty());
    }

    @Test
    @Disabled("Need to mock Verifier Service endpoint")
    void getRulesTest() throws URISyntaxException, IOException, InterruptedException {
        final String response = getResponse(rulesEndpoint, new HashMap<>()).body();
        if (response.isBlank()) {
            logger.info("ETag hasn't changed - No need to update");
        } else {
            final var intermediateRuleSet = objectMapper.readValue(response, IntermediateRuleSet.class);
            assertNotNull(intermediateRuleSet);
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

            logger.info("downloaded {} rules", rules.size());
            ModeRules intermediateModeRules = intermediateRuleSet.getModeRules();
            ch.admin.bag.covidcertificate.sdk.core.models.trustlist.ModeRules sdkModeRules = new ch.admin.bag.covidcertificate.sdk.core.models.trustlist.ModeRules(intermediateModeRules.getActiveModes(), intermediateModeRules.getVerifierActiveModes(), intermediateModeRules.getLogic());
            logger.info("downloaded {} rules", rules.size());
            RuleSet ruleSet = new RuleSet(
                    displayRules,
                    rules,
                    sdkModeRules,
                    intermediateRuleSet.getValueSets(),
                    intermediateRuleSet.getValidDuration());
            assertNotNull(ruleSet);
        }
    }

    private HttpResponse<String> getResponse(String endpoint, Map<String, String> params)
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
        HttpRequest request = HttpRequest.newBuilder().uri(new URI(uri)).GET().build();
        return httpClient.send(request, BodyHandlers.ofString());
    }
}
