package ch.admin.bag.covidcertificate.backend.verification.check.ws.util;

import ch.admin.bag.covidcertificate.backend.verification.check.model.HCertPayload;
import ch.admin.bag.covidcertificate.sdk.core.decoder.CertificateDecoder;
import ch.admin.bag.covidcertificate.sdk.core.models.healthcert.DccHolder;
import ch.admin.bag.covidcertificate.sdk.core.models.state.DecodeState;
import ch.admin.bag.covidcertificate.sdk.core.models.state.VerificationState;
import ch.admin.bag.covidcertificate.sdk.core.models.state.VerificationState.SUCCESS;
import ch.admin.bag.covidcertificate.sdk.core.models.trustlist.TrustList;
import ch.admin.bag.covidcertificate.sdk.core.verifier.nationalrules.ValidityRange;
import java.time.LocalDateTime;
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
        return new SUCCESS(
                new ValidityRange(LocalDateTime.now(), LocalDateTime.now().plusHours(1)));
    }
}
