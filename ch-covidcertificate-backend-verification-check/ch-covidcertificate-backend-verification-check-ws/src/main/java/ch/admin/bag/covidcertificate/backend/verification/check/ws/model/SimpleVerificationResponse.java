package ch.admin.bag.covidcertificate.backend.verification.check.ws.model;

import ch.admin.bag.covidcertificate.sdk.core.models.healthcert.CovidCertificate;
import ch.admin.bag.covidcertificate.sdk.core.models.healthcert.light.ChLightCert;
import ch.admin.bag.covidcertificate.sdk.core.models.state.VerificationState.ERROR;
import ch.admin.bag.covidcertificate.sdk.core.models.state.VerificationState.INVALID;
import ch.admin.bag.covidcertificate.sdk.core.models.state.VerificationState.SUCCESS;

public class SimpleVerificationResponse {

    private final ChLightCert certificate;
    private SUCCESS successState;
    private ERROR errorState;
    private INVALID invalidState;

    public SimpleVerificationResponse(CovidCertificate certificate) {
        this.certificate =
                new ChLightCert(
                        "1.0.0",
                        certificate.getPersonName(),
                        certificate.getFormattedDateOfBirth());
    }

    public CovidCertificate getCertificate() {
        return certificate;
    }

    public SUCCESS getSuccessState() {
        return successState;
    }

    public void setSuccessState(SUCCESS successState) {
        this.successState = successState;
    }

    public ERROR getErrorState() {
        return errorState;
    }

    public void setErrorState(ERROR errorState) {
        this.errorState = errorState;
    }

    public INVALID getInvalidState() {
        return invalidState;
    }

    public void setInvalidState(INVALID invalidState) {
        this.invalidState = invalidState;
    }
}
