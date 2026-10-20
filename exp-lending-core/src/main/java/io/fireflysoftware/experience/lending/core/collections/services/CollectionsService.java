package io.fireflysoftware.experience.lending.core.collections.services;

import io.fireflysoftware.experience.lending.core.collections.commands.RegisterPaymentPromiseCommand;
import io.fireflysoftware.experience.lending.core.collections.queries.CollectionCaseDetailDTO;
import io.fireflysoftware.experience.lending.core.collections.queries.CollectionCaseSummaryDTO;
import io.fireflysoftware.experience.lending.core.collections.queries.PaymentPromiseDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Service for managing delinquency collection cases and payment promises
 * associated with overdue loan accounts.
 */
public interface CollectionsService {

    Flux<CollectionCaseSummaryDTO> listCollectionCases();

    Mono<CollectionCaseDetailDTO> getCollectionCase(UUID caseId);

    Mono<PaymentPromiseDTO> registerPaymentPromise(UUID caseId, RegisterPaymentPromiseCommand command);
}
