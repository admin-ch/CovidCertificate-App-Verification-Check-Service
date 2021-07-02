/*
 * Copyright (c) 2021 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */

package ch.admin.bag.covidcertificate.backend.verification.check.ws.controller;

import ch.admin.bag.covidcertificate.backend.verification.check.model.HCertPayload;
import ch.admin.bag.covidcertificate.backend.verification.check.ws.model.TrustListConfig;
import ch.admin.bag.covidcertificate.backend.verification.check.ws.model.VerificationResponse;
import ch.admin.bag.covidcertificate.backend.verification.check.ws.util.LibWrapper;
import ch.admin.bag.covidcertificate.backend.verification.check.ws.util.VerifierHelper;
import ch.admin.bag.covidcertificate.sdk.core.models.state.VerificationState;
import ch.ubique.openapi.docannotations.Documentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("v1")
public class VerificationController {

    private static final Logger logger = LoggerFactory.getLogger(VerificationController.class);

    private final TrustListConfig trustListConfig;
    private final VerifierHelper verifierHelper;

    public VerificationController(TrustListConfig trustListConfig, VerifierHelper verifierHelper) {
        this.trustListConfig = trustListConfig;
        this.verifierHelper = verifierHelper;
    }

    @Documentation(
            description = "Echo endpoint",
            responses = {"200 => Hello from CH CovidCertificate Verification Check WS"})
    @CrossOrigin(origins = {"https://editor.swagger.io"})
    @GetMapping(path = {"", "/"})
    public @ResponseBody String hello() {
        return "Hello from CH CovidCertificate Verification Check WS";
    }

    @PostMapping(path = {"/verify"})
    public @ResponseBody ResponseEntity<VerificationResponse> verify(
            @RequestBody HCertPayload hCertPayload) {
        // Decode hcert
        final var dccHolder = LibWrapper.decodeHCert(hCertPayload);
        // Update trustList
        var trustList = trustListConfig.getTrustList();
        if (trustList == null) {
            verifierHelper.updateTrustListConfig();
        }
        // Verify hcert
        final var verificationState = LibWrapper.verifyDcc(dccHolder, trustList);
        // Build response
        final var verificationResponse = new VerificationResponse();
        verificationResponse.setHcertDecoded(dccHolder);
        verificationResponse.setValid(verificationState instanceof VerificationState.SUCCESS);
        return ResponseEntity.ok(verificationResponse);
    }
}
