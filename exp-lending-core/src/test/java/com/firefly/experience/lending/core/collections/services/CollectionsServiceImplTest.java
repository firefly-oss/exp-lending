package com.firefly.experience.lending.core.collections.services;

import com.firefly.core.lending.servicing.sdk.api.LoanServicingCaseApi;
import com.firefly.core.lending.servicing.sdk.model.LoanServicingCaseDTO;
import com.firefly.core.lending.servicing.sdk.model.PaginationResponseLoanServicingCaseDTO;
import com.firefly.experience.lending.core.collections.commands.RegisterPaymentPromiseCommand;
import com.firefly.experience.lending.core.collections.services.impl.CollectionsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CollectionsServiceImplTest {

    @Mock
    private LoanServicingCaseApi loanServicingCaseApi;

    private CollectionsServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CollectionsServiceImpl(loanServicingCaseApi);
    }

    // --- listCollectionCases ---

    @Test
    void listCollectionCases_mapsServicingCasesToSummaries() {
        var caseId = UUID.randomUUID();
        var dto = new LoanServicingCaseDTO(caseId)
                .servicingStatus(LoanServicingCaseDTO.ServicingStatusEnum.ACTIVE)
                .principalAmount(new BigDecimal("5000.00"));

        var page = new PaginationResponseLoanServicingCaseDTO().content(List.of(dto));
        when(loanServicingCaseApi.findAllServicingCases(any(), any()))
                .thenReturn(Mono.just(page));

        StepVerifier.create(service.listCollectionCases())
                .assertNext(summary -> {
                    assertThat(summary.getCaseId()).isEqualTo(caseId);
                    assertThat(summary.getLoanId()).isEqualTo(caseId);
                    assertThat(summary.getStatus()).isEqualTo("ACTIVE");
                    assertThat(summary.getOverdueAmount()).isEqualByComparingTo(new BigDecimal("5000.00"));
                    assertThat(summary.getDaysPastDue()).isEqualTo(0);
                })
                .verifyComplete();
    }

    @Test
    void listCollectionCases_returnsEmpty_whenPageContentIsNull() {
        var page = new PaginationResponseLoanServicingCaseDTO().content(null);
        when(loanServicingCaseApi.findAllServicingCases(any(), any()))
                .thenReturn(Mono.just(page));

        StepVerifier.create(service.listCollectionCases())
                .verifyComplete();
    }

    // --- getCollectionCase ---

    @Test
    void getCollectionCase_mapsServicingCaseToDetail() {
        var caseId = UUID.randomUUID();
        var dto = new LoanServicingCaseDTO(caseId)
                .servicingStatus(LoanServicingCaseDTO.ServicingStatusEnum.DELINQUENT)
                .principalAmount(new BigDecimal("3200.00"));

        when(loanServicingCaseApi.getServicingCaseById(eq(caseId), any()))
                .thenReturn(Mono.just(dto));

        StepVerifier.create(service.getCollectionCase(caseId))
                .assertNext(detail -> {
                    assertThat(detail.getCaseId()).isEqualTo(caseId);
                    assertThat(detail.getStatus()).isEqualTo("DELINQUENT");
                    assertThat(detail.getOverdueAmount()).isEqualByComparingTo(new BigDecimal("3200.00"));
                    assertThat(detail.getActions()).isEmpty();
                })
                .verifyComplete();
    }

    // --- registerPaymentPromise ---

    @Test
    void registerPaymentPromise_returnsStubPromise() {
        var caseId = UUID.randomUUID();
        var command = new RegisterPaymentPromiseCommand();
        command.setPromisedAmount(new BigDecimal("1500.00"));
        command.setPromiseDate(LocalDate.of(2026, 3, 31));

        StepVerifier.create(service.registerPaymentPromise(caseId, command))
                .assertNext(promise -> {
                    assertThat(promise.getPromiseId()).isNotNull();
                    assertThat(promise.getPromisedAmount()).isEqualByComparingTo(new BigDecimal("1500.00"));
                    assertThat(promise.getPromiseDate()).isEqualTo(LocalDate.of(2026, 3, 31));
                    assertThat(promise.getStatus()).isEqualTo("PENDING");
                })
                .verifyComplete();
    }
}
