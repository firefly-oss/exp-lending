package com.firefly.experience.lending.core.application.disbursement.services;

import com.firefly.experience.lending.core.application.disbursement.commands.ConfigureDisbursementAccountCommand;
import com.firefly.experience.lending.core.application.disbursement.commands.RegisterExternalAccountCommand;
import com.firefly.experience.lending.core.application.disbursement.queries.DisbursementAccountDTO;
import com.firefly.experience.lending.core.application.disbursement.queries.ExternalAccountDTO;
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
