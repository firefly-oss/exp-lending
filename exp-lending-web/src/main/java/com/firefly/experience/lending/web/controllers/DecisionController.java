package com.firefly.experience.lending.web.controllers;

import com.firefly.experience.lending.core.application.decision.commands.AcceptOfferCommand;
import com.firefly.experience.lending.core.application.decision.commands.RejectOfferCommand;
import com.firefly.experience.lending.core.application.decision.commands.SignContractCommand;
import com.firefly.experience.lending.core.application.decision.queries.ContractDTO;
import com.firefly.experience.lending.core.application.decision.queries.DecisionDTO;
import com.firefly.experience.lending.core.application.decision.queries.OfferDetailDTO;
import com.firefly.experience.lending.core.application.decision.queries.OfferSummaryDTO;
import com.firefly.experience.lending.core.application.decision.queries.ScoringStatusDTO;
import com.firefly.experience.lending.core.application.decision.services.DecisionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * REST controller exposing the underwriting decision flow endpoints: scoring status,
 * decisions, offer management, and contract signing.
 */
@RestController
@RequestMapping("/api/v1/experience/lending/applications/{id}")
@RequiredArgsConstructor
@Tag(name = "Lending - Decision & Contract")
public class DecisionController {

    private final DecisionService decisionService;

    @GetMapping(value = "/scoring-status", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get Scoring Status",
            description = "Returns the current credit scoring status for a loan application. "
                    + "Status transitions: PENDING → IN_PROGRESS → COMPLETED.")
    public Mono<ResponseEntity<ScoringStatusDTO>> getScoringStatus(@PathVariable UUID id) {
        return decisionService.getScoringStatus(id)
                .map(ResponseEntity::ok);
    }

    @GetMapping(value = "/decision", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get Underwriting Decision",
            description = "Returns the underwriting decision (APPROVED, REJECTED, or CONDITIONAL) "
                    + "and the reason codes for the outcome.")
    public Mono<ResponseEntity<DecisionDTO>> getDecision(@PathVariable UUID id) {
        return decisionService.getDecision(id)
                .map(ResponseEntity::ok);
    }

    @GetMapping(value = "/offers", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List Offers",
            description = "Returns all proposed loan offers for the application.")
    public Flux<OfferSummaryDTO> getOffers(@PathVariable UUID id) {
        return decisionService.getOffers(id);
    }

    @GetMapping(value = "/offers/{offerId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get Offer Detail",
            description = "Returns the full detail of a specific offer, including total cost, "
                    + "itemised fees, and attached conditions.")
    public Mono<ResponseEntity<OfferDetailDTO>> getOffer(
            @PathVariable UUID id,
            @PathVariable UUID offerId) {
        return decisionService.getOffer(id, offerId)
                .map(ResponseEntity::ok);
    }

    @PostMapping(value = "/offers/{offerId}/accept", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Accept Offer",
            description = "Marks the specified offer as ACCEPTED. "
                    + "Only one offer per application may be accepted.")
    public Mono<ResponseEntity<Void>> acceptOffer(
            @PathVariable UUID id,
            @PathVariable UUID offerId) {
        return decisionService.acceptOffer(id, offerId)
                .thenReturn(ResponseEntity.<Void>ok().build());
    }

    @PostMapping(value = "/offers/{offerId}/reject",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Reject Offer",
            description = "Marks the specified offer as REJECTED. "
                    + "An optional reason may be provided.")
    public Mono<ResponseEntity<Void>> rejectOffer(
            @PathVariable UUID id,
            @PathVariable UUID offerId,
            @RequestBody RejectOfferCommand command) {
        return decisionService.rejectOffer(id, offerId, command)
                .thenReturn(ResponseEntity.<Void>ok().build());
    }

    @GetMapping(value = "/contract", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get Contract",
            description = "Returns the loan contract associated with this application, "
                    + "including its current status and document reference.")
    public Mono<ResponseEntity<ContractDTO>> getContract(@PathVariable UUID id) {
        return decisionService.getContract(id)
                .map(ResponseEntity::ok);
    }

    @PostMapping(value = "/contract/sign",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Sign Contract",
            description = "Initiates the SCA (Strong Customer Authentication) flow for contract "
                    + "signing and transitions the contract to ACTIVE status. "
                    + "The client must complete the SCA challenge to finalise the signature.")
    public Mono<ResponseEntity<ContractDTO>> signContract(
            @PathVariable UUID id,
            @RequestBody SignContractCommand command) {
        return decisionService.signContract(id, command)
                .map(ResponseEntity::ok);
    }
}
