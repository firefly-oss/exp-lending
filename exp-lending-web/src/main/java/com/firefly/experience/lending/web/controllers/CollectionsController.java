package com.firefly.experience.lending.web.controllers;

import com.firefly.experience.lending.core.collections.commands.RegisterPaymentPromiseCommand;
import com.firefly.experience.lending.core.collections.queries.CollectionCaseDetailDTO;
import com.firefly.experience.lending.core.collections.queries.CollectionCaseSummaryDTO;
import com.firefly.experience.lending.core.collections.queries.PaymentPromiseDTO;
import com.firefly.experience.lending.core.collections.services.CollectionsService;
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
 * REST controller exposing delinquency collection case endpoints and payment promise registration.
 */
@RestController
@RequestMapping("/api/v1/experience/lending/collections")
@RequiredArgsConstructor
@Tag(name = "Lending - Collections")
public class CollectionsController {

    private final CollectionsService collectionsService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List Collection Cases",
            description = "Returns all active collection cases accessible to the caller.")
    public Flux<CollectionCaseSummaryDTO> listCollectionCases() {
        return collectionsService.listCollectionCases();
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get Collection Case",
            description = "Retrieves full details of a collection case including its actions.")
    public Mono<ResponseEntity<CollectionCaseDetailDTO>> getCollectionCase(@PathVariable UUID id) {
        return collectionsService.getCollectionCase(id)
                .map(ResponseEntity::ok);
    }

    @PostMapping(value = "/{id}/promise-to-pay",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Register Payment Promise",
            description = "Records a promise-to-pay agreement against a collection case.")
    public Mono<ResponseEntity<PaymentPromiseDTO>> registerPaymentPromise(
            @PathVariable UUID id,
            @RequestBody RegisterPaymentPromiseCommand command) {
        return collectionsService.registerPaymentPromise(id, command)
                .map(dto -> ResponseEntity.status(HttpStatus.CREATED).body(dto));
    }
}
