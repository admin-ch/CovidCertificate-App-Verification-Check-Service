package ch.admin.bag.covidcertificate.backend.verification.check.ws.model;

import ch.admin.bag.covidcertificate.sdk.core.models.trustlist.TrustList;

public class TrustListConfig {

    private TrustList trustList;

    public TrustList getTrustList() {
        return trustList;
    }

    public void setTrustList(TrustList trustList) {
        this.trustList = trustList;
    }
}
