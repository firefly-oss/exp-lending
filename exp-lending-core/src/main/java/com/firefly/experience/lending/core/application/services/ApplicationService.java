package com.firefly.experience.lending.core.application.services;

import com.firefly.domain.lending.loan.origination.sdk.model.ApplicationPartyDTO;
import com.firefly.experience.lending.core.application.commands.CreateApplicationCommand;
import com.firefly.experience.lending.core.application.commands.UpdateApplicationCommand;
import com.firefly.experience.lending.core.application.commands.UpdateEmploymentDataCommand;
import com.firefly.experience.lending.core.application.queries.ApplicationDetailDTO;
import com.firefly.experience.lending.core.application.queries.ApplicationStatusHistoryDTO;
import com.firefly.experience.lending.core.application.queries.ApplicationSummaryDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Service for managing loan applications throughout their lifecycle,
 * including creation, retrieval, updates, submission, withdrawal, and status history.
 */
public interface ApplicationService {

    Mono<ApplicationDetailDTO> createApplication(CreateApplicationCommand command);

    Flux<ApplicationSummaryDTO> listApplications();

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
