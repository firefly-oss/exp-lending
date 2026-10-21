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


package io.fireflysoftware.experience.lending.core.adapter;

import io.fireflysoftware.experience.lending.core.product.BusinessParameterPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Calls domain-core-configuration. This class does not copy the sandbox parameter table.
 */
@Component
public class HttpBusinessParameterAdapter implements BusinessParameterPort {

    private static final ParameterizedTypeReference<Map<String, Object>> MAP = new ParameterizedTypeReference<>() {
    };

    private final WebClient webClient;

    @Autowired
    public HttpBusinessParameterAdapter(
            @Value("${api-configuration.domain-platform.configuration.base-path:http://localhost:8094}") String basePath) {
        this(WebClient.builder().baseUrl(basePath).build());
    }

    HttpBusinessParameterAdapter(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public Mono<String> findByKey(String key) {
        return webClient.get().uri("/api/v1/parameters/by-key/{key}", key)
                .retrieve()
                .bodyToMono(MAP)
                .map(body -> String.valueOf(body.get("value")));
    }
}
