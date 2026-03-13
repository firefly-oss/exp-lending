package com.firefly.experience.lending.core.collections.services;

import com.firefly.experience.lending.core.collections.commands.RegisterPaymentPromiseCommand;
import com.firefly.experience.lending.core.collections.queries.CollectionCaseDetailDTO;
import com.firefly.experience.lending.core.collections.queries.CollectionCaseSummaryDTO;
import com.firefly.experience.lending.core.collections.queries.PaymentPromiseDTO;
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
