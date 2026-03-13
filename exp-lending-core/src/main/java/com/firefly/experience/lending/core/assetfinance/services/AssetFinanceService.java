package com.firefly.experience.lending.core.assetfinance.services;

import com.firefly.experience.lending.core.assetfinance.commands.ReportServiceEventCommand;
import com.firefly.experience.lending.core.assetfinance.commands.ReportUsageCommand;
import com.firefly.experience.lending.core.assetfinance.commands.RequestReturnCommand;
import com.firefly.experience.lending.core.assetfinance.queries.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Service for asset finance agreement operations, including agreement and asset queries,
 * delivery/return/pickup tracking, service events, usage reporting, and end-option exercise.
 */
public interface AssetFinanceService {

    Flux<AgreementSummaryDTO> listAgreements();

    Mono<AgreementDetailDTO> getAgreement(UUID agreementId);

    Flux<FinancedAssetDTO> listAssets(UUID agreementId);

    Mono<FinancedAssetDTO> getAsset(UUID agreementId, UUID assetId);

    Flux<DeliveryDTO> listDeliveries(UUID agreementId, UUID assetId);

    Mono<DeliveryDTO> getDelivery(UUID agreementId, UUID assetId, UUID deliveryId);

    Flux<ReturnDTO> listReturns(UUID agreementId, UUID assetId);

    Mono<ReturnDTO> requestReturn(UUID agreementId, UUID assetId, RequestReturnCommand command);

    Flux<PickupDTO> listPickups(UUID agreementId, UUID assetId);

    Flux<ServiceEventDTO> listServiceEvents(UUID agreementId, UUID assetId);

    Mono<ServiceEventDTO> reportServiceEvent(UUID agreementId, UUID assetId, ReportServiceEventCommand command);

    Flux<UsageRecordDTO> listUsage(UUID agreementId, UUID assetId);

    Mono<UsageRecordDTO> reportUsage(UUID agreementId, UUID assetId, ReportUsageCommand command);

    Flux<EndOptionDTO> listEndOptions(UUID agreementId);

    Mono<EndOptionDTO> exerciseEndOption(UUID agreementId, UUID optionId);
}
