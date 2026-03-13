package com.firefly.experience.lending.infra;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Configuration properties for the Product Pricing domain-tier API.
 * <p>
 * Binds to {@code api-configuration.domain-platform.product-pricing} in application.yaml.
 */
@Component
@ConfigurationProperties(prefix = "api-configuration.domain-platform.product-pricing")
@Data
public class ProductPricingProperties {

    /** Base URL of the Product Pricing service (e.g. {@code http://localhost:8075}). */
    private String basePath;

    /** Read/connect timeout for SDK calls. Defaults to 5 seconds. */
    private Duration timeout = Duration.ofSeconds(5);
}
