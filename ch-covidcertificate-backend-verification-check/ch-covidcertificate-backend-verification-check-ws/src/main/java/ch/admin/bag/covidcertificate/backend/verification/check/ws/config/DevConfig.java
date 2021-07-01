package ch.admin.bag.covidcertificate.backend.verification.check.ws.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("dev")
public class DevConfig extends WsBaseConfig {

}

