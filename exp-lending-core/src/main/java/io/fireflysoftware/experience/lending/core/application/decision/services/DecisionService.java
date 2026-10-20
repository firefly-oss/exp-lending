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
