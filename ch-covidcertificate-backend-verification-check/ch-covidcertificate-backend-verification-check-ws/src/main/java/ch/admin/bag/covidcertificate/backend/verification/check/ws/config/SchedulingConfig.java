package ch.admin.bag.covidcertificate.backend.verification.check.ws.config;

import ch.admin.bag.covidcertificate.backend.verification.check.ws.util.VerifierHelper;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class SchedulingConfig {

    private final VerifierHelper verifierHelper;

    public SchedulingConfig(VerifierHelper verifierHelper) {
        this.verifierHelper = verifierHelper;
    }

    @Scheduled(cron = "${trustlist.cron}")
    public void updateTrustList() {
        verifierHelper.updateTrustListConfig();
    }
}
