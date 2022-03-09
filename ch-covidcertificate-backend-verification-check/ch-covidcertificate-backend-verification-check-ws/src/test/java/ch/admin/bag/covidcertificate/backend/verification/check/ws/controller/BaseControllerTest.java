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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ExtendWith(SpringExtension.class)
@ActiveProfiles({"test", "actuator-security"})
@AutoConfigureMockMvc
@TestPropertySource("classpath:application-test.properties")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class BaseControllerTest {

    @Autowired protected MockMvc mockMvc;
    @Autowired protected ObjectMapper objectMapper;

    @Test
    public void testActuatorSecurity() throws Exception {
        var response =
                mockMvc.perform(get("/actuator/health"))
                        .andExpect(status().is2xxSuccessful())
                        .andReturn()
                        .getResponse();
        response =
                mockMvc.perform(get("/actuator/loggers"))
                        .andExpect(status().is(401))
                        .andReturn()
                        .getResponse();
        response =
                mockMvc.perform(
                                get("/actuator/loggers")
                                        .header(
                                                "Authorization",
                                                "Basic cHJvbWV0aGV1czpwcm9tZXRoZXVz"))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse();
    }

}
