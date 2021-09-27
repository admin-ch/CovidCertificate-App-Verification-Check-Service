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
import ch.admin.bag.covidcertificate.backend.verification.check.ws.model.DecodingException;
import ch.admin.bag.covidcertificate.backend.verification.check.ws.model.VerificationResponse;
import ch.admin.bag.covidcertificate.backend.verification.check.ws.verification.VerificationService;
import ch.admin.bag.covidcertificate.sdk.core.models.state.VerificationState;
import ch.admin.bag.covidcertificate.sdk.core.models.state.VerificationState.ERROR;
import ch.admin.bag.covidcertificate.sdk.core.models.state.VerificationState.INVALID;
import ch.ubique.openapi.docannotations.Documentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Profile("detailed")
@RestController
@RequestMapping("v1")
public class VerificationController {

    private static final Logger logger = LoggerFactory.getLogger(VerificationController.class);

    private final VerificationService verificationService;

    public VerificationController(VerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @Documentation(
            description = "Echo endpoint",
            responses = {"200 => Hello from CH CovidCertificate Verification Check WS"})
    @CrossOrigin(origins = {"https://editor.swagger.io"})
    @GetMapping(path = {"", "/"})
    public @ResponseBody String hello() {
        return "Hello from CH CovidCertificate Verification Check WS";
    }

    @Documentation(
            description = "Certificate verification endpoint",
            responses = {
                "200 => The certificate could be fully decoded - The response contains its verification status",
                "400 => The certificate couldn't be decoded"
            })
    @CrossOrigin(origins = {"https://editor.swagger.io"})
    @PostMapping(path = {"/verify"})
    public @ResponseBody VerificationResponse verify(
            @RequestBody HCertPayload hCertPayload) {
        // Decode hcert
        final var certificateHolder = verificationService.decodeHCert(hCertPayload);

        // Verify hcert
        final var verificationState = verificationService.verifyDcc(certificateHolder);

        // Build response
        final var verificationResponse = new VerificationResponse();
        verificationResponse.setHcertDecoded(certificateHolder);
        if (verificationState instanceof VerificationState.SUCCESS) {
            verificationResponse.setSuccessState((VerificationState.SUCCESS) verificationState);
        } else if (verificationState instanceof ERROR) {
            verificationResponse.setErrorState((ERROR) verificationState);
        } else {
            verificationResponse.setInvalidState((INVALID) verificationState);
        }
        return verificationResponse;
    }

    @ExceptionHandler(DecodingException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<String> invalidHCert(DecodingException e) {
        logger.info("Decoding exception thrown: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
}
