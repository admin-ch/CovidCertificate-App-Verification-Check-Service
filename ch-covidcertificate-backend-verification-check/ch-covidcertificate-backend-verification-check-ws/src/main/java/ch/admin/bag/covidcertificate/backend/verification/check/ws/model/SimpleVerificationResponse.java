package ch.admin.bag.covidcertificate.backend.verification.check.ws.model;

import ch.admin.bag.covidcertificate.sdk.core.models.healthcert.CovidCertificate;
import ch.admin.bag.covidcertificate.sdk.core.models.state.VerificationState.ERROR;
import ch.admin.bag.covidcertificate.sdk.core.models.state.VerificationState.INVALID;
import ch.admin.bag.covidcertificate.sdk.core.models.state.VerificationState.SUCCESS;

public class SimpleVerificationResponse {

    private final CovidCertificate certificate;
    private SUCCESS successState;
    private ERROR errorState;
    private INVALID invalidState;

    public SimpleVerificationResponse(CovidCertificate certificate) {
        this.certificate = certificate;
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
