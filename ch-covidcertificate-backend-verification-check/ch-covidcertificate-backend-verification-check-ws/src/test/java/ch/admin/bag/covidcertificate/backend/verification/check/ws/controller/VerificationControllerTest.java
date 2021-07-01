package ch.admin.bag.covidcertificate.backend.verification.check.ws.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;

class VerificationControllerTest extends BaseControllerTest {

    private static final Logger logger =
        LoggerFactory.getLogger(VerificationControllerTest.class);

    private static final String BASE_URL = "/v1/verify";

    @Test
    void helloTest() throws Exception {
        final MockHttpServletResponse response =
            mockMvc.perform(get(BASE_URL + "/").accept(MediaType.TEXT_PLAIN))
                .andExpect(status().is2xxSuccessful())
                .andReturn()
                .getResponse();

        assertNotNull(response);
        assertEquals(
            "Hello from CH CovidCertificate Verification Check WS", response.getContentAsString());
    }
}