package com.firefly.experience.lending.web.controllers;

import com.firefly.domain.core.pricing.engine.sdk.model.ProductPricingDTO;
import com.firefly.experience.lending.core.products.services.ProductCatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * REST controller exposing the lending product catalog with pricing data
 * sourced from the Pricing Engine domain service.
 */
@RestController
@RequestMapping("/api/v1/experience/lending/products")
@RequiredArgsConstructor
@Tag(name = "Lending - Products")
public class ProductCatalogController {

    private final ProductCatalogService productCatalogService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "listLendingProducts",
            summary = "List products with pricing",
            description = "Returns the lending products catalog with their pricing configuration, "
                    + "optionally filtered by product type.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Products returned successfully")
    })
    public Flux<ProductPricingDTO> listProducts(
            @RequestParam(value = "productType", required = false) String productType) {
        return productCatalogService.listProducts(productType);
    }

    @GetMapping(value = "/{productId}/pricing", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "getLendingProductPricing",
            summary = "Get product pricing",
            description = "Retrieves the pricing configuration for a specific lending product.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product pricing returned successfully"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public Mono<ResponseEntity<ProductPricingDTO>> getProductPricing(@PathVariable UUID productId) {
        return productCatalogService.getProductPricing(productId)
                .map(ResponseEntity::ok);
    }
}
