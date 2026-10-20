package io.fireflysoftware.experience.lending.web.controllers;

import io.fireflysoftware.domain.core.pricing.engine.sdk.model.ProductPricingDTO;
import io.fireflysoftware.experience.lending.core.products.services.ProductCatalogService;
import org.fireflyframework.web.error.config.ErrorHandlingProperties;
import org.fireflyframework.web.error.converter.ExceptionConverterService;
import org.fireflyframework.web.error.service.ErrorResponseNegotiator;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.web.reactive.ReactiveWebSecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.ReactiveUserDetailsServiceAutoConfiguration;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = ProductCatalogController.class,
        excludeAutoConfiguration = {
                ReactiveWebSecurityAutoConfiguration.class,
                ReactiveUserDetailsServiceAutoConfiguration.class
        })
class ProductCatalogControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private ProductCatalogService productCatalogService;

    @MockitoBean
    private ExceptionConverterService exceptionConverterService;
    @MockitoBean
    private ErrorHandlingProperties errorHandlingProperties;
    @MockitoBean
    private ErrorResponseNegotiator errorResponseNegotiator;

    private static final String BASE_PATH = "/api/v1/experience/lending/products";

    @Test
    void listProducts_returns200WithFilteredResults() {
        var product = new ProductPricingDTO()
                .productId(UUID.randomUUID())
                .productCode("PL-STANDARD")
                .productType("PERSONAL_LOAN")
                .currency("EUR")
                .minAmount(new BigDecimal("1000"))
                .maxAmount(new BigDecimal("60000"));

        when(productCatalogService.listProducts(eq("PERSONAL_LOAN")))
                .thenReturn(Flux.just(product));

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path(BASE_PATH)
                        .queryParam("productType", "PERSONAL_LOAN")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ProductPricingDTO.class)
                .value(list -> {
                    assertThat(list).hasSize(1);
                    assertThat(list.get(0).getProductCode()).isEqualTo("PL-STANDARD");
                });

        ArgumentCaptor<String> typeCaptor = ArgumentCaptor.forClass(String.class);
        verify(productCatalogService).listProducts(typeCaptor.capture());
        assertThat(typeCaptor.getValue()).isEqualTo("PERSONAL_LOAN");
    }

    @Test
    void getProductPricing_returns200WithBody() {
        var productId = UUID.randomUUID();
        var product = new ProductPricingDTO()
                .productId(productId)
                .productCode("LEASING-AUTO")
                .productType("LEASING")
                .currency("EUR");

        when(productCatalogService.getProductPricing(eq(productId)))
                .thenReturn(Mono.just(product));

        webTestClient.get()
                .uri(BASE_PATH + "/{productId}/pricing", productId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ProductPricingDTO.class)
                .value(body -> {
                    assertThat(body.getProductId()).isEqualTo(productId);
                    assertThat(body.getProductType()).isEqualTo("LEASING");
                });
    }

    @Test
    void getProductPricing_propagatesUpstreamErrorAs5xx() {
        // The mocked GlobalExceptionHandler maps upstream errors to 5xx in this
        // test harness (matching the existing controller-test convention).
        var productId = UUID.randomUUID();

        when(productCatalogService.getProductPricing(any(UUID.class)))
                .thenReturn(Mono.error(new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "missing")));

        webTestClient.get()
                .uri(BASE_PATH + "/{productId}/pricing", productId)
                .exchange()
                .expectStatus().is5xxServerError();
    }
}
