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

import ch.admin.bag.covidcertificate.backend.verification.check.ws.VerificationService;
import ch.admin.bag.covidcertificate.backend.verification.check.ws.controller.VerificationController;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.kotlin.KotlinModule;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public abstract class WsBaseConfig implements WebMvcConfigurer {

    @Value("${verifier.baseurl}")
    private String verifierBaseUrl;

    @Value("${verifier.dsc.endpoint:/trust/v1/keys/updates}")
    private String dscEndpoint;

    @Value("${verifier.revocation.endpoint:/trust/v1/revocationList}")
    private String revocationEndpoint;

    @Value("${verifier.rules.endpoint:/trust/v1/verificationRules}")
    private String rulesEndpoint;

    @Bean
    public VerificationController verificationController(VerificationService verificationService) {
        return new VerificationController(verificationService);
    }

    @Bean
    public VerificationService verificationService() {
        return new VerificationService(
                verifierBaseUrl, dscEndpoint, revocationEndpoint, rulesEndpoint);
    }

    @Override
    public void configureMessageConverters(final List<HttpMessageConverter<?>> converters) {
        ObjectMapper objectMapper =
                new ObjectMapper()
                        // Needed to ignore `subjectPublicKeyInfo` field in /updates response
                        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                        .registerModule(new KotlinModule())
                        .registerModule(new JavaTimeModule())
                        .disable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS);
        converters.add(new MappingJackson2HttpMessageConverter(objectMapper));
        WebMvcConfigurer.super.configureMessageConverters(converters);
    }
}
