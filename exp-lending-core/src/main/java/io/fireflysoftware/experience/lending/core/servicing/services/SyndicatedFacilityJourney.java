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
import io.fireflysoftware.experience.lending.core.servicing.queries.SyndicatedFacilityView;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Borrower read of a syndicated facility. Calls domain-lending-loan-servicing-sdk
 * {@code viewSyndicatedFacility}. This journey does not administer participants.
 */
@Service
public class SyndicatedFacilityJourney {

    private final SyndicatedFacilityApi syndicatedFacilityApi;

    public SyndicatedFacilityJourney(SyndicatedFacilityApi syndicatedFacilityApi) {
        this.syndicatedFacilityApi = syndicatedFacilityApi;
    }

    public Mono<SyndicatedFacilityView> view(UUID loanId) {
        return syndicatedFacilityApi.viewSyndicatedFacility(loanId, null).map(this::toView);
    }

    private SyndicatedFacilityView toView(BorrowerFacilityView facility) {
        return new SyndicatedFacilityView(
                facility.getFacilityId(),
                facility.getLoanId(),
                facility.getCurrency(),
                facility.getCommitmentAmount(),
                facility.getStatus());
    }
}
