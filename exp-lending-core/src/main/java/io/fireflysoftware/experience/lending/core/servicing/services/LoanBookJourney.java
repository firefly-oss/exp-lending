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

import io.fireflysoftware.domain.lending.loan.servicing.sdk.api.LoanServicingApi;
import io.fireflysoftware.domain.lending.loan.servicing.sdk.model.EndOfDayRequest;
import io.fireflysoftware.domain.lending.loan.servicing.sdk.model.GenerateScheduleCommand;
import io.fireflysoftware.experience.lending.core.ports.AccessAction;
import io.fireflysoftware.experience.lending.core.ports.AccessGuard;
import io.fireflysoftware.experience.lending.core.ports.AccessPort;
import io.fireflysoftware.experience.lending.core.ports.AccessRequest;
import io.fireflysoftware.experience.lending.core.ports.IdentityPort;
import io.fireflysoftware.experience.lending.core.servicing.queries.BuiltSchedule;
import io.fireflysoftware.experience.lending.core.servicing.queries.EndOfDayRun;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Builds the amortization schedule and runs end of day.
 * Both calls go to domain-lending-loan-servicing. A missing date or a denied caller does not.
 */
@Service
public class LoanBookJourney {

    private final IdentityPort identityPort;
    private final AccessPort accessPort;
    private final LoanServicingApi loanServicingApi;

    public LoanBookJourney(IdentityPort identityPort, AccessPort accessPort, LoanServicingApi loanServicingApi) {
        this.identityPort = identityPort;
        this.accessPort = accessPort;
        this.loanServicingApi = loanServicingApi;
    }

    public Mono<BuiltSchedule> buildSchedule(UUID loanId, LocalDate firstDueDate) {
        if (loanId == null || firstDueDate == null) {
            return Mono.error(new IllegalArgumentException("Loan and first due date are required"));
        }
        return AccessGuard.require(identityPort, accessPort, IdentityPort.SAMPLE_USERNAME,
                AccessRequest.of(loanId, AccessAction.UPDATE, "APPLICATION", loanId),
                () -> loanServicingApi.generateSchedule(loanId.toString(),
                                new GenerateScheduleCommand().firstDueDate(firstDueDate), null)
                        .thenReturn(new BuiltSchedule(loanId, firstDueDate)));
    }

    public Mono<EndOfDayRun> endOfDay(LocalDate businessDate) {
        if (businessDate == null) {
            return Mono.error(new IllegalArgumentException("Business date is required"));
        }
        return AccessGuard.require(identityPort, accessPort, IdentityPort.SAMPLE_USERNAME,
                AccessRequest.of(null, AccessAction.UPDATE, "APPLICATION", null),
                () -> loanServicingApi.endOfDay(new EndOfDayRequest().businessDate(businessDate), null)
                        .map(result -> new EndOfDayRun(
                                result.getBusinessDate() == null ? businessDate : result.getBusinessDate(),
                                result.getJournals() == null ? 0 : result.getJournals().size())));
    }
}
