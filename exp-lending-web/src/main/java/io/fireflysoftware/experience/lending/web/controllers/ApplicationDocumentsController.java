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


package io.fireflysoftware.experience.lending.web.controllers;

import io.fireflysoftware.experience.lending.core.application.documents.commands.UploadDocumentCommand;
import io.fireflysoftware.experience.lending.core.application.documents.queries.ApplicationDocumentDTO;
import io.fireflysoftware.experience.lending.core.application.documents.services.ApplicationDocumentsService;
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
 * REST controller exposing document management endpoints for a loan application
 * (list, upload, delete).
 */
@RestController
@RequestMapping("/api/v1/experience/lending/applications/{id}/documents")
@RequiredArgsConstructor
@Tag(name = "Lending - Application Documents")
public class ApplicationDocumentsController {

    private final ApplicationDocumentsService applicationDocumentsService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List Documents",
            description = "Returns all documents attached to a loan application.")
    public Flux<ApplicationDocumentDTO> listDocuments(@PathVariable UUID id) {
        return applicationDocumentsService.listDocuments(id);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Upload Document",
            description = "Attaches a new document to a loan application.")
    public Mono<ResponseEntity<ApplicationDocumentDTO>> uploadDocument(
            @PathVariable UUID id,
            @RequestBody UploadDocumentCommand command) {
        return applicationDocumentsService.uploadDocument(id, command)
                .map(result -> ResponseEntity.status(HttpStatus.CREATED).body(result));
    }

    @DeleteMapping("/{docId}")
    @Operation(summary = "Delete Document",
            description = "Removes a document from a loan application.")
    public Mono<ResponseEntity<Void>> deleteDocument(
            @PathVariable UUID id,
            @PathVariable UUID docId) {
        return applicationDocumentsService.deleteDocument(id, docId)
                .thenReturn(ResponseEntity.<Void>noContent().build());
    }
}
