package ch.admin.bag.covidcertificate.backend.verification.check.ws.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ch.admin.bag.covidcertificate.backend.verification.check.model.HCertPayload;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;

class VerificationControllerTest extends BaseControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(VerificationControllerTest.class);

    private static final String BASE_URL = "/v1";
    private static final String VERIFY_ENDPOINT = "/verify";

    @Test
    void helloTest() throws Exception {
        final MockHttpServletResponse response =
                mockMvc.perform(get(BASE_URL + "/").accept(MediaType.TEXT_PLAIN))
                        .andExpect(status().is2xxSuccessful())
                        .andReturn()
                        .getResponse();

        assertNotNull(response);
        assertEquals(
                "Hello from CH CovidCertificate Verification Check WS",
                response.getContentAsString());
    }

    @Test
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
    @Disabled("Not implemented")
    void validCertTest() throws Exception {
        var hCertPayload = new HCertPayload();
        hCertPayload.setHcert("HC1:example");
        final MockHttpServletResponse response =
                mockMvc.perform(
                                post(BASE_URL + VERIFY_ENDPOINT)
                                        .content(objectMapper.writeValueAsString(hCertPayload))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .accept(MediaType.APPLICATION_JSON))
                        .andExpect(status().is2xxSuccessful())
                        .andReturn()
                        .getResponse();
    }
}
