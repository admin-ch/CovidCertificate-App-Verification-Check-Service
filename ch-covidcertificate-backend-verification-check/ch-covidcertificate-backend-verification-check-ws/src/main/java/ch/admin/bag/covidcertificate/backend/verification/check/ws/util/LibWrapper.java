package ch.admin.bag.covidcertificate.backend.verification.check.ws.util;

import ch.admin.bag.covidcertificate.backend.verification.check.model.HCertPayload;
import ch.admin.bag.covidcertificate.sdk.core.data.ErrorCodes;
import ch.admin.bag.covidcertificate.sdk.core.decoder.CertificateDecoder;
import ch.admin.bag.covidcertificate.sdk.core.models.healthcert.DccHolder;
import ch.admin.bag.covidcertificate.sdk.core.models.state.DecodeState;
import ch.admin.bag.covidcertificate.sdk.core.models.state.StateError;
import ch.admin.bag.covidcertificate.sdk.core.models.state.VerificationState;
import ch.admin.bag.covidcertificate.sdk.core.models.trustlist.TrustList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LibWrapper {

    private static final Logger logger = LoggerFactory.getLogger(LibWrapper.class);

    private LibWrapper() {
        logger.error("Shouldn't instantiate util class!");
    }

    public static DecodeState decodeHCert(HCertPayload hCertPayload) {
        return CertificateDecoder.decode(hCertPayload.getHcert());
    }

    public static VerificationState verifyDcc(DccHolder dccHolder, TrustList trustList) {
        // TODO Implement
        return new VerificationState.ERROR(
                new StateError(ErrorCodes.GENERAL_OFFLINE, "not implemented", null), null);
    }
}
