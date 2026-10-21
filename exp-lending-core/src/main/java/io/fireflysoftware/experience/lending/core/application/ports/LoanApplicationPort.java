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


package io.fireflysoftware.experience.lending.core.application.ports;

import io.fireflysoftware.domain.lending.loan.origination.sdk.model.ApplicationPartyDTO;
import io.fireflysoftware.experience.lending.core.application.commands.CreateApplicationCommand;
import io.fireflysoftware.experience.lending.core.application.commands.UpdateApplicationCommand;
import io.fireflysoftware.experience.lending.core.application.commands.UpdateEmploymentDataCommand;
import io.fireflysoftware.experience.lending.core.application.queries.ApplicationDetailDTO;
import io.fireflysoftware.experience.lending.core.application.queries.ApplicationStatusHistoryDTO;
import io.fireflysoftware.experience.lending.core.application.queries.ApplicationSummaryDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Service for managing loan applications throughout their lifecycle,
 * including creation, retrieval, updates, submission, withdrawal, and status history.
 */
public interface LoanApplicationPort {

    Mono<ApplicationDetailDTO> createApplication(CreateApplicationCommand command);

    Mono<ApplicationDetailDTO> getApplication(UUID applicationId);

    Mono<ApplicationDetailDTO> updateApplication(UUID applicationId, UpdateApplicationCommand command);

    Mono<Void> submitApplication(UUID applicationId);

    Mono<Void> withdrawApplication(UUID applicationId);

    Mono<ApplicationStatusHistoryDTO> getStatusHistory(UUID applicationId);

    /**
     * Updates the applicant's employment and economic profile attached to a loan application.
     *
     * @param applicationId the application identifier
     * @param command       the validated employment data update
     * @return a {@link Mono} emitting the updated {@link ApplicationPartyDTO}
     */
    Mono<ApplicationPartyDTO> updateEmploymentData(UUID applicationId,
                                                   UpdateEmploymentDataCommand command);
}
