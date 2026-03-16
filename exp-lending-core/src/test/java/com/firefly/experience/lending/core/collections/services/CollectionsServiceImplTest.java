package com.firefly.experience.lending.core.collections.services;

import com.firefly.domain.lending.loan.servicing.sdk.api.LoanServicingApi;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CollectionsServiceImplTest {

    @Mock
    private LoanServicingApi loanServicingApi;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private CollectionsServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CollectionsServiceImpl(loanServicingApi, objectMapper);
    }

    // --- listCollectionCases ---

    @Test
    void listCollectionCases_returnsEmpty() {
        StepVerifier.create(service.listCollectionCases())
                .verifyComplete();
    }

    // --- getCollectionCase ---

    @Test
    void getCollectionCase_mapsServicingCaseToDetail() {
        var caseId = UUID.randomUUID();
        var responseMap = Map.of(
                "loanServicingCaseId", caseId.toString(),
                "servicingStatus", "DELINQUENT",
                "principalAmount", "3200.00"
        );

        when(loanServicingApi.getLoanDetails(eq(caseId.toString()), any()))
                .thenReturn(Mono.just(responseMap));

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
