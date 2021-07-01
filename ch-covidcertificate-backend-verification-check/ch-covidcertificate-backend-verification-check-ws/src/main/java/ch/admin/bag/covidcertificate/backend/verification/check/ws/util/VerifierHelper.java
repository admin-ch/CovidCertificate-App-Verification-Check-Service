package ch.admin.bag.covidcertificate.backend.verification.check.ws.util;

import ch.admin.bag.covidcertificate.backend.verification.check.model.cert.ClientCert;
import java.util.ArrayList;
import java.util.List;

public class VerifierHelper {

    /**
     * Sends a request to the VerifierService to obtain an up-to-date list of DSCs
     *
     * @return a JWKs object as required by the SDK-core mapped from a list of ClientCerts
     */
    public List<ClientCert> getDSCs() {
        // TODO Implement
        return new ArrayList<>();
    }

    /**
     * Sends a request to the VerifierService to obtain an up-to-date list of revoked certificate
     * KIDs
     *
     * @return RevokedCertificates object as required by the SDK-core
     */
    public List<String> getRevokedCerts() {
        // TODO Implement
        return new ArrayList<>();
    }

    /**
     * Sends a request to the VerifierService to obtain an up-to-date list of national rules
     *
     * @return RuleSet object as required by the SDK-core
     */
    public void getNationalRules() {
        // TODO Implement
        return;
    }
}
