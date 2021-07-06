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

import ch.admin.bag.covidcertificate.backend.verification.check.ws.controller.VerificationController;
import ch.admin.bag.covidcertificate.backend.verification.check.ws.model.TrustListConfig;
import ch.admin.bag.covidcertificate.backend.verification.check.ws.util.VerifierHelper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.kotlin.KotlinModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public abstract class WsBaseConfig {

    @Value("${verifier.baseurl}")
    private String verifierBaseUrl;

    @Value("${verifier.dsc.endpoint:/trust/v1/keys/updates}")
    private String dscEndpoint;

    @Value("${verifier.revocation.endpoint:/trust/v1/revocationList}")
    private String revocationEndpoint;

    @Value("${verifier.rules.endpoint:/trust/v1/verificationRules}")
    private String rulesEndpoint;

    @Bean
    public VerificationController verificationController(
            TrustListConfig trustListConfig, VerifierHelper verifierHelper) {
        return new VerificationController(trustListConfig, verifierHelper);
    }

    @Bean
    public TrustListConfig trustListConfig() {
        return new TrustListConfig();
    }

    @Bean
    public VerifierHelper verifierHelper(
            TrustListConfig trustListConfig, ObjectMapper objectMapper) {
        return new VerifierHelper(
                trustListConfig,
                verifierBaseUrl,
                dscEndpoint,
                revocationEndpoint,
                rulesEndpoint,
                objectMapper);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                // Need this to ignore subjectPublicKeyInfo field in /updates response
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .registerModule(new KotlinModule())
                .registerModule(new JavaTimeModule());
    }
}
