package com.firefly.experience.lending.core.products.services;

import com.firefly.domain.core.pricing.engine.sdk.model.ProductPricingDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Service for retrieving lending products and their pricing configuration
 * from the Pricing Engine domain service.
 */
public interface ProductCatalogService {

    /**
     * Lists products with pricing, optionally filtered by product type.
     *
     * @param productType optional filter (e.g. {@code PERSONAL_LOAN}, {@code LEASING})
     * @return a {@link Flux} of {@link ProductPricingDTO}
     */
    Flux<ProductPricingDTO> listProducts(String productType);

    /**
     * Retrieves the pricing configuration for a single product.
     *
     * @param productId the product identifier
     * @return a {@link Mono} emitting the {@link ProductPricingDTO}
     */
    Mono<ProductPricingDTO> getProductPricing(UUID productId);
}
