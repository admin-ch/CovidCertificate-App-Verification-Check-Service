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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public abstract class WsBaseConfig {

    @Value("${verifier.baseurl}")
    private String verifierBaseUrl;

    // TODO: Modify caching logic to keep eTag and send request to /list endpoint
    @Value("${verifier.dsc.endpoint:/trust/v1/keys/updates}")
    private String dscEndpoint;

    @Value("${verifier.revocation.endpoint:/trust/v1/revocationList}")
    private String revocationEndpoint;

    @Value("${verifier.rules.endpoint:/trust/v1/verificationRules}")
    private String rulesEndpoint;

    @Value("${verifier.rules.endpoint:/trust/v1/metadata}")
    private String valueSetsEndpoint;

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
    public VerifierHelper verifierHelper(TrustListConfig trustListConfig) {
        return new VerifierHelper(
                trustListConfig,
                verifierBaseUrl,
                dscEndpoint,
                revocationEndpoint,
                rulesEndpoint,
                valueSetsEndpoint);
    }
}
