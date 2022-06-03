package ch.admin.bag.covidcertificate.backend.verification.check.ws.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.admin.bag.covidcertificate.backend.verification.check.ws.verification.TestData;
import ch.admin.bag.covidcertificate.backend.verification.check.ws.verification.VerifyWrapper;
import ch.admin.bag.covidcertificate.sdk.core.data.AcceptanceCriteriasConstants;
import ch.admin.bag.covidcertificate.sdk.core.decoder.CertificateDecoder;
import ch.admin.bag.covidcertificate.sdk.core.models.healthcert.eu.DccCert;
import ch.admin.bag.covidcertificate.sdk.core.models.state.CheckNationalRulesState;
import ch.admin.bag.covidcertificate.sdk.core.models.state.CheckRevocationState;
import ch.admin.bag.covidcertificate.sdk.core.models.state.CheckSignatureState;
import ch.admin.bag.covidcertificate.sdk.core.models.state.DecodeState;
import ch.admin.bag.covidcertificate.sdk.core.models.state.DecodeState.SUCCESS;
import ch.admin.bag.covidcertificate.sdk.core.models.state.VerificationState;
import ch.admin.bag.covidcertificate.sdk.core.models.state.VerificationState.INVALID;
import ch.admin.bag.covidcertificate.sdk.core.models.trustlist.TrustList;
import ch.admin.bag.covidcertificate.sdk.core.verifier.CertificateVerifier;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Unit test to check the functionality of the decoding + verification pipeline using the core-sdk
 */
@ExtendWith(SpringExtension.class)
@ActiveProfiles({"test"})
@TestPropertySource("classpath:application-test.properties")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LibWrapperTest {

    private static final String HC1_A =
            "HC1:NCFB60MG0/3WUWGSLKH47GO0SK7KFDCBOECI9CKW500XK0JCV498F3: BQE64F3+JJ+NMY50.FK6ZK7:EDOLOPCO8F6%E3.DA%EOPC1G72A6YM83G7DB8ES8/G8.96Y47ES8.96ZA7$962X6-R8SG6UPC0JCZ69FVCBJ0LVC6JD846KF6C463W5EM6+EDG8F3I80/D6$CBECSUER:C2$NS346$C2%E9VC- CSUE145GB8JA5B$D% D3IA4W5646946846.96.JCP9EJY8L/5M/5546.96VF63KC/SC4KCD3DX47B46IL6646H*6Z/E5JD%96IA74R6646407GVC*JC1A6OA73W5Y96B46TPCBEC7ZKW.C2VCDECY CI3DGPC8$CLPCG/DFUCOB8XY8I3D5WEEB8YZAO/EZKEZ967L6256V50MAOCTIEHMJ*E62F8$51G4+KEXLP1ZTO1CS538*5DA0R0QW/3ZHD3IO4OKGBN+WQEBI2+KQ9SM+NRGTV KQ72F%OYWPSOL-0W2.96%25HRG/B16KP:GS%JR$P+24U9MJ4NRE0K89SB9*UB03E.S3P 6QM2/ODSS1WZ73S3LA8W*5.4BI6OLYU53I+*7  HDYKXG6:/7X15F9A.4J2RB0Z1GTLYBS96HSZH%0D5QCU7I+:T8JVUNMZ:7.S6-XOG1LVQD5004KGYHIYMM-$IFKD+KUSEA/YBI04//7:0GYRS7 J51FT.5D2GKYRD.Q$QSJAG.YVQFNLF58GU-M2R6KB5KG/L7JEVY1TRDC-P8YH:R1U425JF0ENJ10PS24EWKPHXAFFZQ";
    private static final String LT1_A =
            "LT1:6BFY90R10RDWT 9O60GO0000W50JB06H08CK%QC/70YM8N34GB8FN04BC6S5WY01BC9HH597MTKGVC*JC1A6/Q63W5KF6746TPCBEC7ZKW.CU2DNXO VD5$C JC3/DMP8$ILZEDZ CW.C9WE.Y9AY8+S9VIAI3D8WEVM8:S9C+9$PC5$CUZCY$5Y$527BK/CV3VEAFC48$CS/M8WBD543I 2QRK$G6RXQT-T74F$SCMWJ+*VADUJR1T46 /Q+38HH61HVL-U78GRAKUIOIVTWXG5%JL%Q1SPOF9";

    private static final Logger logger = LoggerFactory.getLogger(LibWrapperTest.class);
    private static CertificateVerifier certificateVerifier = new CertificateVerifier();

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void decodeTest() {
        // TODO: Assert that invalid hcert can't be decoded
        // Test certificate decoding
        var decodeState = CertificateDecoder.decode(HC1_A);
        assertTrue(decodeState instanceof DecodeState.SUCCESS);
        // Test light certificate decoding
        decodeState = CertificateDecoder.decode(LT1_A);
        assertTrue(decodeState instanceof DecodeState.ERROR);
    }

    @Test
    void verificationTest() {

        // Create CertificateHolder
        var clock = Clock.fixed(Instant.parse("2021-05-25T12:00:00Z"), ZoneId.systemDefault());
        final DccCert eudgc =
                TestDataGenerator.generateVaccineCert(
                        2,
                        2,
                        "ORG-100001699",
                        "EU/1/21/1529/INVALID",
                        AcceptanceCriteriasConstants.TARGET_DISEASE,
                        "1119349007",
                        LocalDate.now(clock).minusDays(10).atStartOfDay());
        assertEquals("asdf", eudgc.getPerson().getFamilyName());

        // Decode cert
        var decoding = CertificateDecoder.decode(HC1_A);
        assertTrue(decoding instanceof SUCCESS);
        var certificateHolder = ((SUCCESS) decoding).getCertificateHolder();

        // Assert wrongly signed cert can't be verified
        TrustList trustList =
                TestData.createTrustList(
                        TestData.getHardcodedSigningKeys("abn"), Collections.emptyList(), null);
        VerificationState verificationState =
                VerifyWrapper.verifyWallet(certificateVerifier, certificateHolder, trustList);
        assertTrue(verificationState instanceof INVALID);
        assertTrue(
                ((INVALID) verificationState).getSignatureState()
                        instanceof CheckSignatureState.INVALID);
        assertTrue(
                ((INVALID) verificationState).getNationalRulesState()
                        instanceof CheckNationalRulesState.INVALID);
        assertTrue(
                ((INVALID) verificationState).getRevocationState()
                        instanceof CheckRevocationState.SUCCESS);

        // Assert correctly signed cert can be verified
        trustList =
                TestData.createTrustList(
                        TestData.getHardcodedSigningKeys("dev"), Collections.emptyList(), null);
        verificationState = VerifyWrapper.verifyWallet(certificateVerifier, certificateHolder, trustList);
        assertTrue(verificationState instanceof INVALID);
        assertTrue(
                ((INVALID) verificationState).getSignatureState()
                        instanceof CheckSignatureState.SUCCESS);
        assertTrue(
                ((INVALID) verificationState).getNationalRulesState()
                        instanceof CheckNationalRulesState.INVALID);
        assertTrue(
                ((INVALID) verificationState).getRevocationState()
                        instanceof CheckRevocationState.SUCCESS);
        ;
    }
}