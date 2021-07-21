package ch.admin.bag.covidcertificate.backend.verification.check.ws.model;

import ch.admin.bag.covidcertificate.sdk.core.models.trustlist.TrustList;
import java.time.Instant;

public class TrustListConfig {

    private TrustList trustList;
    private Instant lastSync;

    public TrustList getTrustList() {
        return trustList;
    }

    public void setTrustList(TrustList trustList) {
        this.trustList = trustList;
    }

    public Instant getLastSync() {
        return lastSync;
    }

    public void setLastSync(Instant lastSync) {
        this.lastSync = lastSync;
    }

    public boolean isOutdated() {
        Instant now = Instant.now();
        return this.lastSync.isBefore(
                        now.minusMillis(trustList.getRevokedCertificates().getValidDuration()))
                || this.lastSync.isBefore(
                        now.minusMillis(trustList.getRuleSet().getValidDuration()));
    }
}
