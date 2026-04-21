package com.firefly.experience.lending.infra;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Configuration properties for the core-platform Loan Origination API.
 * <p>
 * Binds to {@code api-configuration.core-platform.lending-loan-origination} in application.yaml.
 */
@Component
@ConfigurationProperties(prefix = "api-configuration.core-platform.lending-loan-origination")
@Data
public class CoreLoanOriginationProperties {

    private String basePath;
    private Duration timeout = Duration.ofSeconds(10);
}
