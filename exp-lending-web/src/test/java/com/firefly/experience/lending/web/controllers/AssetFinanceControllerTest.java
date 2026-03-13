package com.firefly.experience.lending.web.controllers;

import com.firefly.experience.lending.core.assetfinance.commands.ReportServiceEventCommand;
import com.firefly.experience.lending.core.assetfinance.commands.ReportUsageCommand;
import com.firefly.experience.lending.core.assetfinance.commands.RequestReturnCommand;
import com.firefly.experience.lending.core.assetfinance.queries.*;
import com.firefly.experience.lending.core.assetfinance.services.AssetFinanceService;
import org.fireflyframework.web.error.config.ErrorHandlingProperties;
import org.fireflyframework.web.error.converter.ExceptionConverterService;
import org.fireflyframework.web.error.service.ErrorResponseNegotiator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = AssetFinanceController.class,
        excludeAutoConfiguration = {
                ReactiveSecurityAutoConfiguration.class,
                ReactiveUserDetailsServiceAutoConfiguration.class
        })
class AssetFinanceControllerTest {

    private static final String BASE = "/api/v1/experience/lending/asset-finance";

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private AssetFinanceService assetFinanceService;

    @MockBean
    private ExceptionConverterService exceptionConverterService;
    @MockBean
    private ErrorHandlingProperties errorHandlingProperties;
    @MockBean
    private ErrorResponseNegotiator errorResponseNegotiator;

    // -------------------------------------------------------------------------
    // Agreements
    // -------------------------------------------------------------------------

