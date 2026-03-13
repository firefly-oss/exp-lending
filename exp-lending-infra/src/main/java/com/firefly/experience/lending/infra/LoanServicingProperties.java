package com.firefly.experience.lending.infra;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Configuration properties for the Loan Servicing domain-tier API.
 * <p>
 * Binds to {@code api-configuration.domain-platform.lending-loan-servicing} in application.yaml.
 */
@Component
@ConfigurationProperties(prefix = "api-configuration.domain-platform.lending-loan-servicing")
@Data
public class LoanServicingProperties {

    /** Base URL of the Loan Servicing service (e.g. {@code http://localhost:8084}). */
    private String basePath;

    /** Read/connect timeout for SDK calls. Defaults to 10 seconds. */
    private Duration timeout = Duration.ofSeconds(10);
}
