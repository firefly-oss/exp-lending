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
import io.fireflysoftware.domain.lending.loan.servicing.sdk.model.EndOfDayResult;
import io.fireflysoftware.domain.lending.loan.servicing.sdk.model.GenerateScheduleCommand;
import io.fireflysoftware.domain.lending.loan.servicing.sdk.model.InterestPosting;
import io.fireflysoftware.experience.lending.core.ports.AccessDecision;
import io.fireflysoftware.experience.lending.core.ports.AccessDeniedException;
import io.fireflysoftware.experience.lending.core.ports.AccessPort;
import io.fireflysoftware.experience.lending.core.ports.IdentityPort;
import io.fireflysoftware.experience.lending.core.ports.SubjectRef;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanBookJourneyTest {

    @Mock
    private IdentityPort identityPort;
    @Mock
    private AccessPort accessPort;
    @Mock
    private LoanServicingApi loanServicingApi;

    private LoanBookJourney journey;

    @BeforeEach
    void setUp() {
        when(identityPort.resolve(any())).thenReturn(Mono.just(new SubjectRef(IdentityPort.SAMPLE_ACTOR, "sandbox")));
        journey = new LoanBookJourney(identityPort, accessPort, loanServicingApi);
    }

    @Test
    void scheduleAndEndOfDayCallTheLoanServicingDomain() {
        UUID loanId = UUID.randomUUID();
        LocalDate due = LocalDate.of(2026, 11, 1);
        LocalDate businessDate = LocalDate.of(2026, 10, 21);
        when(accessPort.check(any())).thenReturn(Mono.just(AccessDecision.grant()));
        when(loanServicingApi.generateSchedule(eq(loanId.toString()), any(GenerateScheduleCommand.class), any()))
                .thenReturn(Mono.just("stored"));
        when(loanServicingApi.endOfDay(any(EndOfDayRequest.class), any())).thenReturn(Mono.just(
                new EndOfDayResult().businessDate(businessDate).journals(List.of(new InterestPosting()))));

        var schedule = journey.buildSchedule(loanId, due).block();
        var day = journey.endOfDay(businessDate).block();

        assertThat(schedule.firstDueDate()).isEqualTo(due);
        assertThat(day.businessDate()).isEqualTo(businessDate);
        assertThat(day.journals()).isEqualTo(1);
        verify(loanServicingApi).generateSchedule(eq(loanId.toString()), any(GenerateScheduleCommand.class), any());
        verify(loanServicingApi).endOfDay(any(EndOfDayRequest.class), any());
    }

    @Test
    void deniedCallerDoesNotCallTheDomain() {
        when(accessPort.check(any())).thenReturn(Mono.just(AccessDecision.denied("no")));

        org.junit.jupiter.api.Assertions.assertThrows(AccessDeniedException.class,
                () -> journey.endOfDay(LocalDate.of(2026, 10, 21)).block());
        verify(loanServicingApi, never()).endOfDay(any(), any());
        verify(loanServicingApi, never()).generateSchedule(any(), any(), any());
    }
}
