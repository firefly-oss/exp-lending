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

package com.firefly.experience.lending.web.controllers;

import com.firefly.experience.lending.core.personalloans.commands.CreatePersonalLoanCommand;
import com.firefly.experience.lending.core.personalloans.queries.PersonalLoanDetailDTO;
import com.firefly.experience.lending.core.personalloans.queries.PersonalLoanSummaryDTO;
import com.firefly.experience.lending.core.personalloans.services.PersonalLoansService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
 * REST controller exposing personal loan endpoints: create, list, get, and update agreements.
 */
@RestController
@RequestMapping("/api/v1/experience/lending/personal-loans")
@RequiredArgsConstructor
@Tag(name = "Personal Loans", description = "Personal loan agreement management")
public class PersonalLoansController {

    private final PersonalLoansService personalLoansService;

    // -------------------------------------------------------------------------
    // Create
    // -------------------------------------------------------------------------

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create Personal Loan Agreement",
               description = "Creates a new personal loan agreement.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Agreement created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public Mono<ResponseEntity<PersonalLoanDetailDTO>> createAgreement(
            @RequestBody CreatePersonalLoanCommand command) {
        return personalLoansService.createAgreement(command)
                .map(result -> ResponseEntity.status(HttpStatus.CREATED).body(result));
    }

    // -------------------------------------------------------------------------
    // List
    // -------------------------------------------------------------------------

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List Personal Loan Agreements",
               description = "Returns a list of personal loan agreements.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Agreements retrieved successfully")
    })
    public Flux<PersonalLoanSummaryDTO> listAgreements() {
        return personalLoansService.listAgreements();
    }

    // -------------------------------------------------------------------------
    // Get
    // -------------------------------------------------------------------------

    @GetMapping(value = "/{agreementId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get Personal Loan Agreement",
               description = "Returns full details of a single personal loan agreement.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Agreement retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Agreement not found")
    })
    public Mono<ResponseEntity<PersonalLoanDetailDTO>> getAgreement(@PathVariable UUID agreementId) {
        return personalLoansService.getAgreement(agreementId)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    // -------------------------------------------------------------------------
    // Update
    // -------------------------------------------------------------------------

    @PutMapping(value = "/{agreementId}",
                consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update Personal Loan Agreement",
               description = "Updates an existing personal loan agreement.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Agreement updated successfully"),
            @ApiResponse(responseCode = "404", description = "Agreement not found")
    })
    public Mono<ResponseEntity<PersonalLoanDetailDTO>> updateAgreement(
            @PathVariable UUID agreementId,
            @RequestBody CreatePersonalLoanCommand command) {
        return personalLoansService.updateAgreement(agreementId, command)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }
}
