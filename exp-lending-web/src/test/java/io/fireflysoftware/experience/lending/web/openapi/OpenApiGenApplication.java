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


package io.fireflysoftware.experience.lending.web.openapi;

import org.fireflyframework.web.openapi.EnableOpenApiGen;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Lightweight application used exclusively during the Maven {@code integration-test} phase
 * to generate the OpenAPI specification for the exp-lending service.
 * <p>
 * Started by {@code spring-boot-maven-plugin} on port 18080; the
 * {@code springdoc-openapi-maven-plugin} fetches the spec from
 * {@code /v3/api-docs.yaml} and writes it to {@code target/openapi/openapi.yml}.
 */
@EnableOpenApiGen
@ComponentScan(basePackages = "io.fireflysoftware.experience.lending.web.controllers")
public class OpenApiGenApplication {
    public static void main(String[] args) {
        SpringApplication.run(OpenApiGenApplication.class, args);
    }
}
