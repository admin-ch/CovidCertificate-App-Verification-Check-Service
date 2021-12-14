package ch.admin.bag.covidcertificate.backend.verification.check.model;

import javax.validation.constraints.NotNull;

public class SimpleControllerPayload extends HCertPayload {
    @NotNull
    private String mode;

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }
}
