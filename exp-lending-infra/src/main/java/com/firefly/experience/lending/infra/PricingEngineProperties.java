package com.firefly.experience.lending.infra;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Configuration properties for the Pricing Engine domain-tier API.
 * <p>
 * Binds to {@code api-configuration.domain-platform.pricing-engine} in application.yaml.
 *
 * <p>No {@code @Component} annotation — the application class carries
 * {@code @ConfigurationPropertiesScan} to register all {@code @ConfigurationProperties} beans.
 */
@ConfigurationProperties(prefix = "api-configuration.domain-platform.pricing-engine")
@Data
public class PricingEngineProperties {

    /** Base URL of the Pricing Engine service (e.g. {@code http://localhost:8088}). */
    private String basePath;

    /** Read/connect timeout for SDK calls. Defaults to 5 seconds. */
    private Duration timeout = Duration.ofSeconds(5);
}
