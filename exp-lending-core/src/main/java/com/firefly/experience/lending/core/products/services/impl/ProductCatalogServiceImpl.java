package com.firefly.experience.lending.core.products.services.impl;

import com.firefly.domain.core.pricing.engine.sdk.api.PricingEngineProductsApi;
import com.firefly.domain.core.pricing.engine.sdk.model.ProductPricingDTO;
import com.firefly.experience.lending.core.products.services.ProductCatalogService;
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
