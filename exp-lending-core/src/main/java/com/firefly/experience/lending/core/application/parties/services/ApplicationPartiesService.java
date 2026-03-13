package com.firefly.experience.lending.core.application.parties.services;

import com.firefly.experience.lending.core.application.parties.commands.AddApplicationPartyCommand;
import com.firefly.experience.lending.core.application.parties.commands.UpdateApplicationPartyCommand;
import com.firefly.experience.lending.core.application.parties.queries.ApplicationPartyDTO;
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
