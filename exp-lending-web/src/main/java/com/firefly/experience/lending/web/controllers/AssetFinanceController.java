package com.firefly.experience.lending.web.controllers;

import com.firefly.experience.lending.core.assetfinance.commands.ReportServiceEventCommand;
import com.firefly.experience.lending.core.assetfinance.commands.ReportUsageCommand;
import com.firefly.experience.lending.core.assetfinance.commands.RequestReturnCommand;
import com.firefly.experience.lending.core.assetfinance.queries.*;
import com.firefly.experience.lending.core.assetfinance.services.AssetFinanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * REST controller exposing asset finance endpoints: agreements, assets, deliveries, returns,
 * pickups, service events, usage records, and end-of-term options.
 */
@RestController
@RequestMapping("/api/v1/experience/lending/asset-finance")
@RequiredArgsConstructor
@Tag(name = "Lending - Asset Finance")
public class AssetFinanceController {

    private final AssetFinanceService assetFinanceService;

    // -------------------------------------------------------------------------
    // Agreements
    // -------------------------------------------------------------------------

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List Asset Finance Agreements",
               description = "Returns a list of asset finance agreements (leasing and renting).")
    public Flux<AgreementSummaryDTO> listAgreements() {
        return assetFinanceService.listAgreements();
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get Asset Finance Agreement",
               description = "Returns full details of a single asset finance agreement.")
    public Mono<ResponseEntity<AgreementDetailDTO>> getAgreement(@PathVariable UUID id) {
        return assetFinanceService.getAgreement(id)
                .map(ResponseEntity::ok);
    }

    // -------------------------------------------------------------------------
    // Assets
    // -------------------------------------------------------------------------

    @GetMapping(value = "/{id}/assets", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List Assets",
               description = "Returns all financed assets associated with the agreement.")
    public Flux<FinancedAssetDTO> listAssets(@PathVariable UUID id) {
        return assetFinanceService.listAssets(id);
    }

    @GetMapping(value = "/{id}/assets/{assetId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get Asset",
               description = "Returns details of a single financed asset.")
    public Mono<ResponseEntity<FinancedAssetDTO>> getAsset(
            @PathVariable UUID id,
            @PathVariable UUID assetId) {
        return assetFinanceService.getAsset(id, assetId)
                .map(ResponseEntity::ok);
    }

    // -------------------------------------------------------------------------
    // Deliveries
    // -------------------------------------------------------------------------

    @GetMapping(value = "/{id}/assets/{assetId}/deliveries", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List Deliveries",
               description = "Returns all delivery records for the given asset.")
    public Flux<DeliveryDTO> listDeliveries(
            @PathVariable UUID id,
            @PathVariable UUID assetId) {
        return assetFinanceService.listDeliveries(id, assetId);
    }

    @GetMapping(value = "/{id}/assets/{assetId}/deliveries/{dId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get Delivery",
               description = "Returns details of a single delivery record.")
    public Mono<ResponseEntity<DeliveryDTO>> getDelivery(
            @PathVariable UUID id,
            @PathVariable UUID assetId,
            @PathVariable UUID dId) {
        return assetFinanceService.getDelivery(id, assetId, dId)
                .map(ResponseEntity::ok);
    }

    // -------------------------------------------------------------------------
    // Returns
    // -------------------------------------------------------------------------

    @GetMapping(value = "/{id}/assets/{assetId}/returns", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List Returns",
               description = "Returns all return records for the given asset.")
    public Flux<ReturnDTO> listReturns(
            @PathVariable UUID id,
            @PathVariable UUID assetId) {
        return assetFinanceService.listReturns(id, assetId);
    }

    @PostMapping(value = "/{id}/assets/{assetId}/returns",
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Request Return",
               description = "Initiates a return process for the given asset. Returns the created return record.")
    public Mono<ResponseEntity<ReturnDTO>> requestReturn(
            @PathVariable UUID id,
            @PathVariable UUID assetId,
            @RequestBody RequestReturnCommand command) {
        return assetFinanceService.requestReturn(id, assetId, command)
                .map(result -> ResponseEntity.status(HttpStatus.CREATED).body(result));
    }

    // -------------------------------------------------------------------------
    // Pickups
    // -------------------------------------------------------------------------

    @GetMapping(value = "/{id}/assets/{assetId}/pickups", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List Pickups",
               description = "Returns all pickup records for the given asset.")
    public Flux<PickupDTO> listPickups(
            @PathVariable UUID id,
            @PathVariable UUID assetId) {
        return assetFinanceService.listPickups(id, assetId);
    }

    // -------------------------------------------------------------------------
    // Service Events
    // -------------------------------------------------------------------------

    @GetMapping(value = "/{id}/assets/{assetId}/service-events", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List Service Events",
               description = "Returns all service events (maintenance, damage, inspection) for the given asset.")
    public Flux<ServiceEventDTO> listServiceEvents(
            @PathVariable UUID id,
            @PathVariable UUID assetId) {
        return assetFinanceService.listServiceEvents(id, assetId);
    }

    @PostMapping(value = "/{id}/assets/{assetId}/service-events",
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Report Service Event",
               description = "Records a new service event (maintenance, damage, or inspection) for the given asset.")
    public Mono<ResponseEntity<ServiceEventDTO>> reportServiceEvent(
            @PathVariable UUID id,
            @PathVariable UUID assetId,
            @RequestBody ReportServiceEventCommand command) {
        return assetFinanceService.reportServiceEvent(id, assetId, command)
                .map(result -> ResponseEntity.status(HttpStatus.CREATED).body(result));
    }

    // -------------------------------------------------------------------------
    // Usage
    // -------------------------------------------------------------------------

    @GetMapping(value = "/{id}/assets/{assetId}/usage", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List Usage Records",
               description = "Returns all usage records (mileage snapshots) for the given asset.")
    public Flux<UsageRecordDTO> listUsage(
            @PathVariable UUID id,
            @PathVariable UUID assetId) {
        return assetFinanceService.listUsage(id, assetId);
    }

    @PostMapping(value = "/{id}/assets/{assetId}/usage",
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Report Usage",
               description = "Records a new usage snapshot (e.g. mileage reading) for the given asset.")
    public Mono<ResponseEntity<UsageRecordDTO>> reportUsage(
            @PathVariable UUID id,
            @PathVariable UUID assetId,
            @RequestBody ReportUsageCommand command) {
        return assetFinanceService.reportUsage(id, assetId, command)
                .map(result -> ResponseEntity.status(HttpStatus.CREATED).body(result));
    }

    // -------------------------------------------------------------------------
    // End Options
    // -------------------------------------------------------------------------

    @GetMapping(value = "/{id}/end-options", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List End Options",
               description = "Returns all end-of-term options (purchase, return, extend) for the agreement.")
    public Flux<EndOptionDTO> listEndOptions(@PathVariable UUID id) {
        return assetFinanceService.listEndOptions(id);
    }

    @PostMapping(value = "/{id}/end-options/{optId}/exercise", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Exercise End Option",
               description = "Exercises a lease-end option (e.g., purchase the asset). "
                   + "Marks the option as exercised in the domain service.")
    public Mono<ResponseEntity<EndOptionDTO>> exerciseEndOption(
            @PathVariable UUID id,
            @PathVariable UUID optId) {
        return assetFinanceService.exerciseEndOption(id, optId)
                .map(ResponseEntity::ok);
    }
}
