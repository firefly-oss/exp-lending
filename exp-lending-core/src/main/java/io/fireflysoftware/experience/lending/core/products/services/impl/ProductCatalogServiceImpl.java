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


package io.fireflysoftware.experience.lending.core.products.services.impl;

import io.fireflysoftware.domain.core.pricing.engine.sdk.api.PricingEngineProductsApi;
import io.fireflysoftware.domain.core.pricing.engine.sdk.model.ProductPricingDTO;
import io.fireflysoftware.experience.lending.core.products.services.ProductCatalogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Default implementation of {@link ProductCatalogService}, delegating to the
 * Pricing Engine domain SDK.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductCatalogServiceImpl implements ProductCatalogService {

    private final PricingEngineProductsApi pricingEngineProductsApi;

    @Override
    public Flux<ProductPricingDTO> listProducts(String productType) {
        log.debug("Listing products with pricing, productType={}", productType);
        // SDK signature is Mono<ProductPricingDTO>, but the underlying response is a stream
        // of items. Use the response-spec to bind to a Flux directly so multi-item responses
        // are handled correctly.
        return pricingEngineProductsApi
                .listEngineProductsWithPricingWithResponseSpec(productType, null)
                .bodyToFlux(ProductPricingDTO.class);
    }

    @Override
    public Mono<ProductPricingDTO> getProductPricing(UUID productId) {
        log.debug("Getting product pricing for productId={}", productId);
        return pricingEngineProductsApi.getEngineProductPricing(productId, null);
    }
}
