package ch.admin.bag.covidcertificate.backend.verification.check.ws.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.admin.bag.covidcertificate.backend.verification.check.ws.model.IntermediateRuleSet;
import ch.admin.bag.covidcertificate.sdk.core.data.AcceptanceCriteriasConstants;
import ch.admin.bag.covidcertificate.sdk.core.data.AcceptedVaccineProvider;
import ch.admin.bag.covidcertificate.sdk.core.decoder.CertificateDecoder;
import ch.admin.bag.covidcertificate.sdk.core.models.healthcert.eu.Eudgc;
import ch.admin.bag.covidcertificate.sdk.core.models.healthcert.eu.VaccinationEntry;
import ch.admin.bag.covidcertificate.sdk.core.models.products.AcceptedVaccine;
import ch.admin.bag.covidcertificate.sdk.core.models.products.Vaccine;
import ch.admin.bag.covidcertificate.sdk.core.models.state.DecodeState;
import ch.admin.bag.covidcertificate.sdk.core.models.state.DecodeState.SUCCESS;
import ch.admin.bag.covidcertificate.sdk.core.models.trustlist.Jwks;
import ch.admin.bag.covidcertificate.sdk.core.models.trustlist.RevokedCertificates;
import ch.admin.bag.covidcertificate.sdk.core.models.trustlist.Rule;
import ch.admin.bag.covidcertificate.sdk.core.models.trustlist.RuleSet;
import ch.admin.bag.covidcertificate.sdk.core.models.trustlist.TrustList;
import ch.admin.bag.covidcertificate.sdk.core.verifier.CertificateVerifier;
import ch.admin.bag.covidcertificate.sdk.core.verifier.nationalrules.NationalRulesVerifier;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.kotlin.KotlinModule;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit test to check the functionality of the decoding + verification pipeline using the core-sdk
 */
class LibWrapperTest {

    private static final String HC1_A =
            "HC1:NCFJ60EG0/3WUWGSLKH47GO0KNJ9DSWQIIWT9CK+500XKY-CE59-G80:84F3ZKG%QU2F30GK JEY50.FK6ZK7:EDOLOPCF8F746KG7+59.Q6+A80:6JM8SX8RM8.A8TL6IA7-Q6.Q6JM8WJCT3EYM8XJC +DXJCCWENF6OF63W5$Q69L6%JC+QE$.32%E6VCHQEU$DE44NXOBJE719$QE0/D+8D-ED.24-G8$:8.JCBECB1A-:8$96646AL60A60S6Q$D.UDRYA 96NF6L/5QW6307KQEPD09WEQDD+Q6TW6FA7C466KCN9E%961A6DL6FA7D46JPCT3E5JDJA76L68463W5/A6..DX%DZJC3/DH$9- NTVDWKEI3DK2D4XOXVD1/DLPCG/DU2D4ZA2T9GY8MPCG/DY-CAY81C9XY8O/EZKEZ96446256V50G7AZQ4CUBCD9-FV-.6+OJROVHIBEI3KMU/TLRYPM0FA9DCTID.GQ$NYE3NPBP90/9IQH24YL7WMO0CNV1 SDB1AHX7:O26872.NV/LC+VJ75L%NGF7PT134ERGJ.I0 /49BB6JA7WKY:AL19PB120CUQ37XL1P9505-YEFJHVETB3CB-KE8EN9BPQIMPRTEW*DU+X2STCJ6O6S4XXVJ$UQNJW6IIO0X20D4S3AWSTHTA5FF7I/J9:8ALF/VP 4K1+8QGI:N0H 91QBHPJLSMNSJC BFZC5YSD.9-9E5R8-.IXUB-OG1RRQR7JEH/5T852EA3T7P6 VPFADBFUN0ZD93MQY07/4OH1FKHL9P95LIG841 BM7EXDR/PLCUUE88+-IX:Q";
    private static final String LT1_A =
            "LT1:6BFY90R10RDWT 9O60GO0000W50JB06H08CK%QC/70YM8N34GB8FN04BC6S5WY01BC9HH597MTKGVC*JC1A6/Q63W5KF6746TPCBEC7ZKW.CU2DNXO VD5$C JC3/DMP8$ILZEDZ CW.C9WE.Y9AY8+S9VIAI3D8WEVM8:S9C+9$PC5$CUZCY$5Y$527BK/CV3VEAFC48$CS/M8WBD543I 2QRK$G6RXQT-T74F$SCMWJ+*VADUJR1T46 /Q+38HH61HVL-U78GRAKUIOIVTWXG5%JL%Q1SPOF9";
    private static final String hcert =
            "HC1:NCFOXN%TS3DH3ZSUZK+.V0ETD%65NL-AH-R6IOOA+I7CGA5I.I554S5.7AT4V22F/8X*G3M9JUPY0BX/KR96R/S09T./0LWTKD33236J3TA3M*4VV2 73-E3GG396B-43O058YIB73A*G3W19UEBY5:PI0EGSP4*2DN43U*0CEBQ/GXQFY73CIBC:G 7376BXBJBAJ UNFMJCRN0H3PQN*E33H3OA70M3FMJIJN523.K5QZ4A+2XEN QT QTHC31M3+E32R44$28A9H0D3ZCL4JMYAZ+S-A5$XKX6T2YC 35H/ITX8GL2-LH/CJTK96L6SR9MU9RFGJA6Q3QR$P2OIC0JVLA8J3ET3:H3A+2+33U SAAUOT3TPTO4UBZIC0JKQTL*QDKBO.AI9BVYTOCFOPS4IJCOT0$89NT2V457U8+9W2KQ-7LF9-DF07U$B97JJ1D7WKP/HLIJLRKF1MFHJP7NVDEBU1J*Z222E.GJ$874 7XTHLSS+%O SC$ZNV+OI7GXCWI+DZJBTQ8PYOR6WZ.VO1T2COOXM/FJBWJK4T97V5GFEIJ.KG9ULH9BKIGBCOJRQPHHA0D$E6% RU40HHS54";

