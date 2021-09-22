package ch.admin.bag.covidcertificate.backend.verification.check.ws.controller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.admin.bag.covidcertificate.backend.verification.check.model.HCertPayload;
import ch.admin.bag.covidcertificate.backend.verification.check.ws.model.SimpleVerificationResponse;
import ch.admin.bag.covidcertificate.sdk.core.models.state.VerificationState.SUCCESS;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;

class SimpleControllerTest extends BaseControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(SimpleControllerTest.class);

    private static final String BASE_URL = "/simple";
    private static final String VERIFY_ENDPOINT = "/verify";

    private static final String HC1_A =
            "HC1:NCFJ60EG0/3WUWGSLKH47GO0KNJ9DSWQIIWT9CK+500XKY-CE59-G80:84F3ZKG%QU2F30GK JEY50.FK6ZK7:EDOLOPCF8F746KG7+59.Q6+A80:6JM8SX8RM8.A8TL6IA7-Q6.Q6JM8WJCT3EYM8XJC +DXJCCWENF6OF63W5$Q69L6%JC+QE$.32%E6VCHQEU$DE44NXOBJE719$QE0/D+8D-ED.24-G8$:8.JCBECB1A-:8$96646AL60A60S6Q$D.UDRYA 96NF6L/5QW6307KQEPD09WEQDD+Q6TW6FA7C466KCN9E%961A6DL6FA7D46JPCT3E5JDJA76L68463W5/A6..DX%DZJC3/DH$9- NTVDWKEI3DK2D4XOXVD1/DLPCG/DU2D4ZA2T9GY8MPCG/DY-CAY81C9XY8O/EZKEZ96446256V50G7AZQ4CUBCD9-FV-.6+OJROVHIBEI3KMU/TLRYPM0FA9DCTID.GQ$NYE3NPBP90/9IQH24YL7WMO0CNV1 SDB1AHX7:O26872.NV/LC+VJ75L%NGF7PT134ERGJ.I0 /49BB6JA7WKY:AL19PB120CUQ37XL1P9505-YEFJHVETB3CB-KE8EN9BPQIMPRTEW*DU+X2STCJ6O6S4XXVJ$UQNJW6IIO0X20D4S3AWSTHTA5FF7I/J9:8ALF/VP 4K1+8QGI:N0H 91QBHPJLSMNSJC BFZC5YSD.9-9E5R8-.IXUB-OG1RRQR7JEH/5T852EA3T7P6 VPFADBFUN0ZD93MQY07/4OH1FKHL9P95LIG841 BM7EXDR/PLCUUE88+-IX:Q";

    @Test
    @Disabled("Need to mock verifier service endpoint")
    void invalidCertTest() throws Exception {
        var hCertPayload = new HCertPayload();
        hCertPayload.setHcert("HC1:example");
        final MockHttpServletResponse response =
                mockMvc.perform(
                                post(BASE_URL + VERIFY_ENDPOINT)
                                        .content(objectMapper.writeValueAsString(hCertPayload))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .accept(MediaType.APPLICATION_JSON))
                        .andExpect(status().isBadRequest())
                        .andReturn()
                        .getResponse();
    }

    @Test
    @Disabled("Need to mock verifier service endpoint")
    void validCertTest() throws Exception {
        var hCertPayload = new HCertPayload();
        hCertPayload.setHcert(HC1_A);
        final MockHttpServletResponse response =
                mockMvc.perform(
                                post(BASE_URL + VERIFY_ENDPOINT)
                                        .content(objectMapper.writeValueAsString(hCertPayload))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .accept(MediaType.APPLICATION_JSON))
                        .andExpect(status().is2xxSuccessful())
                        .andReturn()
                        .getResponse();
        assertFalse(response.getContentAsString().isEmpty());
        final var verificationResponse =
                objectMapper.readValue(
                        response.getContentAsString(), SimpleVerificationResponse.class);
        assertTrue(verificationResponse.getSuccessState() instanceof SUCCESS);
    }
}
