package com.firefly.experience.lending.web.controllers;

import com.firefly.experience.lending.core.application.parties.commands.AddApplicationPartyCommand;
import com.firefly.experience.lending.core.application.parties.commands.UpdateApplicationPartyCommand;
import com.firefly.experience.lending.core.application.parties.queries.ApplicationPartyDTO;
import com.firefly.experience.lending.core.application.parties.services.ApplicationPartiesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * REST controller exposing party management endpoints for a loan application
 * (list, add, get, update, remove).
 */
@RestController
@RequestMapping("/api/v1/experience/lending/applications/{id}/parties")
@RequiredArgsConstructor
@Tag(name = "Lending - Application Parties")
public class ApplicationPartiesController {

    private final ApplicationPartiesService applicationPartiesService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List Parties",
            description = "Returns all parties associated with a loan application.")
    public Flux<ApplicationPartyDTO> listParties(@PathVariable UUID id) {
        return applicationPartiesService.listParties(id);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Add Party",
            description = "Associates a new party (co-holder, guarantor, etc.) with a loan application.")
    public Mono<ResponseEntity<ApplicationPartyDTO>> addParty(
            @PathVariable UUID id,
            @RequestBody AddApplicationPartyCommand command) {
        return applicationPartiesService.addParty(id, command)
                .map(result -> ResponseEntity.status(HttpStatus.CREATED).body(result));
    }

    @GetMapping(value = "/{partyId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get Party",
            description = "Fetches details of a specific party associated with the application.")
    public Mono<ResponseEntity<ApplicationPartyDTO>> getParty(
            @PathVariable UUID id,
            @PathVariable UUID partyId) {
        return applicationPartiesService.getParty(id, partyId)
                .map(ResponseEntity::ok);
    }

    @PutMapping(value = "/{partyId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update Party",
            description = "Updates a party's information on a loan application.")
    public Mono<ResponseEntity<ApplicationPartyDTO>> updateParty(
            @PathVariable UUID id,
            @PathVariable UUID partyId,
            @RequestBody UpdateApplicationPartyCommand command) {
        return applicationPartiesService.updateParty(id, partyId, command)
                .map(ResponseEntity::ok);
    }

    @DeleteMapping("/{partyId}")
    @Operation(summary = "Remove Party",
            description = "Removes a party from a loan application.")
    public Mono<ResponseEntity<Void>> removeParty(
            @PathVariable UUID id,
            @PathVariable UUID partyId) {
        return applicationPartiesService.removeParty(id, partyId)
                .thenReturn(ResponseEntity.<Void>noContent().build());
    }
}
