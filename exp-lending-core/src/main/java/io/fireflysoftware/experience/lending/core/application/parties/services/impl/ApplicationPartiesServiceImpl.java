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


package io.fireflysoftware.experience.lending.core.application.parties.services.impl;

import io.fireflysoftware.domain.lending.loan.origination.sdk.api.LoanOriginationApi;
import io.fireflysoftware.domain.lending.loan.origination.sdk.model.ApplicationPartyDTO;
import io.fireflysoftware.experience.lending.core.application.parties.commands.AddApplicationPartyCommand;
import io.fireflysoftware.experience.lending.core.application.parties.commands.UpdateApplicationPartyCommand;
import io.fireflysoftware.experience.lending.core.application.parties.services.ApplicationPartiesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

/**
 * Default implementation of {@link ApplicationPartiesService}, delegating to the
 * domain Loan Origination SDK's {@code LoanOriginationApi} for party management.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationPartiesServiceImpl implements ApplicationPartiesService {

    private final LoanOriginationApi loanOriginationApi;

    @Override
    public Flux<io.fireflysoftware.experience.lending.core.application.parties.queries.ApplicationPartyDTO> listParties(UUID applicationId) {
        log.debug("Listing parties for applicationId={}", applicationId);
        return loanOriginationApi
                .getApplicationParties(applicationId, null)
                .flatMapIterable(page -> page.getContent() != null ? page.getContent() : List.of())
                .map(this::mapToDTO);
    }

    @Override
    public Mono<io.fireflysoftware.experience.lending.core.application.parties.queries.ApplicationPartyDTO> addParty(UUID applicationId, AddApplicationPartyCommand command) {
        log.debug("Adding party partyId={} to applicationId={}", command.getPartyId(), applicationId);
        // MVP: the domain SDK does not expose a dedicated create-party endpoint.
        // Party creation is handled as part of the application submission flow.
        // Returns a stub DTO. Replace when the domain layer surfaces this endpoint.
        return Mono.just(io.fireflysoftware.experience.lending.core.application.parties.queries.ApplicationPartyDTO.builder()
                .partyId(command.getPartyId())
                .applicationId(applicationId)
                .role(null)
                .fullName(null)
                .identificationNumber(null)
                .addedAt(null)
                .build());
    }

    @Override
    public Mono<io.fireflysoftware.experience.lending.core.application.parties.queries.ApplicationPartyDTO> getParty(UUID applicationId, UUID partyId) {
        log.debug("Getting partyId={} for applicationId={}", partyId, applicationId);
        return loanOriginationApi
                .getApplicationParties(applicationId, null)
                .flatMapIterable(page -> page.getContent() != null ? page.getContent() : List.of())
                .filter(p -> partyId.equals(p.getPartyId()))
                .next()
                .map(this::mapToDTO);
    }

    @Override
    public Mono<io.fireflysoftware.experience.lending.core.application.parties.queries.ApplicationPartyDTO> updateParty(UUID applicationId, UUID partyId, UpdateApplicationPartyCommand command) {
        log.debug("Updating partyId={} for applicationId={}", partyId, applicationId);
        // MVP: the domain SDK does not expose a dedicated update-party endpoint.
        // Returns the existing party unchanged. Replace when the domain layer surfaces this endpoint.
        return getParty(applicationId, partyId);
    }

    @Override
    public Mono<Void> removeParty(UUID applicationId, UUID partyId) {
        log.debug("Removing partyId={} from applicationId={}", partyId, applicationId);
        // MVP: the domain SDK does not expose a dedicated delete-party endpoint.
        // Operation completes as a no-op until the domain layer surfaces this endpoint.
        return Mono.empty();
    }

    private io.fireflysoftware.experience.lending.core.application.parties.queries.ApplicationPartyDTO mapToDTO(ApplicationPartyDTO src) {
        // MVP: fullName and identificationNumber require enrichment from the customer domain service.
        return io.fireflysoftware.experience.lending.core.application.parties.queries.ApplicationPartyDTO.builder()
                .partyId(src.getPartyId())
                .applicationId(src.getLoanApplicationId())
                .role(src.getRoleCodeId() != null ? src.getRoleCodeId().toString() : null)
                .fullName(null)
                .identificationNumber(null)
                .addedAt(src.getCreatedAt())
                .build();
    }
}
