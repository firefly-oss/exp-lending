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
