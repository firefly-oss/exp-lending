package com.firefly.experience.lending.infra;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Configuration properties for the Common Contracts domain-tier API.
 * <p>
 * Binds to {@code api-configuration.domain-platform.common-contracts} in application.yaml.
 */
@Component
@ConfigurationProperties(prefix = "api-configuration.domain-platform.common-contracts")
@Data
public class ContractsProperties {

    /** Base URL of the Common Contracts service (e.g. {@code http://localhost:8090}). */
    private String basePath;

    /** Read/connect timeout for SDK calls. Defaults to 10 seconds. */
    private Duration timeout = Duration.ofSeconds(10);
}
