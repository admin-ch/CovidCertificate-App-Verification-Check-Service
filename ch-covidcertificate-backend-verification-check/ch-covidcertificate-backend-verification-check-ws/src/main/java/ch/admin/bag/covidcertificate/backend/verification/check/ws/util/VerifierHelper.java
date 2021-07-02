package ch.admin.bag.covidcertificate.backend.verification.check.ws.util;

import ch.admin.bag.covidcertificate.backend.verification.check.ws.model.TrustListConfig;
import ch.admin.bag.covidcertificate.backend.verification.check.model.cert.CertsResponse;
import ch.admin.bag.covidcertificate.backend.verification.check.model.cert.ClientCert;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VerifierHelper {

    private static final Logger logger = LoggerFactory.getLogger(VerifierHelper.class);

    private final TrustListConfig trustListConfig;
    private final String verifierBaseUrl;
    private final String dscEndpoint;
    private final String revocationEndpoint;
    private final String rulesEndpoint;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public VerifierHelper(
            TrustListConfig trustListConfig,
            String verifierBaseUrl,
            String dscEndpoint,
            String revocationEndpoint,
            String rulesEndpoint) {
        this.trustListConfig = trustListConfig;
        this.verifierBaseUrl = verifierBaseUrl;
        this.dscEndpoint = dscEndpoint;
        this.revocationEndpoint = revocationEndpoint;
        this.rulesEndpoint = rulesEndpoint;
    }

    public void updateTrustListConfig() {
        // TODO Implement once core-SDK models available
        getDSCs();
        getRevokedCerts();
        getNationalRules();
    }

    /**
     * Sends a request to the VerifierService to obtain an up-to-date list of DSCs
     *
     * @return a JWKs object as required by the SDK-core mapped from a list of ClientCerts
     */
    private List<ClientCert> getDSCs() {
        logger.info("Updating list of DSCs");
        try {
            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(new URI(verifierBaseUrl + dscEndpoint))
                            .GET()
                            .build();
            final HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
            final CertsResponse certsResponse =
                    objectMapper.readValue(response.body(), CertsResponse.class);
            return certsResponse.getCerts();
        } catch (URISyntaxException | IOException | InterruptedException e) {
            logger.error("An error occurred while downloading the list of DSCs: {}", e.getMessage());
        }
        return new ArrayList<>();
    }

    /**
     * Sends a request to the VerifierService to obtain an up-to-date list of revoked certificate
     * KIDs
     *
     * @return RevokedCertificates object as required by the SDK-core
     */
    private List<String> getRevokedCerts() {
        // TODO Implement
        return new ArrayList<>();
    }

    /**
     * Sends a request to the VerifierService to obtain an up-to-date list of national rules
     *
     * @return RuleSet object as required by the SDK-core
     */
    private void getNationalRules() {
        // TODO Implement
        return;
    }
}
