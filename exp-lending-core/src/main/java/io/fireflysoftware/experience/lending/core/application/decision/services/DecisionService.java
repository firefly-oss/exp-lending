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


package io.fireflysoftware.experience.lending.core.application.decision.services;

import io.fireflysoftware.experience.lending.core.application.decision.commands.RejectOfferCommand;
import io.fireflysoftware.experience.lending.core.application.decision.commands.SignContractCommand;
import io.fireflysoftware.experience.lending.core.application.decision.queries.ContractDTO;
import io.fireflysoftware.experience.lending.core.application.decision.queries.DecisionDTO;
import io.fireflysoftware.experience.lending.core.application.decision.queries.OfferDetailDTO;
import io.fireflysoftware.experience.lending.core.application.decision.queries.OfferSummaryDTO;
import io.fireflysoftware.experience.lending.core.application.decision.queries.ScoringStatusDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Service for the underwriting decision flow: credit scoring status, underwriting decisions,
 * offer management, and contract signing via SCA.
 */
public interface DecisionService {

    Mono<ScoringStatusDTO> getScoringStatus(UUID applicationId);

    Mono<DecisionDTO> getDecision(UUID applicationId);

    Flux<OfferSummaryDTO> getOffers(UUID applicationId);

    Mono<OfferDetailDTO> getOffer(UUID applicationId, UUID offerId);

    Mono<Void> acceptOffer(UUID applicationId, UUID offerId);

    Mono<Void> rejectOffer(UUID applicationId, UUID offerId, RejectOfferCommand command);

    Mono<ContractDTO> getContract(UUID applicationId);

    Mono<ContractDTO> signContract(UUID applicationId, SignContractCommand command);
}
