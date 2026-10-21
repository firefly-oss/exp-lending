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


package io.fireflysoftware.experience.lending.core.application.parties.services;

import io.fireflysoftware.experience.lending.core.application.parties.commands.AddApplicationPartyCommand;
import io.fireflysoftware.experience.lending.core.application.parties.commands.UpdateApplicationPartyCommand;
import io.fireflysoftware.experience.lending.core.application.parties.queries.ApplicationPartyDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Service for managing parties (e.g. applicants, co-borrowers, guarantors)
 * associated with a loan application.
 */
public interface ApplicationPartiesService {

    Flux<ApplicationPartyDTO> listParties(UUID applicationId);

    Mono<ApplicationPartyDTO> addParty(UUID applicationId, AddApplicationPartyCommand command);

    Mono<ApplicationPartyDTO> getParty(UUID applicationId, UUID partyId);

    Mono<ApplicationPartyDTO> updateParty(UUID applicationId, UUID partyId, UpdateApplicationPartyCommand command);

    Mono<Void> removeParty(UUID applicationId, UUID partyId);
}
