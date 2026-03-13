package com.firefly.experience.lending.infra;

import com.firefly.domain.product.catalog.sdk.api.ProductsApi;
import com.firefly.domain.product.catalog.sdk.invoker.ApiClient;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Factory that creates and configures the Product Catalog SDK {@link ApiClient}
 * and exposes domain API beans for dependency injection.
 */
@Component
public class ProductCatalogClientFactory {

    private final ApiClient apiClient;

    /**
     * Initialises the API client with the base path from configuration properties.
     *
     * @param properties connection properties for the Product Catalog service
     */
    public ProductCatalogClientFactory(ProductCatalogProperties properties) {
        this.apiClient = new ApiClient();
        this.apiClient.setBasePath(properties.getBasePath());
    }

    /**
     * Provides the {@link ProductsApi} bean for financial product catalogue queries.
     *
     * @return a ready-to-use ProductsApi instance
     */
    @Bean
    public ProductsApi productsApi() {
        return new ProductsApi(apiClient);
    }
}
