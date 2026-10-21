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
import io.fireflysoftware.experience.lending.core.servicing.queries.AmortizationCalendarRow;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.UUID;

/**
 * Borrower read of the amortization calendar. Calls domain-lending-loan-servicing-sdk
 * {@code getAmortizationCalendar}. Restructuring and end of day stay in the domain.
 */
@Service
public class AmortizationCalendarJourney {

    private final LoanServicingQueriesApi loanServicingQueriesApi;

    public AmortizationCalendarJourney(LoanServicingQueriesApi loanServicingQueriesApi) {
        this.loanServicingQueriesApi = loanServicingQueriesApi;
    }

    public Flux<AmortizationCalendarRow> read(UUID loanId) {
        return loanServicingQueriesApi.getAmortizationCalendar(loanId, null).map(this::toRow);
    }

    private AmortizationCalendarRow toRow(AmortizationCalendarEntry entry) {
        return new AmortizationCalendarRow(
                entry.getInstallmentNumber(),
                entry.getDueDate(),
                entry.getPrincipal(),
                entry.getInterest(),
                entry.getRemainingBalance());
    }
}
