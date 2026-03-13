package com.firefly.experience.lending.web.openapi;

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
@ComponentScan(basePackages = "com.firefly.experience.lending.web.controllers")
public class OpenApiGenApplication {
    public static void main(String[] args) {
        SpringApplication.run(OpenApiGenApplication.class, args);
    }
}
