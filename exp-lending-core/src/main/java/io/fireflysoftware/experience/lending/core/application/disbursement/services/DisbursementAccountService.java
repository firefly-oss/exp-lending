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


package io.fireflysoftware.experience.lending.core.application.disbursement.services;

import io.fireflysoftware.experience.lending.core.application.disbursement.commands.ConfigureDisbursementAccountCommand;
import io.fireflysoftware.experience.lending.core.application.disbursement.commands.RegisterExternalAccountCommand;
import io.fireflysoftware.experience.lending.core.application.disbursement.queries.DisbursementAccountDTO;
import io.fireflysoftware.experience.lending.core.application.disbursement.queries.ExternalAccountDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Service for managing disbursement account configuration and external bank accounts
 * linked to a loan application for payout purposes.
 */
public interface DisbursementAccountService {

    Mono<DisbursementAccountDTO> getDisbursementAccount(UUID applicationId);

    Mono<DisbursementAccountDTO> configureDisbursementAccount(UUID applicationId,
                                                               ConfigureDisbursementAccountCommand command);

    Mono<ExternalAccountDTO> registerExternalAccount(UUID applicationId,
                                                      RegisterExternalAccountCommand command);

    Flux<ExternalAccountDTO> listExternalAccounts(UUID applicationId);

    Mono<Void> deleteExternalAccount(UUID applicationId, UUID accountId);
}
