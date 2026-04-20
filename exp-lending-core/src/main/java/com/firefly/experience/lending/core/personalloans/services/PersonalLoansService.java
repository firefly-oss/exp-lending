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
