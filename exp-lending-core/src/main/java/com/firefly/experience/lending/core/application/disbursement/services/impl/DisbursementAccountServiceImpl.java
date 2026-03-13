package com.firefly.experience.lending.core.application.disbursement.services.impl;

import com.firefly.core.lending.origination.sdk.api.ApplicationExternalBankAccountsApi;
import com.firefly.core.lending.origination.sdk.api.LoanApplicationsApi;
import com.firefly.core.lending.origination.sdk.model.ApplicationExternalBankAccountDTO;
import com.firefly.core.lending.origination.sdk.model.LoanApplicationDTO;
import com.firefly.core.lending.origination.sdk.model.PaginationRequest;
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
 * Loan Origination SDK's {@code LoanApplicationsApi} and {@code ApplicationExternalBankAccountsApi}.
 *
 * // ARCH-EXCEPTION: No domain SDK exposes {@link LoanApplicationsApi} or
 * {@link ApplicationExternalBankAccountsApi}; direct core-lending-origination-sdk usage is
 * temporary until the domain layer surfaces these endpoints.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DisbursementAccountServiceImpl implements DisbursementAccountService {

    private final LoanApplicationsApi loanApplicationsApi;
    private final ApplicationExternalBankAccountsApi applicationExternalBankAccountsApi;

    @Override
    public Mono<DisbursementAccountDTO> getDisbursementAccount(UUID applicationId) {
        log.debug("Getting disbursement account for applicationId={}", applicationId);
        return loanApplicationsApi.getLoanApplication(applicationId, null)
                .flatMap(app -> buildDisbursementAccountDTO(applicationId, app));
    }

    @Override
    public Mono<DisbursementAccountDTO> configureDisbursementAccount(UUID applicationId,
                                                                      ConfigureDisbursementAccountCommand command) {
        log.debug("Configuring disbursement account for applicationId={} accountType={}",
                applicationId, command.getAccountType());
        return loanApplicationsApi.getLoanApplication(applicationId, null)
                .flatMap(existing -> {
                    boolean isExternal = "EXTERNAL".equalsIgnoreCase(command.getAccountType());
                    var updated = new LoanApplicationDTO(existing.getLoanApplicationId())
                            .disbursementMethodType(isExternal
                                    ? LoanApplicationDTO.DisbursementMethodTypeEnum.EXTERNAL_ACCOUNT
                                    : LoanApplicationDTO.DisbursementMethodTypeEnum.INTERNAL_ACCOUNT);
                    if (isExternal) {
                        updated.disbursementExternalBankAccountId(command.getAccountId());
                    } else {
                        updated.disbursementInternalAccountId(command.getAccountId());
                    }
                    return loanApplicationsApi.updateLoanApplication(applicationId, updated,
                            UUID.randomUUID().toString());
                })
                .flatMap(result -> buildDisbursementAccountDTO(applicationId, result));
    }

    @Override
    public Mono<ExternalAccountDTO> registerExternalAccount(UUID applicationId,
                                                             RegisterExternalAccountCommand command) {
        log.debug("Registering external account for applicationId={}", applicationId);
        var sdkDto = new ApplicationExternalBankAccountDTO()
                .loanApplicationId(applicationId)
                .accountHolderName(command.getHolderName())
                .bankName(command.getBankName())
                .iban(command.getIban())
                .accountUsageType(ApplicationExternalBankAccountDTO.AccountUsageTypeEnum.DISBURSEMENT)
                .accountNumber(command.getIban());
        return applicationExternalBankAccountsApi.create(applicationId, sdkDto,
                        UUID.randomUUID().toString())
                .map(this::mapToExternalAccountDTO);
    }

    @Override
    public Flux<ExternalAccountDTO> listExternalAccounts(UUID applicationId) {
        log.debug("Listing external accounts for applicationId={}", applicationId);
        return applicationExternalBankAccountsApi.findAll(applicationId, new PaginationRequest(), null)
                .flatMapIterable(page -> page.getContent() != null ? page.getContent() : List.of())
                .map(this::mapToExternalAccountDTO);
    }

    @Override
    public Mono<Void> deleteExternalAccount(UUID applicationId, UUID accountId) {
        log.debug("Deleting external account accountId={} for applicationId={}", accountId, applicationId);
        return applicationExternalBankAccountsApi.delete(applicationId, accountId,
                UUID.randomUUID().toString());
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
        // EXTERNAL_ACCOUNT — enrich with account details
        UUID extAccountId = app.getDisbursementExternalBankAccountId();
        if (extAccountId == null) {
            return Mono.empty();
        }
        return applicationExternalBankAccountsApi.get(applicationId, extAccountId, null)
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
