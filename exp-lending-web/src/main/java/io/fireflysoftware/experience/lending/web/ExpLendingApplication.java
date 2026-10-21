/*
 * Copyright 2025 Firefly Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package io.fireflysoftware.experience.lending.web;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.web.reactive.config.EnableWebFlux;

/**
 * Spring Boot application entry point for the Experience Lending service.
 * <p>
 * Provides REST APIs for lending journeys, composing domain-tier calls to
 * loan origination, loan servicing, credit scoring, asset finance,
 * product catalogue, and product pricing services.
 */
@SpringBootApplication(
        scanBasePackages = {
                "io.fireflysoftware.experience.lending",
                "org.fireflyframework.web"
        }
)
@EnableWebFlux
@ConfigurationPropertiesScan(basePackages = "io.fireflysoftware.experience.lending")
@OpenAPIDefinition(
        info = @Info(
                title = "${spring.application.name}",
                version = "${spring.application.version}",
                description = "${spring.application.description}",
                contact = @Contact(
                        name = "${spring.application.team.name}",
                        email = "${spring.application.team.email}"
                )
        ),
        servers = {
                @Server(
                        url = "https://openplatform.fireflysoftware.io/exp-lending",
                        description = "Development Environment"
                ),
                @Server(
                        url = "/",
                        description = "Local Development Environment"
                )
        }
)
public class ExpLendingApplication {
    public static void main(String[] args) {
        SpringApplication.run(ExpLendingApplication.class, args);
    }
}
