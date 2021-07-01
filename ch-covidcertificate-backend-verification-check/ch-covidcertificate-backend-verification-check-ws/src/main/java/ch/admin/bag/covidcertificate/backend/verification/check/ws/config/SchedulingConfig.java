package ch.admin.bag.covidcertificate.backend.verification.check.ws.config;

import ch.admin.bag.covidcertificate.backend.verification.check.model.TrustListConfig;
import ch.admin.bag.covidcertificate.backend.verification.check.ws.util.VerifierHelper;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class SchedulingConfig {

    private final TrustListConfig trustListConfig;
    private final VerifierHelper verifierHelper;

    public SchedulingConfig(TrustListConfig trustListConfig, VerifierHelper verifierHelper) {
        this.trustListConfig = trustListConfig;
        this.verifierHelper = verifierHelper;
    }

    @Scheduled(cron = "${trustlist.cron}")
    public void updateTrustList() {
        // TODO Update local cached copy of trustList
        verifierHelper.getDSCs();
        verifierHelper.getRevokedCerts();
        verifierHelper.getNationalRules();
    }
}
