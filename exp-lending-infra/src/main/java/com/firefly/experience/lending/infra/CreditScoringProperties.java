package com.firefly.experience.lending.infra;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Configuration properties for the Credit Scoring domain-tier API.
 * <p>
 * Binds to {@code api-configuration.domain-platform.lending-credit-scoring} in application.yaml.
 */
@Component
@ConfigurationProperties(prefix = "api-configuration.domain-platform.lending-credit-scoring")
@Data
public class CreditScoringProperties {

    /** Base URL of the Credit Scoring service (e.g. {@code http://localhost:8042}). */
    private String basePath;

    /** Read/connect timeout for SDK calls. Defaults to 15 seconds. */
    private Duration timeout = Duration.ofSeconds(15);
}
