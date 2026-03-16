package com.firefly.experience.lending.core.application.disbursement.services;

import com.firefly.domain.lending.loan.origination.sdk.api.LoanOriginationApi;
import com.firefly.domain.lending.loan.origination.sdk.model.ApplicationExternalBankAccountDTO;
import com.firefly.domain.lending.loan.origination.sdk.model.LoanApplicationDTO;
import com.firefly.domain.lending.loan.origination.sdk.model.PaginationResponseApplicationExternalBankAccountDTO;
import com.firefly.experience.lending.core.application.disbursement.commands.ConfigureDisbursementAccountCommand;
import com.firefly.experience.lending.core.application.disbursement.commands.RegisterExternalAccountCommand;
import com.firefly.experience.lending.core.application.disbursement.services.impl.DisbursementAccountServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DisbursementAccountServiceImplTest {

    @Mock
    private LoanOriginationApi loanOriginationApi;

    @InjectMocks
    private DisbursementAccountServiceImpl service;

    private static final UUID APPLICATION_ID = UUID.randomUUID();
    private static final UUID INTERNAL_ACCOUNT_ID = UUID.randomUUID();
    private static final UUID EXTERNAL_ACCOUNT_ID = UUID.randomUUID();

    // --- getDisbursementAccount ---

    @Test
    void getDisbursementAccount_returnsInternalAccount() {
        var app = new LoanApplicationDTO()
                .loanApplicationId(APPLICATION_ID)
                .disbursementMethodType(LoanApplicationDTO.DisbursementMethodTypeEnum.INTERNAL_ACCOUNT)
                .disbursementInternalAccountId(INTERNAL_ACCOUNT_ID);

        when(loanOriginationApi.getApplication(eq(APPLICATION_ID), isNull()))
                .thenReturn(Mono.just(app));

        StepVerifier.create(service.getDisbursementAccount(APPLICATION_ID))
                .assertNext(dto -> {
                    assertThat(dto.getAccountId()).isEqualTo(INTERNAL_ACCOUNT_ID);
                    assertThat(dto.getAccountType()).isEqualTo("INTERNAL");
                    assertThat(dto.isDefault()).isTrue();
                    assertThat(dto.getIban()).isNull();
                })
                .verifyComplete();
    }

    @Test
    void getDisbursementAccount_returnsExternalAccountWithEnrichedDetails() {
        var app = new LoanApplicationDTO()
                .loanApplicationId(APPLICATION_ID)
                .disbursementMethodType(LoanApplicationDTO.DisbursementMethodTypeEnum.EXTERNAL_ACCOUNT)
                .disbursementExternalBankAccountId(EXTERNAL_ACCOUNT_ID);

        var extAccount = new ApplicationExternalBankAccountDTO()
                .externalBankAccountId(EXTERNAL_ACCOUNT_ID)
                .iban("ES1234567890")
                .bankName("Firefly Bank");

        when(loanOriginationApi.getApplication(eq(APPLICATION_ID), isNull()))
                .thenReturn(Mono.just(app));
        when(loanOriginationApi.getApplicationBankAccounts(eq(APPLICATION_ID), isNull()))
                .thenReturn(Mono.just(new PaginationResponseApplicationExternalBankAccountDTO()
                        .content(List.of(extAccount))));

        StepVerifier.create(service.getDisbursementAccount(APPLICATION_ID))
                .assertNext(dto -> {
                    assertThat(dto.getAccountId()).isEqualTo(EXTERNAL_ACCOUNT_ID);
                    assertThat(dto.getAccountType()).isEqualTo("EXTERNAL");
                    assertThat(dto.getIban()).isEqualTo("ES1234567890");
                    assertThat(dto.getBankName()).isEqualTo("Firefly Bank");
                    assertThat(dto.isDefault()).isTrue();
                })
                .verifyComplete();
    }

    @Test
    void getDisbursementAccount_returnsEmptyWhenNoDisbursementConfigured() {
        var app = new LoanApplicationDTO().loanApplicationId(APPLICATION_ID);

        when(loanOriginationApi.getApplication(eq(APPLICATION_ID), isNull()))
                .thenReturn(Mono.just(app));

        StepVerifier.create(service.getDisbursementAccount(APPLICATION_ID))
                .verifyComplete();
    }

    @Test
    void getDisbursementAccount_propagatesUpstreamError() {
        when(loanOriginationApi.getApplication(eq(APPLICATION_ID), isNull()))
                .thenReturn(Mono.error(new RuntimeException("upstream error")));

        StepVerifier.create(service.getDisbursementAccount(APPLICATION_ID))
                .expectError(RuntimeException.class)
                .verify();
    }

    // --- registerExternalAccount ---

    @Test
    void registerExternalAccount_returnsStubDTO() {
        var command = new RegisterExternalAccountCommand();
        command.setIban("ES1234");
        command.setBankName("My Bank");
        command.setHolderName("John Doe");

        StepVerifier.create(service.registerExternalAccount(APPLICATION_ID, command))
                .assertNext(dto -> {
                    assertThat(dto.getAccountId()).isNotNull();
                    assertThat(dto.getIban()).isEqualTo("ES1234");
                    assertThat(dto.getBankName()).isEqualTo("My Bank");
                    assertThat(dto.getHolderName()).isEqualTo("John Doe");
                })
                .verifyComplete();
    }

    // --- listExternalAccounts ---

    @Test
    void listExternalAccounts_returnsMappedAccounts() {
        var acc = new ApplicationExternalBankAccountDTO()
                .externalBankAccountId(EXTERNAL_ACCOUNT_ID)
                .iban("ES5555")
                .bankName("List Bank")
                .accountHolderName("Alice")
                .createdAt(LocalDateTime.now());

        var page = new PaginationResponseApplicationExternalBankAccountDTO()
                .content(List.of(acc));

        when(loanOriginationApi.getApplicationBankAccounts(eq(APPLICATION_ID), isNull()))
                .thenReturn(Mono.just(page));

        StepVerifier.create(service.listExternalAccounts(APPLICATION_ID))
                .assertNext(dto -> {
                    assertThat(dto.getAccountId()).isEqualTo(EXTERNAL_ACCOUNT_ID);
                    assertThat(dto.getIban()).isEqualTo("ES5555");
                    assertThat(dto.getHolderName()).isEqualTo("Alice");
                })
                .verifyComplete();
    }

    @Test
    void listExternalAccounts_returnsEmptyWhenPageContentIsNull() {
        var page = new PaginationResponseApplicationExternalBankAccountDTO().content(null);

        when(loanOriginationApi.getApplicationBankAccounts(eq(APPLICATION_ID), isNull()))
                .thenReturn(Mono.just(page));

        StepVerifier.create(service.listExternalAccounts(APPLICATION_ID))
                .verifyComplete();
    }

    // --- deleteExternalAccount ---

    @Test
    void deleteExternalAccount_completesSuccessfully() {
        StepVerifier.create(service.deleteExternalAccount(APPLICATION_ID, EXTERNAL_ACCOUNT_ID))
                .verifyComplete();
    }
}
