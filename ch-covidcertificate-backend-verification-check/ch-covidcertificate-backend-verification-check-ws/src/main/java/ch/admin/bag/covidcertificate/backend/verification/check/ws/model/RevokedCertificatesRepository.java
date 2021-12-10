package ch.admin.bag.covidcertificate.backend.verification.check.ws.model;

import ch.admin.bag.covidcertificate.sdk.core.models.trustlist.RevokedCertificatesStore;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public class RevokedCertificatesRepository implements RevokedCertificatesStore {
    private RevokedCertificates revokedCertificates = new RevokedCertificates(new ArrayList<>(), 0);

    public RevokedCertificatesRepository(RevokedCertificates revokedCertificates){
        this.revokedCertificates = revokedCertificates;
    }

    @Override
    public void addCertificates(@NotNull List<String> list) {
        revokedCertificates.getRevokedCerts().addAll(list);
    }

    @Override
    public boolean containsCertificate(@NotNull String s) {
        return revokedCertificates.getRevokedCerts().contains(s);
    }

    public long getValidDuration(){
        return revokedCertificates.getValidDuration();
    }

    public static class RevokedCertificates {
        private List<String> revokedCerts;
        private long validDuration;
        public RevokedCertificates(){}

        public RevokedCertificates(List<String> revokedCerts, long validDuration){
            this.revokedCerts = revokedCerts;
            this.validDuration = validDuration;
        }

        public List<String> getRevokedCerts() {
            return revokedCerts;
        }

        public void setRevokedCerts(List<String> revokedCerts) {
            this.revokedCerts = revokedCerts;
        }

        public long getValidDuration() {
            return validDuration;
        }

        public void setValidDuration(long validDuration) {
            this.validDuration = validDuration;
        }
    }
}
