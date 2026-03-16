package com.firefly.experience.lending.core.application.disbursement.services.impl;

import com.firefly.domain.lending.loan.origination.sdk.api.LoanOriginationApi;
import com.firefly.domain.lending.loan.origination.sdk.model.ApplicationExternalBankAccountDTO;
import com.firefly.domain.lending.loan.origination.sdk.model.LoanApplicationDTO;
import com.firefly.experience.lending.core.application.disbursement.commands.ConfigureDisbursementAccountCommand;
import com.firefly.experience.lending.core.application.disbursement.commands.RegisterExternalAccountCommand;
import com.firefly.experience.lending.core.application.disbursement.queries.DisbursementAccountDTO;
import com.firefly.experience.lending.core.application.disbursement.queries.ExternalAccountDTO;
import com.firefly.experience.lending.core.application.disbursement.services.DisbursementAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

/**
 * Default implementation of {@link DisbursementAccountService}, delegating to the
 * domain Loan Origination SDK's {@code LoanOriginationApi}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DisbursementAccountServiceImpl implements DisbursementAccountService {

    private final LoanOriginationApi loanOriginationApi;

    @Override
    public Mono<DisbursementAccountDTO> getDisbursementAccount(UUID applicationId) {
        log.debug("Getting disbursement account for applicationId={}", applicationId);
        return loanOriginationApi.getApplication(applicationId, null)
                .flatMap(app -> buildDisbursementAccountDTO(applicationId, app));
    }

    @Override
    public Mono<DisbursementAccountDTO> configureDisbursementAccount(UUID applicationId,
                                                                      ConfigureDisbursementAccountCommand command) {
        log.debug("Configuring disbursement account for applicationId={} accountType={}",
                applicationId, command.getAccountType());
        // MVP: the domain SDK does not expose an update-application endpoint.
        // Returns the current disbursement account unchanged. Replace when the domain layer
        // surfaces this endpoint.
        return getDisbursementAccount(applicationId);
    }

    @Override
    public Mono<ExternalAccountDTO> registerExternalAccount(UUID applicationId,
                                                             RegisterExternalAccountCommand command) {
        log.debug("Registering external account for applicationId={}", applicationId);
        // MVP: the domain SDK does not expose a create-external-bank-account endpoint.
        // Returns a stub DTO. Replace when the domain layer surfaces this endpoint.
        return Mono.just(ExternalAccountDTO.builder()
                .accountId(UUID.randomUUID())
                .iban(command.getIban())
                .bankName(command.getBankName())
                .holderName(command.getHolderName())
                .registeredAt(null)
                .build());
    }

    @Override
    public Flux<ExternalAccountDTO> listExternalAccounts(UUID applicationId) {
        log.debug("Listing external accounts for applicationId={}", applicationId);
        return loanOriginationApi.getApplicationBankAccounts(applicationId, null)
                .flatMapIterable(page -> page.getContent() != null ? page.getContent() : List.of())
                .map(this::mapToExternalAccountDTO);
    }

    @Override
    public Mono<Void> deleteExternalAccount(UUID applicationId, UUID accountId) {
        log.debug("Deleting external account accountId={} for applicationId={}", accountId, applicationId);
        // MVP: the domain SDK does not expose a delete-external-bank-account endpoint.
        // Operation completes as a no-op until the domain layer surfaces this endpoint.
        return Mono.empty();
    }

    private Mono<DisbursementAccountDTO> buildDisbursementAccountDTO(UUID applicationId,
                                                                       LoanApplicationDTO app) {
        var methodType = app.getDisbursementMethodType();
        if (methodType == null) {
            return Mono.empty();
        }
        if (methodType == LoanApplicationDTO.DisbursementMethodTypeEnum.INTERNAL_ACCOUNT) {
            return Mono.just(DisbursementAccountDTO.builder()
                    .accountId(app.getDisbursementInternalAccountId())
                    .accountType("INTERNAL")
                    .isDefault(true)
                    .build());
        }
        // EXTERNAL_ACCOUNT -- enrich with account details
        UUID extAccountId = app.getDisbursementExternalBankAccountId();
        if (extAccountId == null) {
            return Mono.empty();
        }
        return loanOriginationApi.getApplicationBankAccounts(applicationId, null)
                .flatMapIterable(page -> page.getContent() != null ? page.getContent() : List.of())
                .filter(ext -> extAccountId.equals(ext.getExternalBankAccountId()))
                .next()
                .map(ext -> DisbursementAccountDTO.builder()
                        .accountId(ext.getExternalBankAccountId())
                        .iban(ext.getIban())
                        .bankName(ext.getBankName())
                        .accountType("EXTERNAL")
                        .isDefault(true)
                        .build());
    }

    private ExternalAccountDTO mapToExternalAccountDTO(ApplicationExternalBankAccountDTO src) {
        return ExternalAccountDTO.builder()
                .accountId(src.getExternalBankAccountId())
                .iban(src.getIban())
                .bankName(src.getBankName())
                .holderName(src.getAccountHolderName())
                .registeredAt(src.getCreatedAt())
                .build();
    }
}
