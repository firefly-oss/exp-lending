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


package io.fireflysoftware.experience.lending.core.servicing.services;

import io.fireflysoftware.domain.lending.loan.servicing.sdk.api.LoanServicingQueriesApi;
import io.fireflysoftware.domain.lending.loan.servicing.sdk.model.AmortizationCalendarEntry;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AmortizationCalendarJourneyTest {

    @Test
    void readUsesTheDomainSdkCalendar() {
        UUID loanId = UUID.randomUUID();
        LoanServicingQueriesApi api = mock(LoanServicingQueriesApi.class);
        AmortizationCalendarEntry entry = new AmortizationCalendarEntry()
                .installmentNumber(1)
                .dueDate(LocalDate.of(2026, 2, 1))
                .principal(new BigDecimal("88.00"))
                .interest(new BigDecimal("12.00"))
                .remainingBalance(new BigDecimal("1112.00"));
        when(api.getAmortizationCalendar(loanId, null)).thenReturn(Flux.just(entry));

        var row = new AmortizationCalendarJourney(api).read(loanId).blockFirst();

        assertThat(row.installmentNumber()).isEqualTo(1);
        assertThat(row.dueDate()).isEqualTo(LocalDate.of(2026, 2, 1));
        assertThat(row.principal()).isEqualByComparingTo("88.00");
        assertThat(row.interest()).isEqualByComparingTo("12.00");
        assertThat(row.remainingBalance()).isEqualByComparingTo("1112.00");
        verify(api).getAmortizationCalendar(loanId, null);
    }
}
