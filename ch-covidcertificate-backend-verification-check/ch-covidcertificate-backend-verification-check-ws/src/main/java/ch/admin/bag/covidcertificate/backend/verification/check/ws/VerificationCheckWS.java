/*
 * Copyright (c) 2021 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */

package ch.admin.bag.covidcertificate.backend.verification.check.ws;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(
        basePackages = {
            "ch.admin.bag.covidcertificate.backend.verification.check.ws",
            "ch.admin.bag.covidcertificate.log",
            "ch.admin.bag.covidcertificate.rest"
        })
@EnableAutoConfiguration
public class VerificationCheckWS {

    public static void main(String[] args) {
        SpringApplication.run(VerificationCheckWS.class, args);
    }
}
