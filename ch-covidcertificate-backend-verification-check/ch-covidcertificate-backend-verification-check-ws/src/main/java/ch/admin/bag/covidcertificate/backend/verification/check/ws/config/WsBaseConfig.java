/*
 * Copyright (c) 2021 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */

package ch.admin.bag.covidcertificate.backend.verification.check.ws.config;

import ch.admin.bag.covidcertificate.backend.verification.check.ws.jackson.CustomInstantSerializer;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.kotlin.KotlinModule;
import java.time.Instant;
import java.util.Collections;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

@Configuration
public abstract class WsBaseConfig {

    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
        MappingJackson2HttpMessageConverter jacksonConverter =
                new MappingJackson2HttpMessageConverter(objectMapper());
        jacksonConverter.setSupportedMediaTypes(
                Collections.singletonList(MediaType.APPLICATION_JSON));
        return jacksonConverter;
    }

    @Bean
    public ObjectMapper objectMapper() {
        var serialization = new SimpleModule();
        serialization.addSerializer(Instant.class, new CustomInstantSerializer());
        return new ObjectMapper()
                // Needed to ignore `subjectPublicKeyInfo` field in /updates response
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .registerModule(new KotlinModule())
                .registerModule(new JavaTimeModule())
                .registerModule(serialization)
                .disable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS);
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
