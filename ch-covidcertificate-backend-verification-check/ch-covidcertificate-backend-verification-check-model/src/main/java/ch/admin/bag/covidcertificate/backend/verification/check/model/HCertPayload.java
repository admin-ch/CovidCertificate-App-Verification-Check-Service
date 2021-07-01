package ch.admin.bag.covidcertificate.backend.verification.check.model;

import ch.ubique.openapi.docannotations.Documentation;
import javax.validation.constraints.NotNull;

public class HCertPayload {

    @Documentation(
            description = "Base-45-encoded covid certificate to be transformed with prefix HC1:",
            example = "HC1:NCFS...MPIW4")
    @NotNull
    private String hcert;

    public String getHcert() {
        return hcert;
    }

    public void setHcert(String hcert) {
        this.hcert = hcert;
    }
}
