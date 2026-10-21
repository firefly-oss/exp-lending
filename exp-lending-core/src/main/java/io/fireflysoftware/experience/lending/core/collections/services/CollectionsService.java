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
