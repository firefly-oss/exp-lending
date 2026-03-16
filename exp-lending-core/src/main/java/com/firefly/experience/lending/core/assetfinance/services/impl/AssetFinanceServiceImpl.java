package com.firefly.experience.lending.core.assetfinance.services.impl;

import com.firefly.domain.lending.assetfinance.sdk.api.AssetFinanceApi;
import com.firefly.domain.lending.assetfinance.sdk.model.AssetFinanceAgreementDTO;
import com.firefly.domain.lending.assetfinance.sdk.model.AssetFinanceAssetDTO;
import com.firefly.domain.lending.assetfinance.sdk.model.DeliveryRecordDTO;
import com.firefly.domain.lending.assetfinance.sdk.model.PickupRecordDTO;
import com.firefly.domain.lending.assetfinance.sdk.model.ReturnRecordDTO;
import com.firefly.experience.lending.core.assetfinance.commands.ReportServiceEventCommand;
import com.firefly.experience.lending.core.assetfinance.commands.ReportUsageCommand;
import com.firefly.experience.lending.core.assetfinance.commands.RequestReturnCommand;
import com.firefly.experience.lending.core.assetfinance.queries.AgreementDetailDTO;
import com.firefly.experience.lending.core.assetfinance.queries.AgreementSummaryDTO;
import com.firefly.experience.lending.core.assetfinance.queries.DeliveryDTO;
import com.firefly.experience.lending.core.assetfinance.queries.EndOptionDTO;
import com.firefly.experience.lending.core.assetfinance.queries.FinancedAssetDTO;
import com.firefly.experience.lending.core.assetfinance.queries.PickupDTO;
import com.firefly.experience.lending.core.assetfinance.queries.ReturnDTO;
import com.firefly.experience.lending.core.assetfinance.queries.ServiceEventDTO;
import com.firefly.experience.lending.core.assetfinance.queries.UsageRecordDTO;
import com.firefly.experience.lending.core.assetfinance.services.AssetFinanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Default implementation of {@link AssetFinanceService}, delegating to the Asset Finance SDK
 * for agreement, asset, delivery, return, pickup, service event, usage, and end-option operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AssetFinanceServiceImpl implements AssetFinanceService {

    private final AssetFinanceApi assetFinanceApi;

    // -------------------------------------------------------------------------
    // Agreements
    // -------------------------------------------------------------------------

    @Override
    public Flux<AgreementSummaryDTO> listAgreements() {
        // MVP: domain-lending-asset-finance-sdk does not expose a findAll agreements
        // endpoint. Replace when upstream adds a paginated agreements resource.
        log.debug("Listing asset finance agreements (MVP stub)");
        return Flux.empty();
    }

    @Override
    public Mono<AgreementDetailDTO> getAgreement(UUID agreementId) {
        log.debug("Getting asset finance agreement agreementId={}", agreementId);
        return assetFinanceApi.getAgreement(agreementId, UUID.randomUUID().toString())
                .map(this::toDetail);
    }

    // -------------------------------------------------------------------------
    // Assets
    // -------------------------------------------------------------------------

    @Override
    public Flux<FinancedAssetDTO> listAssets(UUID agreementId) {
        log.debug("Listing assets agreementId={}", agreementId);
        return assetFinanceApi.getAgreementAssets(agreementId, UUID.randomUUID().toString())
                .flatMapIterable(page -> page.getContent() != null ? page.getContent() : List.of())
                .map(this::toAsset);
    }

    @Override
    public Mono<FinancedAssetDTO> getAsset(UUID agreementId, UUID assetId) {
        log.debug("Getting asset agreementId={} assetId={}", agreementId, assetId);
        return assetFinanceApi.getAsset(agreementId, assetId, UUID.randomUUID().toString())
                .map(this::toAssetFromTyped);
    }

    // -------------------------------------------------------------------------
    // Deliveries
    // -------------------------------------------------------------------------

    @Override
    public Flux<DeliveryDTO> listDeliveries(UUID agreementId, UUID assetId) {
        log.debug("Listing deliveries agreementId={} assetId={}", agreementId, assetId);
        return assetFinanceApi.getAssetDeliveries(agreementId, assetId, UUID.randomUUID().toString())
                .flatMapIterable(page -> page.getContent() != null ? page.getContent() : List.of())
                .map(this::toDelivery);
    }

    @Override
    public Mono<DeliveryDTO> getDelivery(UUID agreementId, UUID assetId, UUID deliveryId) {
        log.debug("Getting delivery agreementId={} assetId={} deliveryId={}", agreementId, assetId, deliveryId);
        return assetFinanceApi.getAssetDeliveries(agreementId, assetId, UUID.randomUUID().toString())
                .flatMapIterable(page -> page.getContent() != null ? page.getContent() : List.of())
                .filter(item -> deliveryId.equals(extractUuid(toMap(item), "deliveryRecordId")))
                .next()
                .map(this::toDelivery)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Delivery not found: " + deliveryId)));
    }

    // -------------------------------------------------------------------------
    // Returns
    // -------------------------------------------------------------------------

    @Override
    public Flux<ReturnDTO> listReturns(UUID agreementId, UUID assetId) {
        log.debug("Listing returns agreementId={} assetId={}", agreementId, assetId);
        return assetFinanceApi.getAssetReturns(agreementId, assetId, UUID.randomUUID().toString())
                .flatMapIterable(page -> page.getContent() != null ? page.getContent() : List.of())
                .map(this::toReturn);
    }

    @Override
    public Mono<ReturnDTO> requestReturn(UUID agreementId, UUID assetId, RequestReturnCommand command) {
        log.debug("Requesting return agreementId={} assetId={}", agreementId, assetId);
        var dto = new ReturnRecordDTO()
                .assetFinanceAssetId(assetId)
                .conditionReport(command.getReason())
                .isFinalized(false);
        return assetFinanceApi.registerReturn(agreementId, assetId, dto, UUID.randomUUID().toString())
                .map(this::toReturnFromTyped);
    }

    // -------------------------------------------------------------------------
    // Pickups
    // -------------------------------------------------------------------------

    @Override
    public Flux<PickupDTO> listPickups(UUID agreementId, UUID assetId) {
        log.debug("Listing pickups agreementId={} assetId={}", agreementId, assetId);
        return assetFinanceApi.getAssetPickups(agreementId, assetId, UUID.randomUUID().toString())
                .flatMapIterable(page -> page.getContent() != null ? page.getContent() : List.of())
                .map(this::toPickup);
    }

    // -------------------------------------------------------------------------
    // Service Events
    // -------------------------------------------------------------------------

    @Override
    public Flux<ServiceEventDTO> listServiceEvents(UUID agreementId, UUID assetId) {
        log.debug("Listing service events agreementId={} assetId={}", agreementId, assetId);
        return assetFinanceApi.getAssetServiceEvents(agreementId, assetId, UUID.randomUUID().toString())
                .flatMapIterable(page -> page.getContent() != null ? page.getContent() : List.of())
                .map(this::toServiceEvent);
    }

    @Override
    public Mono<ServiceEventDTO> reportServiceEvent(UUID agreementId, UUID assetId,
                                                    ReportServiceEventCommand command) {
        log.debug("Reporting service event agreementId={} assetId={}", agreementId, assetId);
        var sdkDto = new com.firefly.domain.lending.assetfinance.sdk.model.ServiceEventDTO()
                .assetFinanceAssetId(assetId)
                .eventDate(LocalDate.now())
                .cost(command.getCost())
                .note(command.getDescription());
        if (command.getEventType() != null) {
            sdkDto.eventType(com.firefly.domain.lending.assetfinance.sdk.model.ServiceEventDTO.EventTypeEnum
                    .valueOf(command.getEventType()));
        }
        return assetFinanceApi.registerServiceEvent(agreementId, assetId, sdkDto, UUID.randomUUID().toString())
                .map(this::toServiceEventFromTyped);
    }

    // -------------------------------------------------------------------------
    // Usage
    // -------------------------------------------------------------------------

    @Override
    public Flux<UsageRecordDTO> listUsage(UUID agreementId, UUID assetId) {
        log.debug("Listing usage records agreementId={} assetId={}", agreementId, assetId);
        return assetFinanceApi.getAssetUsage(agreementId, assetId, UUID.randomUUID().toString())
                .flatMapIterable(page -> page.getContent() != null ? page.getContent() : List.of())
                .map(this::toUsageRecord);
    }

    @Override
    public Mono<UsageRecordDTO> reportUsage(UUID agreementId, UUID assetId, ReportUsageCommand command) {
        log.debug("Reporting usage agreementId={} assetId={}", agreementId, assetId);
        var sdkDto = new com.firefly.domain.lending.assetfinance.sdk.model.UsageRecordDTO()
                .assetFinanceAssetId(assetId)
                .usageDate(LocalDate.now())
                .usageDetail(command.getUsageDetail());
        if (command.getMileage() != null) {
            sdkDto.mileage(command.getMileage().intValue());
        }
        return assetFinanceApi.reportUsage(agreementId, assetId, sdkDto, UUID.randomUUID().toString())
                .map(this::toUsageRecordFromTyped);
    }

    // -------------------------------------------------------------------------
    // End Options
    // -------------------------------------------------------------------------

    @Override
    public Flux<EndOptionDTO> listEndOptions(UUID agreementId) {
        log.debug("Listing end options agreementId={}", agreementId);
        return assetFinanceApi.getAgreementEndOptions(agreementId, UUID.randomUUID().toString())
                .flatMapIterable(page -> page.getContent() != null ? page.getContent() : List.of())
                .map(this::toEndOption);
    }

    @Override
    public Mono<EndOptionDTO> exerciseEndOption(UUID agreementId, UUID optionId) {
        log.debug("Exercising end option agreementId={} optionId={}", agreementId, optionId);
        var sdkDto = new com.firefly.domain.lending.assetfinance.sdk.model.EndOptionDTO()
                .assetFinanceAgreementId(agreementId)
                .isExercised(true)
                .optionExerciseDate(LocalDate.now());
        return assetFinanceApi.updateEndOption(agreementId, optionId, sdkDto, UUID.randomUUID().toString())
                .map(this::toEndOptionFromTyped);
    }

    // -------------------------------------------------------------------------
    // Mappers — typed SDK DTOs
    // -------------------------------------------------------------------------

    private AgreementDetailDTO toDetail(AssetFinanceAgreementDTO dto) {
        return AgreementDetailDTO.builder()
                .agreementId(dto.getAssetFinanceAgreementId())
                .financeType(dto.getFinanceType() != null ? dto.getFinanceType().getValue() : null)
                .status(dto.getAgreementStatus() != null ? dto.getAgreementStatus().getValue() : null)
                .totalValue(dto.getTotalValue())
                .residualValue(dto.getResidualValue())
                .purchaseOptionPrice(dto.getPurchaseOptionPrice())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .build();
    }

    private FinancedAssetDTO toAssetFromTyped(AssetFinanceAssetDTO dto) {
        return FinancedAssetDTO.builder()
                .assetId(dto.getAssetFinanceAssetId())
                .description(dto.getAssetDescription())
                .serialNumber(dto.getAssetSerialNumber())
                .value(dto.getAssetValue())
                .isActive(Boolean.TRUE.equals(dto.getIsActive()))
                .build();
    }

    private ReturnDTO toReturnFromTyped(ReturnRecordDTO dto) {
        return ReturnDTO.builder()
                .returnId(dto.getReturnRecordId())
                .conditionReport(dto.getConditionReport())
                .damageCost(dto.getDamageCost())
                .isFinalized(Boolean.TRUE.equals(dto.getIsFinalized()))
                .build();
    }

    private ServiceEventDTO toServiceEventFromTyped(
            com.firefly.domain.lending.assetfinance.sdk.model.ServiceEventDTO dto) {
        return ServiceEventDTO.builder()
                .eventId(dto.getServiceEventId())
                .eventType(dto.getEventType() != null ? dto.getEventType().getValue() : null)
                .cost(dto.getCost())
                .description(dto.getNote())
                .eventDate(dto.getEventDate() != null ? dto.getEventDate().atStartOfDay() : null)
                .build();
    }

    private UsageRecordDTO toUsageRecordFromTyped(
            com.firefly.domain.lending.assetfinance.sdk.model.UsageRecordDTO dto) {
        return UsageRecordDTO.builder()
                .recordId(dto.getUsageRecordId())
                .mileage(dto.getMileage() != null ? dto.getMileage().longValue() : null)
                .usageDetail(dto.getUsageDetail())
                .reportedAt(dto.getCreatedAt())
                .build();
    }

    private EndOptionDTO toEndOptionFromTyped(
            com.firefly.domain.lending.assetfinance.sdk.model.EndOptionDTO dto) {
        return EndOptionDTO.builder()
                .optionId(dto.getEndOptionId())
                .type(dto.getNote())   // MVP: SDK has no type field; stored in note
                .paidAmount(dto.getOptionPaidAmount())
                .isExercised(Boolean.TRUE.equals(dto.getIsExercised()))
                .build();
    }

    // -------------------------------------------------------------------------
    // Mappers — untyped PaginationResponse items (Map extraction)
    // -------------------------------------------------------------------------

    private FinancedAssetDTO toAsset(Object item) {
        if (item instanceof AssetFinanceAssetDTO typed) {
            return toAssetFromTyped(typed);
        }
        Map<?, ?> m = toMap(item);
        return FinancedAssetDTO.builder()
                .assetId(extractUuid(m, "assetFinanceAssetId"))
                .description(extractString(m, "assetDescription"))
                .serialNumber(extractString(m, "assetSerialNumber"))
                .value(extractBigDecimal(m, "assetValue"))
                .isActive(Boolean.TRUE.equals(m.get("isActive")))
                .build();
    }

    private DeliveryDTO toDelivery(Object item) {
        if (item instanceof DeliveryRecordDTO typed) {
            return DeliveryDTO.builder()
                    .deliveryId(typed.getDeliveryRecordId())
                    .status(typed.getDeliveryStatus() != null ? typed.getDeliveryStatus().getValue() : null)
                    .trackingNumber(typed.getTrackingNumber())
                    .carrierName(typed.getCarrierName())
                    .deliveredAt(typed.getActualDeliveryDate() != null
                            ? typed.getActualDeliveryDate().atStartOfDay() : null)
                    .build();
        }
        Map<?, ?> m = toMap(item);
        return DeliveryDTO.builder()
                .deliveryId(extractUuid(m, "deliveryRecordId"))
                .status(extractString(m, "deliveryStatus"))
                .trackingNumber(extractString(m, "trackingNumber"))
                .carrierName(extractString(m, "carrierName"))
                .deliveredAt(toLocalDateTime(extractLocalDate(m, "actualDeliveryDate")))
                .build();
    }

    private ReturnDTO toReturn(Object item) {
        if (item instanceof ReturnRecordDTO typed) {
            return toReturnFromTyped(typed);
        }
        Map<?, ?> m = toMap(item);
        return ReturnDTO.builder()
                .returnId(extractUuid(m, "returnRecordId"))
                .conditionReport(extractString(m, "conditionReport"))
                .damageCost(extractBigDecimal(m, "damageCost"))
                .isFinalized(Boolean.TRUE.equals(m.get("isFinalized")))
                .build();
    }

    private PickupDTO toPickup(Object item) {
        if (item instanceof PickupRecordDTO typed) {
            return PickupDTO.builder()
                    .pickupId(typed.getPickupRecordId())
                    .status(typed.getPickupStatus() != null ? typed.getPickupStatus().getValue() : null)
                    .scheduledDate(typed.getScheduledPickupDate())
                    .collectorName(typed.getCollectorName())
                    .build();
        }
        Map<?, ?> m = toMap(item);
        return PickupDTO.builder()
                .pickupId(extractUuid(m, "pickupRecordId"))
                .status(extractString(m, "pickupStatus"))
                .scheduledDate(extractLocalDate(m, "scheduledPickupDate"))
                .collectorName(extractString(m, "collectorName"))
                .build();
    }

    private ServiceEventDTO toServiceEvent(Object item) {
        if (item instanceof com.firefly.domain.lending.assetfinance.sdk.model.ServiceEventDTO typed) {
            return toServiceEventFromTyped(typed);
        }
        Map<?, ?> m = toMap(item);
        return ServiceEventDTO.builder()
                .eventId(extractUuid(m, "serviceEventId"))
                .eventType(extractString(m, "eventType"))
                .cost(extractBigDecimal(m, "cost"))
                .description(extractString(m, "note"))
                .eventDate(toLocalDateTime(extractLocalDate(m, "eventDate")))
                .build();
    }

    private UsageRecordDTO toUsageRecord(Object item) {
        if (item instanceof com.firefly.domain.lending.assetfinance.sdk.model.UsageRecordDTO typed) {
            return toUsageRecordFromTyped(typed);
        }
        Map<?, ?> m = toMap(item);
        return UsageRecordDTO.builder()
                .recordId(extractUuid(m, "usageRecordId"))
                .mileage(extractLong(m, "mileage"))
                .usageDetail(extractString(m, "usageDetail"))
                .reportedAt(extractLocalDateTime(m, "createdAt"))
                .build();
    }

    private EndOptionDTO toEndOption(Object item) {
        if (item instanceof com.firefly.domain.lending.assetfinance.sdk.model.EndOptionDTO typed) {
            return toEndOptionFromTyped(typed);
        }
        Map<?, ?> m = toMap(item);
        return EndOptionDTO.builder()
                .optionId(extractUuid(m, "endOptionId"))
                .type(extractString(m, "note"))  // MVP: SDK has no type field; stored in note
                .paidAmount(extractBigDecimal(m, "optionPaidAmount"))
                .isExercised(Boolean.TRUE.equals(m.get("isExercised")))
                .build();
    }

    // -------------------------------------------------------------------------
    // Map extraction helpers
    // -------------------------------------------------------------------------

    private Map<?, ?> toMap(Object item) {
        return item instanceof Map<?, ?> m ? m : Map.of();
    }

    private UUID extractUuid(Map<?, ?> map, String key) {
        Object value = map.get(key);
        if (value instanceof String s) {
            try { return UUID.fromString(s); } catch (IllegalArgumentException e) { return null; }
        }
        if (value instanceof UUID u) return u;
        return null;
    }

    private BigDecimal extractBigDecimal(Map<?, ?> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        if (value instanceof String s) {
            try { return new BigDecimal(s); } catch (NumberFormatException e) { return null; }
        }
        return null;
    }

    private String extractString(Map<?, ?> map, String key) {
        Object value = map.get(key);
        return value instanceof String s ? s : (value != null ? value.toString() : null);
    }

    private Long extractLong(Map<?, ?> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number n) return n.longValue();
        if (value instanceof String s) {
            try { return Long.parseLong(s); } catch (NumberFormatException e) { return null; }
        }
        return null;
    }

    private LocalDate extractLocalDate(Map<?, ?> map, String key) {
        Object value = map.get(key);
        if (value instanceof String s && !s.isBlank()) {
            try { return LocalDate.parse(s); } catch (Exception e) { return null; }
        }
        return null;
    }

    private LocalDateTime extractLocalDateTime(Map<?, ?> map, String key) {
        Object value = map.get(key);
        if (value instanceof String s && !s.isBlank()) {
            try { return LocalDateTime.parse(s); } catch (Exception e) { return null; }
        }
        return null;
    }

    private LocalDateTime toLocalDateTime(LocalDate date) {
        return date != null ? date.atStartOfDay() : null;
    }
}
