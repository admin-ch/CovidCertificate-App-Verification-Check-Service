package ch.admin.bag.covidcertificate.backend.verification.check.ws.model;

import ch.admin.bag.covidcertificate.sdk.core.models.trustlist.Jwks;
import java.util.ArrayList;
import java.util.List;

public class CertsResponse {
    private List<Jwks> certs = new ArrayList<>();

    public CertsResponse(List<Jwks> certs) {
        if (certs == null) {
            certs = new ArrayList<>();
        }
        this.certs = certs;
    }

    public List<Jwks> getCerts() {
        return certs;
    }

    public void setCerts(List<Jwks> certs) {
        this.certs = certs;
    }
}
