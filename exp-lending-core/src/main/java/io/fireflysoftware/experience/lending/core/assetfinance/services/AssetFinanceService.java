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


package io.fireflysoftware.experience.lending.core.assetfinance.services;

import io.fireflysoftware.experience.lending.core.assetfinance.commands.ReportServiceEventCommand;
import io.fireflysoftware.experience.lending.core.assetfinance.commands.ReportUsageCommand;
import io.fireflysoftware.experience.lending.core.assetfinance.commands.RequestReturnCommand;
import io.fireflysoftware.experience.lending.core.assetfinance.queries.*;
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
