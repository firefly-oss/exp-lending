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

package com.firefly.experience.lending.core.personalloans.services;

import com.firefly.experience.lending.core.personalloans.commands.CreatePersonalLoanCommand;
import com.firefly.experience.lending.core.personalloans.queries.PersonalLoanDetailDTO;
import com.firefly.experience.lending.core.personalloans.queries.PersonalLoanSummaryDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Service for personal loan agreement operations, including creation, retrieval,
 * listing, and update of agreements.
 */
public interface PersonalLoansService {

    Mono<PersonalLoanDetailDTO> createAgreement(CreatePersonalLoanCommand command);

    Mono<PersonalLoanDetailDTO> getAgreement(UUID agreementId);

    Flux<PersonalLoanSummaryDTO> listAgreements();

    Mono<PersonalLoanDetailDTO> updateAgreement(UUID agreementId, CreatePersonalLoanCommand command);
}
