package com.firefly.experience.lending.infra;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Configuration properties for the Personal Loans domain-tier API.
 * <p>
 * Binds to {@code api-configuration.domain-platform.lending-personal-loans} in application.yaml.
 */
@Component
@ConfigurationProperties(prefix = "api-configuration.domain-platform.lending-personal-loans")
@Data
public class PersonalLoansProperties {

    /** Base URL of the Personal Loans service (e.g. {@code http://localhost:8044}). */
    private String basePath;

    /** Read/connect timeout for SDK calls. Defaults to 10 seconds. */
    private Duration timeout = Duration.ofSeconds(10);
}
