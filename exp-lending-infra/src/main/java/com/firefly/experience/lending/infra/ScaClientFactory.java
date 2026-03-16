package com.firefly.experience.lending.infra;

import org.springframework.stereotype.Component;

/**
 * Factory placeholder for the Common SCA (Strong Customer Authentication) service.
 * <p>
 * The {@code domain-common-sca-sdk} artifact is currently being built; until its
 * OpenAPI generation is complete, ScaOperationsApi is not available.
 * This factory is retained as a placeholder for when the SCA SDK is ready.
 */
@Component
public class ScaClientFactory {

    /**
     * Initialises the factory with the base path from configuration properties.
     *
     * @param properties connection properties for the Common SCA service
     */
    public ScaClientFactory(ScaProperties properties) {
        // SCA client will be configured here once ScaOperationsApi is available
    }
}
