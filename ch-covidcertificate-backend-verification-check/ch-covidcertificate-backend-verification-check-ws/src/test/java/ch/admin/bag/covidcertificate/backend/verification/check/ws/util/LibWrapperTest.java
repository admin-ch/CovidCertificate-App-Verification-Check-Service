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
            "HC1:NCFJ60EG0/3WUWGSLKH47GO0KNJ9DSWQIIWT9CK+500XKY-CE59-G80:84F3ZKG%QU2F30GK JEY50.FK6ZK7:EDOLOPCF8F746KG7+59.Q6+A80:6JM8SX8RM8.A8TL6IA7-Q6.Q6JM8WJCT3EYM8XJC +DXJCCWENF6OF63W5$Q69L6%JC+QE$.32%E6VCHQEU$DE44NXOBJE719$QE0/D+8D-ED.24-G8$:8.JCBECB1A-:8$96646AL60A60S6Q$D.UDRYA 96NF6L/5QW6307KQEPD09WEQDD+Q6TW6FA7C466KCN9E%961A6DL6FA7D46JPCT3E5JDJA76L68463W5/A6..DX%DZJC3/DH$9- NTVDWKEI3DK2D4XOXVD1/DLPCG/DU2D4ZA2T9GY8MPCG/DY-CAY81C9XY8O/EZKEZ96446256V50G7AZQ4CUBCD9-FV-.6+OJROVHIBEI3KMU/TLRYPM0FA9DCTID.GQ$NYE3NPBP90/9IQH24YL7WMO0CNV1 SDB1AHX7:O26872.NV/LC+VJ75L%NGF7PT134ERGJ.I0 /49BB6JA7WKY:AL19PB120CUQ37XL1P9505-YEFJHVETB3CB-KE8EN9BPQIMPRTEW*DU+X2STCJ6O6S4XXVJ$UQNJW6IIO0X20D4S3AWSTHTA5FF7I/J9:8ALF/VP 4K1+8QGI:N0H 91QBHPJLSMNSJC BFZC5YSD.9-9E5R8-.IXUB-OG1RRQR7JEH/5T852EA3T7P6 VPFADBFUN0ZD93MQY07/4OH1FKHL9P95LIG841 BM7EXDR/PLCUUE88+-IX:Q";
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