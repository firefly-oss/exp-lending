package com.firefly.experience.lending.infra;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Configuration properties for the Loan Origination domain-tier API.
 * <p>
 * Binds to {@code api-configuration.domain-platform.lending-loan-origination} in application.yaml.
 */
@Component
@ConfigurationProperties(prefix = "api-configuration.domain-platform.lending-loan-origination")
@Data
public class LoanOriginationProperties {

    /** Base URL of the Loan Origination service (e.g. {@code http://localhost:8082}). */
    private String basePath;

    /** Read/connect timeout for SDK calls. Defaults to 10 seconds. */
    private Duration timeout = Duration.ofSeconds(10);
}
