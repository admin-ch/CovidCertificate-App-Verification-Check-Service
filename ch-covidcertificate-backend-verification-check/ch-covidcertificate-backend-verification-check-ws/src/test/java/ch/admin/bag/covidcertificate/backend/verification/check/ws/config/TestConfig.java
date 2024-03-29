/*
 * Copyright (c) 2021 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */

package ch.admin.bag.covidcertificate.backend.verification.check.ws.config;

import ch.admin.bag.covidcertificate.backend.verification.check.ws.jackson.CustomInstantSerializer;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.kotlin.KotlinModule;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Profile("test")
@Configuration
public class TestConfig implements WebMvcConfigurer {

    @Value("${verifier.baseurl}")
    private String verifierBaseUrl;

    @Value("${verifier.dsc.endpoint:/trust/v1/keys/updates}")
    private String dscEndpoint;

    @Value("${verifier.revocation.endpoint:/trust/v2/revocationList}")
    private String revocationEndpoint;

    @Value("${verifier.rules.endpoint:/trust/v1/verificationRules}")
    private String rulesEndpoint;

    //    @Bean
    //    public VerificationController verificationController(VerificationService
    // verificationService) {
    //        return new VerificationController(verificationService);
    //    }

    //    @Bean
    //    public VerificationService verificationService(ObjectMapper objectMapper) {
    //        return new VerificationService(
    //                verifierBaseUrl,
    //                dscEndpoint,
    //                revocationEndpoint,
    //                rulesEndpoint,
    //                "none",
    //                objectMapper);
    //    }

    @Override
    public void configureMessageConverters(final List<HttpMessageConverter<?>> converters) {
        converters.add(new MappingJackson2HttpMessageConverter(objectMapper()));
        WebMvcConfigurer.super.configureMessageConverters(converters);
    }

    @Bean
    public ObjectMapper objectMapper() {
        SimpleModule serialization = new SimpleModule();
        serialization.addSerializer(Instant.class, new CustomInstantSerializer());
        ObjectMapper objectMapper =
                new ObjectMapper()
                        // Needed to ignore `subjectPublicKeyInfo` field in /updates response
                        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                        .registerModule(new KotlinModule())
                        .registerModule(new JavaTimeModule())
                        .registerModule(serialization)
                        .disable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS);
        return objectMapper;
    }
}
