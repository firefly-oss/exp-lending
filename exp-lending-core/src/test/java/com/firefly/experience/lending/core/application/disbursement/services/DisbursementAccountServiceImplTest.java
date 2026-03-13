package com.firefly.experience.lending.core.application.disbursement.services;

import com.firefly.core.lending.origination.sdk.api.ApplicationExternalBankAccountsApi;
import com.firefly.core.lending.origination.sdk.api.LoanApplicationsApi;
import com.firefly.core.lending.origination.sdk.model.ApplicationExternalBankAccountDTO;
import com.firefly.core.lending.origination.sdk.model.LoanApplicationDTO;
import com.firefly.core.lending.origination.sdk.model.PaginationResponseApplicationExternalBankAccountDTO;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DisbursementAccountServiceImplTest {

    @Mock
    private LoanApplicationsApi loanApplicationsApi;

    @Mock
    private ApplicationExternalBankAccountsApi applicationExternalBankAccountsApi;

    @InjectMocks
    private DisbursementAccountServiceImpl service;

    private static final UUID APPLICATION_ID = UUID.randomUUID();
    private static final UUID INTERNAL_ACCOUNT_ID = UUID.randomUUID();
    private static final UUID EXTERNAL_ACCOUNT_ID = UUID.randomUUID();

    // --- getDisbursementAccount ---

    @Test
    void getDisbursementAccount_returnsInternalAccount() {
        var app = new LoanApplicationDTO(APPLICATION_ID)
                .disbursementMethodType(LoanApplicationDTO.DisbursementMethodTypeEnum.INTERNAL_ACCOUNT)
                .disbursementInternalAccountId(INTERNAL_ACCOUNT_ID);

        when(loanApplicationsApi.getLoanApplication(eq(APPLICATION_ID), isNull()))
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
        var app = new LoanApplicationDTO(APPLICATION_ID)
                .disbursementMethodType(LoanApplicationDTO.DisbursementMethodTypeEnum.EXTERNAL_ACCOUNT)
                .disbursementExternalBankAccountId(EXTERNAL_ACCOUNT_ID);

        var extAccount = new ApplicationExternalBankAccountDTO(EXTERNAL_ACCOUNT_ID, null, null)
                .iban("ES1234567890")
                .bankName("Firefly Bank");

        when(loanApplicationsApi.getLoanApplication(eq(APPLICATION_ID), isNull()))
                .thenReturn(Mono.just(app));
        when(applicationExternalBankAccountsApi.get(eq(APPLICATION_ID), eq(EXTERNAL_ACCOUNT_ID), isNull()))
                .thenReturn(Mono.just(extAccount));

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
        var app = new LoanApplicationDTO(APPLICATION_ID);

        when(loanApplicationsApi.getLoanApplication(eq(APPLICATION_ID), isNull()))
                .thenReturn(Mono.just(app));

        StepVerifier.create(service.getDisbursementAccount(APPLICATION_ID))
                .verifyComplete();
    }

    @Test
    void getDisbursementAccount_propagatesUpstreamError() {
        when(loanApplicationsApi.getLoanApplication(eq(APPLICATION_ID), isNull()))
                .thenReturn(Mono.error(new RuntimeException("upstream error")));

        StepVerifier.create(service.getDisbursementAccount(APPLICATION_ID))
                .expectError(RuntimeException.class)
                .verify();
    }

    // --- configureDisbursementAccount ---

    @Test
    void configureDisbursementAccount_setsInternalAccount() {
        var existing = new LoanApplicationDTO(APPLICATION_ID);
        var updated = new LoanApplicationDTO(APPLICATION_ID)
                .disbursementMethodType(LoanApplicationDTO.DisbursementMethodTypeEnum.INTERNAL_ACCOUNT)
                .disbursementInternalAccountId(INTERNAL_ACCOUNT_ID);

        when(loanApplicationsApi.getLoanApplication(eq(APPLICATION_ID), isNull()))
                .thenReturn(Mono.just(existing))
                .thenReturn(Mono.just(updated));
        when(loanApplicationsApi.updateLoanApplication(eq(APPLICATION_ID), any(), any()))
                .thenReturn(Mono.just(updated));

        var command = new ConfigureDisbursementAccountCommand();
        command.setAccountId(INTERNAL_ACCOUNT_ID);
        command.setAccountType("INTERNAL");

        StepVerifier.create(service.configureDisbursementAccount(APPLICATION_ID, command))
                .assertNext(dto -> {
                    assertThat(dto.getAccountType()).isEqualTo("INTERNAL");
                    assertThat(dto.getAccountId()).isEqualTo(INTERNAL_ACCOUNT_ID);
                })
                .verifyComplete();
    }

    @Test
    void configureDisbursementAccount_setsExternalAccount() {
        var existing = new LoanApplicationDTO(APPLICATION_ID);
        var updated = new LoanApplicationDTO(APPLICATION_ID)
                .disbursementMethodType(LoanApplicationDTO.DisbursementMethodTypeEnum.EXTERNAL_ACCOUNT)
                .disbursementExternalBankAccountId(EXTERNAL_ACCOUNT_ID);
        var extAccount = new ApplicationExternalBankAccountDTO(EXTERNAL_ACCOUNT_ID, null, null)
                .iban("ES9900").bankName("Test Bank");

        when(loanApplicationsApi.getLoanApplication(eq(APPLICATION_ID), isNull()))
                .thenReturn(Mono.just(existing))
                .thenReturn(Mono.just(updated));
        when(loanApplicationsApi.updateLoanApplication(eq(APPLICATION_ID), any(), any()))
                .thenReturn(Mono.just(updated));
        when(applicationExternalBankAccountsApi.get(eq(APPLICATION_ID), eq(EXTERNAL_ACCOUNT_ID), isNull()))
                .thenReturn(Mono.just(extAccount));

        var command = new ConfigureDisbursementAccountCommand();
        command.setAccountId(EXTERNAL_ACCOUNT_ID);
        command.setAccountType("EXTERNAL");

        StepVerifier.create(service.configureDisbursementAccount(APPLICATION_ID, command))
                .assertNext(dto -> {
                    assertThat(dto.getAccountType()).isEqualTo("EXTERNAL");
                    assertThat(dto.getIban()).isEqualTo("ES9900");
                })
                .verifyComplete();
    }

    // --- registerExternalAccount ---

    @Test
    void registerExternalAccount_returnsCreatedDTO() {
        var sdkResponse = new ApplicationExternalBankAccountDTO(EXTERNAL_ACCOUNT_ID,
                LocalDateTime.now(), null)
                .loanApplicationId(APPLICATION_ID)
                .iban("ES1234")
                .bankName("My Bank")
                .accountHolderName("John Doe");

        when(applicationExternalBankAccountsApi.create(eq(APPLICATION_ID), any(), any()))
                .thenReturn(Mono.just(sdkResponse));

        var command = new RegisterExternalAccountCommand();
        command.setIban("ES1234");
        command.setBankName("My Bank");
        command.setHolderName("John Doe");

        StepVerifier.create(service.registerExternalAccount(APPLICATION_ID, command))
                .assertNext(dto -> {
                    assertThat(dto.getAccountId()).isEqualTo(EXTERNAL_ACCOUNT_ID);
                    assertThat(dto.getIban()).isEqualTo("ES1234");
                    assertThat(dto.getBankName()).isEqualTo("My Bank");
                    assertThat(dto.getHolderName()).isEqualTo("John Doe");
                })
                .verifyComplete();
    }

    @Test
    void registerExternalAccount_propagatesUpstreamError() {
        when(applicationExternalBankAccountsApi.create(eq(APPLICATION_ID), any(), any()))
                .thenReturn(Mono.error(new RuntimeException("create failed")));

        var command = new RegisterExternalAccountCommand();
        command.setIban("ES0000");
        command.setBankName("Fail Bank");
        command.setHolderName("Jane");

        StepVerifier.create(service.registerExternalAccount(APPLICATION_ID, command))
                .expectError(RuntimeException.class)
                .verify();
    }

    // --- listExternalAccounts ---

    @Test
    void listExternalAccounts_returnsMappedAccounts() {
        var acc = new ApplicationExternalBankAccountDTO(EXTERNAL_ACCOUNT_ID, LocalDateTime.now(), null)
                .iban("ES5555")
                .bankName("List Bank")
                .accountHolderName("Alice");

        var page = new PaginationResponseApplicationExternalBankAccountDTO()
                .content(List.of(acc));

        when(applicationExternalBankAccountsApi.findAll(eq(APPLICATION_ID), any(), isNull()))
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

        when(applicationExternalBankAccountsApi.findAll(eq(APPLICATION_ID), any(), isNull()))
                .thenReturn(Mono.just(page));

        StepVerifier.create(service.listExternalAccounts(APPLICATION_ID))
                .verifyComplete();
    }

    // --- deleteExternalAccount ---

    @Test
    void deleteExternalAccount_completesSuccessfully() {
        when(applicationExternalBankAccountsApi.delete(eq(APPLICATION_ID), eq(EXTERNAL_ACCOUNT_ID), any()))
                .thenReturn(Mono.empty());

        StepVerifier.create(service.deleteExternalAccount(APPLICATION_ID, EXTERNAL_ACCOUNT_ID))
                .verifyComplete();

        verify(applicationExternalBankAccountsApi).delete(eq(APPLICATION_ID), eq(EXTERNAL_ACCOUNT_ID), any());
    }

    @Test
    void deleteExternalAccount_propagatesUpstreamError() {
        when(applicationExternalBankAccountsApi.delete(eq(APPLICATION_ID), eq(EXTERNAL_ACCOUNT_ID), any()))
                .thenReturn(Mono.error(new RuntimeException("delete failed")));

        StepVerifier.create(service.deleteExternalAccount(APPLICATION_ID, EXTERNAL_ACCOUNT_ID))
                .expectError(RuntimeException.class)
                .verify();
    }
}
