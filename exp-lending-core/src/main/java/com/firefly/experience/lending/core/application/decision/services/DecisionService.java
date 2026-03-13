package com.firefly.experience.lending.core.application.decision.services;

import com.firefly.experience.lending.core.application.decision.commands.RejectOfferCommand;
import com.firefly.experience.lending.core.application.decision.commands.SignContractCommand;
import com.firefly.experience.lending.core.application.decision.queries.ContractDTO;
import com.firefly.experience.lending.core.application.decision.queries.DecisionDTO;
import com.firefly.experience.lending.core.application.decision.queries.OfferDetailDTO;
import com.firefly.experience.lending.core.application.decision.queries.OfferSummaryDTO;
import com.firefly.experience.lending.core.application.decision.queries.ScoringStatusDTO;
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
