/*
 * Copyright 2025 Firefly Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package io.fireflysoftware.experience.lending.infra;

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
