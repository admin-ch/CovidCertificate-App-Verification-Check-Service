package ch.admin.bag.covidcertificate.backend.verification.check.ws.model;

import ch.admin.bag.covidcertificate.sdk.core.models.healthcert.DccHolder;
import ch.admin.bag.covidcertificate.sdk.core.models.state.VerificationState;
import ch.admin.bag.covidcertificate.sdk.core.models.state.VerificationState.ERROR;
import ch.admin.bag.covidcertificate.sdk.core.models.state.VerificationState.INVALID;
import ch.admin.bag.covidcertificate.sdk.core.models.state.VerificationState.SUCCESS;

public class VerificationResponse {

    private SUCCESS successState;
    private ERROR errorState;
    private INVALID invalidState;
    private DccHolder hcertDecoded;

    public VerificationState getSuccessState() {
        return successState;
    }

    public void setSuccessState(SUCCESS successState) {
        this.successState = successState;
    }

    public DccHolder getHcertDecoded() {
        return hcertDecoded;
    }

    public void setHcertDecoded(DccHolder hcertDecoded) {
        this.hcertDecoded = hcertDecoded;
    }

    public ERROR getErrorState() {
        return errorState;
    }

    public void setErrorState(
        ERROR errorState) {
        this.errorState = errorState;
    }

    public INVALID getInvalidState() {
        return invalidState;
    }

    public void setInvalidState(
        INVALID invalidState) {
        this.invalidState = invalidState;
    }
}
