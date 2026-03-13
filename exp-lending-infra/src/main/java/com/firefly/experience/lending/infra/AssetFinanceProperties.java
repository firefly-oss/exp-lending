package com.firefly.experience.lending.infra;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Configuration properties for the Asset Finance domain-tier API.
 * <p>
 * Binds to {@code api-configuration.domain-platform.lending-asset-finance} in application.yaml.
 */
@Component
@ConfigurationProperties(prefix = "api-configuration.domain-platform.lending-asset-finance")
@Data
public class AssetFinanceProperties {

    /** Base URL of the Asset Finance service (e.g. {@code http://localhost:8043}). */
    private String basePath;

    /** Read/connect timeout for SDK calls. Defaults to 10 seconds. */
    private Duration timeout = Duration.ofSeconds(10);
}
