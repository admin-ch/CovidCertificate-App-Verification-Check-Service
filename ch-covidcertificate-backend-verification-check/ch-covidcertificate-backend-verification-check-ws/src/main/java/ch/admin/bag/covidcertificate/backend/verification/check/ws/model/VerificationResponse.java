package ch.admin.bag.covidcertificate.backend.verification.check.ws.model;

import ch.admin.bag.covidcertificate.sdk.core.models.healthcert.DccHolder;

public class VerificationResponse {

    private boolean valid;
    private DccHolder hcertDecoded;

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public DccHolder getHcertDecoded() {
        return hcertDecoded;
    }

    public void setHcertDecoded(DccHolder hcertDecoded) {
        this.hcertDecoded = hcertDecoded;
    }
}
