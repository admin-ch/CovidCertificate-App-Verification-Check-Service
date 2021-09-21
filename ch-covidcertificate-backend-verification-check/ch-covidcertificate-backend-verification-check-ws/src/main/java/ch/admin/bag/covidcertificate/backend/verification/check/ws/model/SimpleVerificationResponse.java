package ch.admin.bag.covidcertificate.backend.verification.check.ws.model;

import ch.admin.bag.covidcertificate.sdk.core.models.healthcert.CovidCertificate;

public class SimpleVerificationResponse {

    private final CovidCertificate certificate;
    // TODO: How much information is needed here? Do we need 3 separate boolean fields? Error codes?
    private boolean isValid;

    public SimpleVerificationResponse(CovidCertificate certificate) {
        this.certificate = certificate;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }

    public CovidCertificate getCertificate() {
        return certificate;
    }
}