    private static final ObjectMapper objectMapper =
            new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .registerModule(new KotlinModule());
    private static final Logger logger = LoggerFactory.getLogger(LibWrapperTest.class);

    private static CertificateVerifier certificateVerifier;

    @BeforeAll
    static void setup() {
        try {
            final var acceptedVaccines =
                    objectMapper.readValue(
                            "src/main/resources/acceptedCHVaccine.json", AcceptedVaccine.class);
            final var acceptedVaccineProvider =
                    new AcceptedVaccineProvider() {

                        @NotNull
                        @Override
                        public String getVaccineName(@NotNull VaccinationEntry vaccinationEntry) {
                            final var matchingVaccine =
                                    acceptedVaccines.getEntries().stream()
                                            .filter(
                                                    vaccine ->
                                                            vaccine.getCode()
                                                                    .equals(
                                                                            vaccinationEntry
                                                                                    .getMedicinialProduct()))
                                            .findFirst();
                            return matchingVaccine.map(Vaccine::getName).orElse("");
                        }

                        @NotNull
                        @Override
                        public String getProphylaxis(@NotNull VaccinationEntry vaccinationEntry) {
                            final var matchingVaccine =
                                    acceptedVaccines.getEntries().stream()
                                            .filter(
                                                    vaccine ->
                                                            vaccine.getCode()
                                                                    .equals(
                                                                            vaccinationEntry
                                                                                    .getMedicinialProduct()))
                                            .findFirst();
                            return matchingVaccine.map(Vaccine::getProphylaxis).orElse("");
                        }

                        @NotNull
                        @Override
                        public String getAuthHolder(@NotNull VaccinationEntry vaccinationEntry) {
                            final var matchingVaccine =
                                    acceptedVaccines.getEntries().stream()
                                            .filter(
                                                    vaccine ->
                                                            vaccine.getCode()
                                                                    .equals(
                                                                            vaccinationEntry
                                                                                    .getMedicinialProduct()))
                                            .findFirst();
                            return matchingVaccine.map(Vaccine::getAuth_holder).orElse("");
                        }

                        @Nullable
                        @Override
                        public Vaccine getVaccineDataFromList(
                                @NotNull VaccinationEntry vaccinationEntry) {
                            final var matchingVaccine =
                                    acceptedVaccines.getEntries().stream()
                                            .filter(
                                                    vaccine ->
                                                            vaccine.getCode()
                                                                    .equals(
                                                                            vaccinationEntry
                                                                                    .getMedicinialProduct()))
                                            .findFirst();
                            return matchingVaccine.orElse(null);
                        }
                    };
            final var nationalRulesVerifier = new NationalRulesVerifier(acceptedVaccineProvider);
            certificateVerifier = new CertificateVerifier(nationalRulesVerifier);
        } catch (JsonProcessingException e) {
            logger.error("Couldn't load accepted vaccine data from json: {}", e.getMessage());
        }
    }

    @Test
    void decodeTest() {
        // TODO: Assert that invalid hcert can't be decoded
        // Test certificate decoding
        var decodeState = CertificateDecoder.decode(HC1_A);
        assertTrue(decodeState instanceof DecodeState.SUCCESS);
        // Test light certificate decoding
        decodeState = CertificateDecoder.decode(LT1_A);
        assertTrue(decodeState instanceof DecodeState.SUCCESS);
    }

    @Test
    @Disabled("Not implemented")
    void verificationTest() throws JsonProcessingException {

        var clock = Clock.fixed(Instant.parse("2021-05-25T12:00:00Z"), ZoneId.systemDefault());
        final Eudgc eudgc =
                TestDataGenerator.generateVaccineCert(
                        2,
                        2,
                        "ORG-100001699",
                        "EU/1/21/1529/INVALID",
                        AcceptanceCriteriasConstants.TARGET_DISEASE,
                        "1119349007",
                        LocalDate.now(clock).minusDays(10).atStartOfDay());
        assertEquals("asdf", eudgc.getPerson().getFamilyName());
        // TODO: Assert valid cert can and invalid cert can't be verified
        var decoding = CertificateDecoder.decode(HC1_A);
        if (decoding instanceof SUCCESS) {
            var dccHolder = ((SUCCESS) decoding).getDccHolder();
            // TODO: verify
        }
    }

    @Test
    void decodeAndVerify() {
        // TODO: Test full pipeline
    }

    private TrustList createEmptyTrustList() throws JsonProcessingException {
        final var rules =
                objectMapper.readValue("src/test/resources/rules.json", IntermediateRuleSet.class);
        assertNotNull(rules);
        List<Rule> rulesList = new ArrayList<>();
        for (var rule : rules.getRules()) {
            rulesList.add(
                    new Rule(
                            rule.getId(),
                            rule.getBusinessDescription(),
                            rule.getDescription(),
                            rule.getInputParameter(),
                            rule.getLogic()));
        }
        final var ruleSet = new RuleSet(rulesList, rules.getValueSets(), rules.getValidDuration());
        return new TrustList(
                new Jwks(new ArrayList<>()),
                new RevokedCertificates(new ArrayList<>(), 120000L),
                ruleSet);
    }
}
