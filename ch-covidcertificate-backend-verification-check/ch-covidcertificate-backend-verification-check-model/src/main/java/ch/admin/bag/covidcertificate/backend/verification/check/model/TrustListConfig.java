package ch.admin.bag.covidcertificate.backend.verification.check.model;

import ch.admin.bag.covidcertificate.backend.verification.check.model.cert.ClientCert;
import java.util.List;

public class TrustListConfig {

    private List<ClientCert> trustList;

    public List<ClientCert> getTrustList() {
        return trustList;
    }

    public void setTrustList(
        List<ClientCert> trustList) {
        this.trustList = trustList;
    }
}
