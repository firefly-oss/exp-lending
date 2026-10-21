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


package io.fireflysoftware.experience.lending.core.application.disbursement.services.impl;

import io.fireflysoftware.domain.lending.loan.origination.sdk.api.LoanOriginationApi;
import io.fireflysoftware.domain.lending.loan.origination.sdk.model.ApplicationExternalBankAccountDTO;
import io.fireflysoftware.domain.lending.loan.origination.sdk.model.LoanApplicationDTO;
import io.fireflysoftware.experience.lending.core.application.disbursement.commands.ConfigureDisbursementAccountCommand;
import io.fireflysoftware.experience.lending.core.application.disbursement.commands.RegisterExternalAccountCommand;
import io.fireflysoftware.experience.lending.core.application.disbursement.queries.DisbursementAccountDTO;
import io.fireflysoftware.experience.lending.core.application.disbursement.queries.ExternalAccountDTO;
import io.fireflysoftware.experience.lending.core.application.disbursement.services.DisbursementAccountService;
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
        var disbursement = new LoanApplicationDTO();
        if ("INTERNAL".equals(command.getAccountType())) {
            disbursement.setDisbursementMethodType(LoanApplicationDTO.DisbursementMethodTypeEnum.INTERNAL_ACCOUNT);
            disbursement.setDisbursementInternalAccountId(command.getAccountId());
            disbursement.setDisbursementExternalBankAccountId(null);
        } else if ("EXTERNAL".equals(command.getAccountType())) {
            disbursement.setDisbursementMethodType(LoanApplicationDTO.DisbursementMethodTypeEnum.EXTERNAL_ACCOUNT);
            disbursement.setDisbursementExternalBankAccountId(command.getAccountId());
            disbursement.setDisbursementInternalAccountId(null);
        } else {
            return Mono.error(new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Account type must be INTERNAL or EXTERNAL"));
        }
        return loanOriginationApi
                .configureDisbursementAccount(applicationId, disbursement, UUID.randomUUID().toString())
                .flatMap(app -> buildDisbursementAccountDTO(applicationId, app));
    }

    @Override
    public Mono<ExternalAccountDTO> registerExternalAccount(UUID applicationId,
                                                             RegisterExternalAccountCommand command) {
        log.debug("Registering external account for applicationId={}", applicationId);
        var body = new ApplicationExternalBankAccountDTO()
                .loanApplicationId(applicationId)
                .accountUsageType(ApplicationExternalBankAccountDTO.AccountUsageTypeEnum.DISBURSEMENT)
                .accountHolderName(command.getHolderName())
                .bankName(command.getBankName())
                .iban(command.getIban())
                .accountNumber(command.getIban());
        return loanOriginationApi
                .registerExternalBankAccount(applicationId, body, UUID.randomUUID().toString())
                .map(this::mapToExternalAccountDTO);
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
        return loanOriginationApi.deleteExternalBankAccount(applicationId, accountId, UUID.randomUUID().toString());
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
