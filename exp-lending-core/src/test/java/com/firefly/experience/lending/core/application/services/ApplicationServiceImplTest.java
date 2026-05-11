package com.firefly.experience.lending.core.application.services;

import com.firefly.domain.lending.loan.origination.sdk.api.LoanOriginationApi;
import com.firefly.domain.lending.loan.origination.sdk.model.LoanApplicationDTO;
import com.firefly.domain.lending.loan.origination.sdk.model.SubmitApplicationCommand;
import com.firefly.experience.lending.core.application.commands.CreateApplicationCommand;
import com.firefly.experience.lending.core.application.commands.UpdateApplicationCommand;
import com.firefly.experience.lending.core.application.services.impl.ApplicationServiceImpl;
import org.fireflyframework.web.error.exceptions.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.Mockito.verify;
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
        var simulationId = UUID.randomUUID();
        Map<String, UUID> submitResponse = Map.of("loanApplicationId", applicationId);

        var dto = new LoanApplicationDTO()
                .loanApplicationId(applicationId)
                .loanPurpose("PERSONAL")
                .simulationId(simulationId)
                .createdAt(LocalDateTime.now());

        when(loanOriginationApi.submitApplication(any(SubmitApplicationCommand.class), any()))
                .thenReturn(Mono.just(submitResponse));
        when(loanOriginationApi.getApplication(eq(applicationId), any()))
                .thenReturn(Mono.just(dto));

        var command = new CreateApplicationCommand();
        command.setSimulationId(simulationId);
        command.setProductId(UUID.randomUUID());
        command.setRequestedAmount(new BigDecimal("15000"));
        command.setTerm(36);
        command.setPurpose("PERSONAL");

        StepVerifier.create(service.createApplication(command))
                .assertNext(result -> {
                    assertThat(result).isNotNull();
                    assertThat(result.getApplicationId()).isEqualTo(applicationId);
                    assertThat(result.getSimulationId()).isEqualTo(simulationId);
                    assertThat(result.getRequestedAmount()).isEqualByComparingTo("15000");
                    assertThat(result.getTerm()).isEqualTo(36);
                    assertThat(result.getPurpose()).isEqualTo("PERSONAL");
                })
                .verifyComplete();
    }

    @Test
    void createApplication_passesSimulationIdThroughToDownstreamCommand() {
        var applicationId = UUID.randomUUID();
        var simulationId = UUID.randomUUID();
        Map<String, UUID> submitResponse = Map.of("loanApplicationId", applicationId);

        var dto = new LoanApplicationDTO()
                .loanApplicationId(applicationId)
                .createdAt(LocalDateTime.now());

        when(loanOriginationApi.submitApplication(any(SubmitApplicationCommand.class), any()))
                .thenReturn(Mono.just(submitResponse));
        when(loanOriginationApi.getApplication(eq(applicationId), any()))
                .thenReturn(Mono.just(dto));

        var command = new CreateApplicationCommand();
        command.setSimulationId(simulationId);
        command.setProductId(UUID.randomUUID());
        command.setRequestedAmount(new BigDecimal("9000"));
        command.setTerm(24);
        command.setPurpose("CAR");

        StepVerifier.create(service.createApplication(command))
                .expectNextCount(1)
                .verifyComplete();

        var captor = ArgumentCaptor.forClass(SubmitApplicationCommand.class);
        verify(loanOriginationApi).submitApplication(captor.capture(), any());
        var sent = captor.getValue();
        assertThat(sent.getApplication()).isNotNull();
        assertThat(sent.getApplication().getSimulationId()).isEqualTo(simulationId);
        assertThat(sent.getApplication().getLoanPurpose()).isEqualTo("CAR");
        assertThat(sent.getApplication().getApplicationDate()).isNotNull();
    }

    @Test
    void createApplication_failsWithBusinessException_whenResponseMissingApplicationId() {
        Map<String, UUID> submitResponse = Map.of();

        when(loanOriginationApi.submitApplication(any(SubmitApplicationCommand.class), any()))
                .thenReturn(Mono.just(submitResponse));

        var command = new CreateApplicationCommand();
        command.setProductId(UUID.randomUUID());
        command.setRequestedAmount(new BigDecimal("1000"));
        command.setTerm(12);
        command.setPurpose("HOME_IMPROVEMENT");

        StepVerifier.create(service.createApplication(command))
                .expectError(BusinessException.class)
                .verify();
    }

    @Test
    void createApplication_propagatesUpstreamError() {
        when(loanOriginationApi.submitApplication(any(SubmitApplicationCommand.class), any()))
                .thenReturn(Mono.error(new RuntimeException("upstream error")));

        var command = new CreateApplicationCommand();
        command.setProductId(UUID.randomUUID());
        command.setRequestedAmount(new BigDecimal("5000"));
        command.setTerm(18);

        StepVerifier.create(service.createApplication(command))
                .expectErrorMessage("upstream error")
                .verify();
    }

    @Test
    void createApplication_rejectsMissingProductId_withBusinessException() {
        var command = new CreateApplicationCommand();
        command.setRequestedAmount(new BigDecimal("5000"));
        command.setTerm(18);

        StepVerifier.create(service.createApplication(command))
                .expectError(BusinessException.class)
                .verify();
    }

    @Test
    void createApplication_rejectsNonPositiveRequestedAmount_withBusinessException() {
        var command = new CreateApplicationCommand();
        command.setProductId(UUID.randomUUID());
        command.setRequestedAmount(BigDecimal.ZERO);
        command.setTerm(18);

        StepVerifier.create(service.createApplication(command))
                .expectError(BusinessException.class)
                .verify();
    }

    @Test
    void createApplication_rejectsZeroTerm_withBusinessException() {
        var command = new CreateApplicationCommand();
        command.setProductId(UUID.randomUUID());
        command.setRequestedAmount(new BigDecimal("5000"));
        command.setTerm(0);

        StepVerifier.create(service.createApplication(command))
                .expectError(BusinessException.class)
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
