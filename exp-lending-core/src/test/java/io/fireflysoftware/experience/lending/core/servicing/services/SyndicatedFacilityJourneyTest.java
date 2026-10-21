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

import io.fireflysoftware.domain.lending.loan.servicing.sdk.api.SyndicatedFacilityApi;
import io.fireflysoftware.domain.lending.loan.servicing.sdk.model.BorrowerFacilityView;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SyndicatedFacilityJourneyTest {

    @Test
    void viewCallsTheDomainSdkAndOmitsParticipants() {
        UUID loanId = UUID.randomUUID();
        UUID facilityId = UUID.randomUUID();
        SyndicatedFacilityApi api = mock(SyndicatedFacilityApi.class);
        BorrowerFacilityView domainView = new BorrowerFacilityView()
                .facilityId(facilityId)
                .loanId(loanId)
                .currency("EUR")
                .commitmentAmount(new BigDecimal("1000000"))
                .status("OPEN");
        when(api.viewSyndicatedFacility(loanId, null)).thenReturn(Mono.just(domainView));

        var shown = new SyndicatedFacilityJourney(api).view(loanId).block();

        assertThat(shown.facilityId()).isEqualTo(facilityId);
        assertThat(shown.loanId()).isEqualTo(loanId);
        assertThat(shown.currency()).isEqualTo("EUR");
        assertThat(shown.commitmentAmount()).isEqualByComparingTo("1000000");
        assertThat(shown.status()).isEqualTo("OPEN");
        assertThat(shown.getClass().getRecordComponents())
                .extracting(java.lang.reflect.RecordComponent::getName)
                .doesNotContain("participants", "participantRegister");
        verify(api).viewSyndicatedFacility(loanId, null);
    }
}
