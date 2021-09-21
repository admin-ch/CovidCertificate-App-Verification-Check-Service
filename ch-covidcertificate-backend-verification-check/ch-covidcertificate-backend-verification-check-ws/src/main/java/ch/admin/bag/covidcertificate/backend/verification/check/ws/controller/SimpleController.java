package ch.admin.bag.covidcertificate.backend.verification.check.ws.controller;

import ch.admin.bag.covidcertificate.backend.verification.check.model.HCertPayload;
import ch.admin.bag.covidcertificate.backend.verification.check.ws.model.DecodingException;
import ch.admin.bag.covidcertificate.backend.verification.check.ws.model.SimpleVerificationResponse;
import ch.admin.bag.covidcertificate.backend.verification.check.ws.verification.VerificationService;
import ch.admin.bag.covidcertificate.sdk.core.models.state.VerificationState;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("simple")
public class SimpleController {

    private static final Logger logger = LoggerFactory.getLogger(SimpleController.class);

    private final VerificationService verificationService;

    public SimpleController(VerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @PostMapping("/verify")
    public @ResponseBody ResponseEntity<SimpleVerificationResponse> verify(
            @RequestBody HCertPayload hCertPayload) {
        final var start = Instant.now();
        // Decode hcert
        logger.info("Decoding hcert");
        final var certificateHolder = verificationService.decodeHCert(hCertPayload);

        logger.info("Verifying hcert");
        // Verify hcert
        final var verificationState = verificationService.verifyDcc(certificateHolder);

        // Build response
        final var simpleVerificationResponse =
                new SimpleVerificationResponse(certificateHolder.getCertificate());
        simpleVerificationResponse.setValid(verificationState instanceof VerificationState.SUCCESS);
        logger.info(
                "Successfuly verified hcert in {} ms",
                Instant.now().toEpochMilli() - start.toEpochMilli());
        return ResponseEntity.status(200).body(simpleVerificationResponse);
    }

    @ExceptionHandler(DecodingException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<String> invalidHCert(DecodingException e) {
        logger.info("Decoding exception thrown: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
}