    @Test
    void listAgreements_returns200WithEmptyList() {
        when(assetFinanceService.listAgreements()).thenReturn(Flux.empty());

        webTestClient.get()
                .uri(BASE)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AgreementSummaryDTO.class)
                .hasSize(0);
    }

    @Test
    void listAgreements_returns200WithResults() {
        var agreementId = UUID.randomUUID();
        var summary = AgreementSummaryDTO.builder()
                .agreementId(agreementId)
                .financeType("LEASING")
                .status("ACTIVE")
                .totalValue(new BigDecimal("25000.00"))
                .build();

        when(assetFinanceService.listAgreements()).thenReturn(Flux.just(summary));

        webTestClient.get()
                .uri(BASE)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(AgreementSummaryDTO.class)
                .hasSize(1)
                .value(list -> assertThat(list.get(0).getAgreementId()).isEqualTo(agreementId));
    }

    @Test
    void getAgreement_returns200WithDetail() {
        var agreementId = UUID.randomUUID();
        var detail = AgreementDetailDTO.builder()
                .agreementId(agreementId)
                .financeType("LEASING")
                .status("ACTIVE")
                .totalValue(new BigDecimal("50000.00"))
                .residualValue(new BigDecimal("10000.00"))
                .purchaseOptionPrice(new BigDecimal("12000.00"))
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2028, 1, 1))
                .build();

        when(assetFinanceService.getAgreement(eq(agreementId))).thenReturn(Mono.just(detail));

        webTestClient.get()
                .uri(BASE + "/{id}", agreementId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(AgreementDetailDTO.class)
                .value(body -> {
                    assertThat(body.getAgreementId()).isEqualTo(agreementId);
                    assertThat(body.getFinanceType()).isEqualTo("LEASING");
                    assertThat(body.getResidualValue()).isEqualByComparingTo("10000.00");
                });
    }

    @Test
    void getAgreement_returns500WhenServiceFails() {
        var agreementId = UUID.randomUUID();
        when(assetFinanceService.getAgreement(eq(agreementId)))
                .thenReturn(Mono.error(new RuntimeException("upstream error")));

        webTestClient.get()
                .uri(BASE + "/{id}", agreementId)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    // -------------------------------------------------------------------------
    // Assets
    // -------------------------------------------------------------------------

    @Test
    void listAssets_returns200WithResults() {
        var agreementId = UUID.randomUUID();
        var assetId = UUID.randomUUID();
        var asset = FinancedAssetDTO.builder()
                .assetId(assetId)
                .description("BMW 3 Series")
                .serialNumber("WBA123456")
                .value(new BigDecimal("35000.00"))
                .isActive(true)
                .build();

        when(assetFinanceService.listAssets(eq(agreementId))).thenReturn(Flux.just(asset));

        webTestClient.get()
                .uri(BASE + "/{id}/assets", agreementId)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(FinancedAssetDTO.class)
                .hasSize(1)
                .value(list -> assertThat(list.get(0).getAssetId()).isEqualTo(assetId));
    }

    @Test
    void getAsset_returns200WithBody() {
        var agreementId = UUID.randomUUID();
        var assetId = UUID.randomUUID();
        var asset = FinancedAssetDTO.builder()
                .assetId(assetId)
                .description("BMW 3 Series")
                .serialNumber("WBA123456")
                .value(new BigDecimal("35000.00"))
                .isActive(true)
                .build();

        when(assetFinanceService.getAsset(eq(agreementId), eq(assetId))).thenReturn(Mono.just(asset));

        webTestClient.get()
                .uri(BASE + "/{id}/assets/{assetId}", agreementId, assetId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(FinancedAssetDTO.class)
                .value(body -> assertThat(body.getSerialNumber()).isEqualTo("WBA123456"));
    }

    // -------------------------------------------------------------------------
    // Deliveries
    // -------------------------------------------------------------------------

    @Test
    void listDeliveries_returns200() {
        var agreementId = UUID.randomUUID();
        var assetId = UUID.randomUUID();
        var delivery = DeliveryDTO.builder()
                .deliveryId(UUID.randomUUID())
                .status("DELIVERED")
                .trackingNumber("TRK001")
                .carrierName("DHL")
                .deliveredAt(LocalDateTime.of(2025, 3, 1, 10, 0))
                .build();

        when(assetFinanceService.listDeliveries(eq(agreementId), eq(assetId)))
                .thenReturn(Flux.just(delivery));

        webTestClient.get()
                .uri(BASE + "/{id}/assets/{assetId}/deliveries", agreementId, assetId)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(DeliveryDTO.class)
                .hasSize(1);
    }

    @Test
    void getDelivery_returns200() {
        var agreementId = UUID.randomUUID();
        var assetId = UUID.randomUUID();
        var deliveryId = UUID.randomUUID();
        var delivery = DeliveryDTO.builder()
                .deliveryId(deliveryId)
                .status("DELIVERED")
                .carrierName("DHL")
                .build();

        when(assetFinanceService.getDelivery(eq(agreementId), eq(assetId), eq(deliveryId)))
                .thenReturn(Mono.just(delivery));

        webTestClient.get()
                .uri(BASE + "/{id}/assets/{assetId}/deliveries/{dId}", agreementId, assetId, deliveryId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(DeliveryDTO.class)
                .value(body -> assertThat(body.getDeliveryId()).isEqualTo(deliveryId));
    }

    // -------------------------------------------------------------------------
    // Returns
    // -------------------------------------------------------------------------

    @Test
    void listReturns_returns200() {
        var agreementId = UUID.randomUUID();
        var assetId = UUID.randomUUID();
        when(assetFinanceService.listReturns(eq(agreementId), eq(assetId))).thenReturn(Flux.empty());

        webTestClient.get()
                .uri(BASE + "/{id}/assets/{assetId}/returns", agreementId, assetId)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ReturnDTO.class)
                .hasSize(0);
    }

    @Test
    void requestReturn_returns201WithBody() {
        var agreementId = UUID.randomUUID();
        var assetId = UUID.randomUUID();
        var returnId = UUID.randomUUID();
        var returnDto = ReturnDTO.builder()
                .returnId(returnId)
                .conditionReport("Scratches on rear bumper")
                .damageCost(new BigDecimal("500.00"))
                .isFinalized(false)
                .build();

        when(assetFinanceService.requestReturn(eq(agreementId), eq(assetId), any(RequestReturnCommand.class)))
                .thenReturn(Mono.just(returnDto));

        webTestClient.post()
                .uri(BASE + "/{id}/assets/{assetId}/returns", agreementId, assetId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                            "assetId": "%s",
                            "reason": "Scratches on rear bumper"
                        }
                        """.formatted(assetId))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ReturnDTO.class)
                .value(body -> {
                    assertThat(body.getReturnId()).isEqualTo(returnId);
                    assertThat(body.getConditionReport()).isEqualTo("Scratches on rear bumper");
                });
    }

    // -------------------------------------------------------------------------
    // Pickups
    // -------------------------------------------------------------------------

    @Test
    void listPickups_returns200() {
        var agreementId = UUID.randomUUID();
        var assetId = UUID.randomUUID();
        var pickup = PickupDTO.builder()
                .pickupId(UUID.randomUUID())
                .status("SCHEDULED")
                .scheduledDate(LocalDate.of(2028, 2, 1))
                .collectorName("John Doe")
                .build();

        when(assetFinanceService.listPickups(eq(agreementId), eq(assetId)))
                .thenReturn(Flux.just(pickup));

        webTestClient.get()
                .uri(BASE + "/{id}/assets/{assetId}/pickups", agreementId, assetId)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(PickupDTO.class)
                .hasSize(1);
    }

    // -------------------------------------------------------------------------
    // Service Events
    // -------------------------------------------------------------------------

    @Test
    void listServiceEvents_returns200() {
        var agreementId = UUID.randomUUID();
        var assetId = UUID.randomUUID();
        when(assetFinanceService.listServiceEvents(eq(agreementId), eq(assetId)))
                .thenReturn(Flux.empty());

        webTestClient.get()
                .uri(BASE + "/{id}/assets/{assetId}/service-events", agreementId, assetId)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ServiceEventDTO.class)
                .hasSize(0);
    }

    @Test
    void reportServiceEvent_returns201WithBody() {
        var agreementId = UUID.randomUUID();
        var assetId = UUID.randomUUID();
        var eventId = UUID.randomUUID();
        var event = ServiceEventDTO.builder()
                .eventId(eventId)
                .eventType("MAINTENANCE")
                .cost(new BigDecimal("350.00"))
                .description("Oil change and tyre rotation")
                .eventDate(LocalDateTime.of(2026, 3, 10, 9, 0))
                .build();

        when(assetFinanceService.reportServiceEvent(eq(agreementId), eq(assetId),
                any(ReportServiceEventCommand.class)))
                .thenReturn(Mono.just(event));

        webTestClient.post()
                .uri(BASE + "/{id}/assets/{assetId}/service-events", agreementId, assetId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                            "eventType": "MAINTENANCE",
                            "description": "Oil change and tyre rotation",
                            "cost": 350.00
                        }
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ServiceEventDTO.class)
                .value(body -> {
                    assertThat(body.getEventId()).isEqualTo(eventId);
                    assertThat(body.getEventType()).isEqualTo("MAINTENANCE");
                    assertThat(body.getCost()).isEqualByComparingTo("350.00");
                });
    }

    // -------------------------------------------------------------------------
    // Usage
    // -------------------------------------------------------------------------

    @Test
    void listUsage_returns200() {
        var agreementId = UUID.randomUUID();
        var assetId = UUID.randomUUID();
        when(assetFinanceService.listUsage(eq(agreementId), eq(assetId))).thenReturn(Flux.empty());

        webTestClient.get()
                .uri(BASE + "/{id}/assets/{assetId}/usage", agreementId, assetId)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(UsageRecordDTO.class)
                .hasSize(0);
    }

    @Test
    void reportUsage_returns201WithBody() {
        var agreementId = UUID.randomUUID();
        var assetId = UUID.randomUUID();
        var recordId = UUID.randomUUID();
        var record = UsageRecordDTO.builder()
                .recordId(recordId)
                .mileage(15000L)
                .usageDetail("Monthly mileage report")
                .reportedAt(LocalDateTime.now())
                .build();

        when(assetFinanceService.reportUsage(eq(agreementId), eq(assetId), any(ReportUsageCommand.class)))
                .thenReturn(Mono.just(record));

        webTestClient.post()
                .uri(BASE + "/{id}/assets/{assetId}/usage", agreementId, assetId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                            "mileage": 15000,
                            "usageDetail": "Monthly mileage report"
                        }
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(UsageRecordDTO.class)
                .value(body -> {
                    assertThat(body.getRecordId()).isEqualTo(recordId);
                    assertThat(body.getMileage()).isEqualTo(15000L);
                });
    }

    // -------------------------------------------------------------------------
    // End Options
    // -------------------------------------------------------------------------

    @Test
    void listEndOptions_returns200() {
        var agreementId = UUID.randomUUID();
        var option = EndOptionDTO.builder()
                .optionId(UUID.randomUUID())
                .type("PURCHASE")
                .paidAmount(new BigDecimal("12000.00"))
                .isExercised(false)
                .build();

        when(assetFinanceService.listEndOptions(eq(agreementId))).thenReturn(Flux.just(option));

        webTestClient.get()
                .uri(BASE + "/{id}/end-options", agreementId)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(EndOptionDTO.class)
                .hasSize(1);
    }

    @Test
    void exerciseEndOption_returns200WithBody() {
        var agreementId = UUID.randomUUID();
        var optId = UUID.randomUUID();
        var exercised = EndOptionDTO.builder()
                .optionId(optId)
                .type("PURCHASE")
                .paidAmount(new BigDecimal("12000.00"))
                .isExercised(true)
                .build();

        when(assetFinanceService.exerciseEndOption(eq(agreementId), eq(optId)))
                .thenReturn(Mono.just(exercised));

        webTestClient.post()
                .uri(BASE + "/{id}/end-options/{optId}/exercise", agreementId, optId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(EndOptionDTO.class)
                .value(body -> {
                    assertThat(body.getOptionId()).isEqualTo(optId);
                    assertThat(body.isExercised()).isTrue();
                });
    }

    @Test
    void exerciseEndOption_returns500WhenServiceFails() {
        var agreementId = UUID.randomUUID();
        var optId = UUID.randomUUID();
        when(assetFinanceService.exerciseEndOption(eq(agreementId), eq(optId)))
                .thenReturn(Mono.error(new RuntimeException("upstream error")));

        webTestClient.post()
                .uri(BASE + "/{id}/end-options/{optId}/exercise", agreementId, optId)
                .exchange()
                .expectStatus().is5xxServerError();
    }
}
