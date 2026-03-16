package com.firefly.experience.lending.core.application.services;

import com.firefly.domain.lending.loan.origination.sdk.api.LoanOriginationApi;
import com.firefly.domain.lending.loan.origination.sdk.model.LoanApplicationDTO;
import com.firefly.domain.lending.loan.origination.sdk.model.SubmitApplicationCommand;
import com.firefly.experience.lending.core.application.commands.CreateApplicationCommand;
import com.firefly.experience.lending.core.application.commands.UpdateApplicationCommand;
import com.firefly.experience.lending.core.application.services.impl.ApplicationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceImplTest {

    @Mock
    private LoanOriginationApi loanOriginationApi;

    private ApplicationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ApplicationServiceImpl(loanOriginationApi);
    }

    @Test
    void createApplication_submitsToOriginationApi_andReturnsDetail() {
        var applicationId = UUID.randomUUID();
        Map<String, Object> submitResponse = Map.of("loanApplicationId", applicationId.toString());

        var dto = new LoanApplicationDTO()
                .loanApplicationId(applicationId)
                .loanPurpose("PERSONAL")
                .createdAt(LocalDateTime.now());

        when(loanOriginationApi.submitApplication(any(SubmitApplicationCommand.class), any()))
                .thenReturn(Mono.just(submitResponse));
        when(loanOriginationApi.getApplication(eq(applicationId), any()))
                .thenReturn(Mono.just(dto));

        var command = new CreateApplicationCommand();
        command.setProductId(UUID.randomUUID());
        command.setRequestedAmount(new BigDecimal("15000"));
        command.setTerm(36);
        command.setPurpose("PERSONAL");

        StepVerifier.create(service.createApplication(command))
                .assertNext(result -> {
                    assertThat(result).isNotNull();
                    assertThat(result.getApplicationId()).isEqualTo(applicationId);
                    assertThat(result.getRequestedAmount()).isEqualByComparingTo("15000");
                    assertThat(result.getTerm()).isEqualTo(36);
                    assertThat(result.getPurpose()).isEqualTo("PERSONAL");
                })
                .verifyComplete();
    }

    @Test
    void createApplication_fallsBackToGeneratedId_whenResponseMissingApplicationId() {
        Map<String, Object> submitResponse = Map.of();

        var dto = new LoanApplicationDTO()
                .loanApplicationId(UUID.randomUUID())
                .createdAt(LocalDateTime.now());

        when(loanOriginationApi.submitApplication(any(SubmitApplicationCommand.class), any()))
                .thenReturn(Mono.just(submitResponse));
        when(loanOriginationApi.getApplication(any(UUID.class), any()))
                .thenReturn(Mono.just(dto));

        var command = new CreateApplicationCommand();
        command.setProductId(UUID.randomUUID());
        command.setPurpose("HOME_IMPROVEMENT");

        StepVerifier.create(service.createApplication(command))
                .assertNext(result -> assertThat(result).isNotNull())
                .verifyComplete();
    }

    @Test
    void createApplication_propagatesUpstreamError() {
        when(loanOriginationApi.submitApplication(any(SubmitApplicationCommand.class), any()))
                .thenReturn(Mono.error(new RuntimeException("upstream error")));

        var command = new CreateApplicationCommand();
        command.setProductId(UUID.randomUUID());

        StepVerifier.create(service.createApplication(command))
                .expectErrorMessage("upstream error")
                .verify();
    }

    @Test
    void listApplications_returnsEmpty() {
        StepVerifier.create(service.listApplications())
                .verifyComplete();
    }

    @Test
    void getApplication_delegatesToOriginationApi_andMapsResult() {
        var applicationId = UUID.randomUUID();
        var dto = new LoanApplicationDTO()
                .loanApplicationId(applicationId)
                .loanPurpose("CAR")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now());

        when(loanOriginationApi.getApplication(eq(applicationId), any())).thenReturn(Mono.just(dto));

        StepVerifier.create(service.getApplication(applicationId))
                .assertNext(result -> {
                    assertThat(result.getApplicationId()).isEqualTo(applicationId);
                    assertThat(result.getPurpose()).isEqualTo("CAR");
                    assertThat(result.getCreatedAt()).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    void updateApplication_returnsCurrentStateWithPatchedFields() {
        var applicationId = UUID.randomUUID();
        var dto = new LoanApplicationDTO()
                .loanApplicationId(applicationId)
                .loanPurpose("RENOVATION")
                .createdAt(LocalDateTime.now());

        when(loanOriginationApi.getApplication(eq(applicationId), any())).thenReturn(Mono.just(dto));

        var command = new UpdateApplicationCommand();
        command.setRequestedAmount(new BigDecimal("20000"));
        command.setTerm(48);

        StepVerifier.create(service.updateApplication(applicationId, command))
                .assertNext(result -> {
                    assertThat(result.getApplicationId()).isEqualTo(applicationId);
                    assertThat(result.getRequestedAmount()).isEqualByComparingTo("20000");
                    assertThat(result.getTerm()).isEqualTo(48);
                    assertThat(result.getPurpose()).isEqualTo("RENOVATION");
                })
                .verifyComplete();
    }

    @Test
    void submitApplication_delegatesToApproveApplication() {
        var applicationId = UUID.randomUUID();
        when(loanOriginationApi.approveApplication(eq(applicationId), any()))
                .thenReturn(Mono.just(Map.of("status", "APPROVED")));

        StepVerifier.create(service.submitApplication(applicationId))
                .verifyComplete();
    }

    @Test
    void withdrawApplication_delegatesToWithdrawApplication() {
        var applicationId = UUID.randomUUID();
        when(loanOriginationApi.withdrawApplication(eq(applicationId), any()))
                .thenReturn(Mono.just(Map.of("status", "WITHDRAWN")));

        StepVerifier.create(service.withdrawApplication(applicationId))
                .verifyComplete();
    }

    @Test
    void getStatusHistory_returnsEmptyEntries() {
        var applicationId = UUID.randomUUID();

        StepVerifier.create(service.getStatusHistory(applicationId))
                .assertNext(result -> {
                    assertThat(result).isNotNull();
                    assertThat(result.getEntries()).isEmpty();
                })
                .verifyComplete();
    }
}
