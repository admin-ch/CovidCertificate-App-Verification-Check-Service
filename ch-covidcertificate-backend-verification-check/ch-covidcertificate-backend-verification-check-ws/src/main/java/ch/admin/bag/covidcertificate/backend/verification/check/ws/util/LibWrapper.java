package ch.admin.bag.covidcertificate.backend.verification.check.ws.util;

import ch.admin.bag.covidcertificate.backend.verification.check.model.HCertPayload;
import ch.admin.bag.covidcertificate.sdk.core.models.healthcert.DccHolder;
import ch.admin.bag.covidcertificate.sdk.core.models.state.VerificationState;
import ch.admin.bag.covidcertificate.sdk.core.models.trustlist.TrustList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LibWrapper {

    private static final Logger logger = LoggerFactory.getLogger(LibWrapper.class);

    private LibWrapper() {
        logger.error("Shouldn't instantiate util class!");
    }

    public static DccHolder decodeHCert(HCertPayload hCertPayload) {
        // TODO Implement
        return null;
    }

    public static VerificationState verifyDcc(DccHolder dccHolder, TrustList trustList) {
        // TODO Implement
        return null;
    }
}
