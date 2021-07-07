package ch.admin.bag.covidcertificate.backend.verification.check.ws.config;

import ch.admin.bag.covidcertificate.backend.verification.check.ws.verification.VerificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class SchedulingConfig {

    private static final Logger logger = LoggerFactory.getLogger(SchedulingConfig.class);

    private final VerificationService verificationService;

    public SchedulingConfig(VerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @Scheduled(cron = "${trustlist.cron:0 10 * ? * *}")
    public void updateTrustList() {
        try {
            verificationService.updateTrustListConfig();
        } catch (Exception e) {
            logger.error("failed to update trust list config", e);
        }
    }
}
