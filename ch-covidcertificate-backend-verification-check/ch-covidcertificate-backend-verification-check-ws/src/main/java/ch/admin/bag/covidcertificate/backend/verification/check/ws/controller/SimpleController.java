package ch.admin.bag.covidcertificate.backend.verification.check.ws.controller;

import ch.admin.bag.covidcertificate.backend.verification.check.model.HCertPayload;
import ch.admin.bag.covidcertificate.backend.verification.check.model.SimpleControllerPayload;
import ch.admin.bag.covidcertificate.backend.verification.check.ws.model.DecodingException;
import ch.admin.bag.covidcertificate.backend.verification.check.ws.model.SimpleVerificationResponse;
import ch.admin.bag.covidcertificate.backend.verification.check.ws.verification.VerificationService;
import ch.admin.bag.covidcertificate.sdk.core.models.state.VerificationState;
import ch.admin.bag.covidcertificate.sdk.core.models.state.VerificationState.ERROR;
import ch.admin.bag.covidcertificate.sdk.core.models.state.VerificationState.INVALID;
import ch.admin.bag.covidcertificate.sdk.core.models.trustlist.ActiveModes;
import ch.ubique.openapi.docannotations.Documentation;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Profile("simple")
@RestController
@RequestMapping("/v1")
public class SimpleController {

    private static final Logger logger = LoggerFactory.getLogger(SimpleController.class);

    private final VerificationService verificationService;

    public SimpleController(VerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @Documentation(
            description = "Simplified certificate verification endpoint",
            responses = {
                "200 => The certificate could be fully decoded - The response contains its verification status",
                "400 => The certificate couldn't be decoded"
            })
    @CrossOrigin(origins = {"https://editor.swagger.io"})
    @PostMapping("/verify")
    public @ResponseBody SimpleVerificationResponse verify(@RequestBody SimpleControllerPayload hCertPayload) {
        String verificationMode = hCertPayload.getMode();
        final var start = Instant.now();

        // Decode hcert
        logger.info("Decoding hcert");
        final var certificateHolder = verificationService.decodeHCert(hCertPayload);

        logger.info("Verifying hcert");
        // Verify hcert
        final var verificationState = verificationService.verifyDccSingleMode(certificateHolder,
                verificationMode);

        // Build response
        final var simpleVerificationResponse =
                new SimpleVerificationResponse(certificateHolder.getCertificate());
        if (verificationState instanceof VerificationState.SUCCESS) {
            simpleVerificationResponse.setSuccessState(
                    (VerificationState.SUCCESS) verificationState);
        } else if (verificationState instanceof ERROR) {
            simpleVerificationResponse.setErrorState((ERROR) verificationState);
        } else {
            simpleVerificationResponse.setInvalidState((INVALID) verificationState);
        }
        logger.info(
                "Checked validity of hcert in {} ms",
                Instant.now().toEpochMilli() - start.toEpochMilli());
        return simpleVerificationResponse;
    }

    @Documentation(
            description = "Get currently valid verification modes"
    )
    @CrossOrigin(origins = {"https://editor.swagger.io"})
    @GetMapping("/modes")
    public @ResponseBody List<String> getVerificationModes() {
        return verificationService.getVerificationModes().stream().map(ActiveModes::getId).collect(
                Collectors.toList());
    }


        @ExceptionHandler(DecodingException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<String> invalidHCert(DecodingException e) {
        logger.info("Decoding exception thrown: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMsg());
    }
}