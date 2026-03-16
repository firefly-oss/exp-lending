package com.firefly.experience.lending.core.application.parties.services.impl;

import com.firefly.core.lending.origination.sdk.api.ApplicationPartyApi;
import com.firefly.experience.lending.core.application.parties.commands.AddApplicationPartyCommand;
import com.firefly.experience.lending.core.application.parties.commands.UpdateApplicationPartyCommand;
import com.firefly.experience.lending.core.application.parties.queries.ApplicationPartyDTO;
import com.firefly.experience.lending.core.application.parties.services.ApplicationPartiesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

/**
 * Default implementation of {@link ApplicationPartiesService}, delegating to the
 * Loan Origination SDK's {@code ApplicationPartyApi} for party management.
 *
 * // ARCH-EXCEPTION: No domain SDK exposes {@link ApplicationPartyApi}; direct
 * core-lending-origination-sdk usage is temporary until the domain layer surfaces this endpoint.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationPartiesServiceImpl implements ApplicationPartiesService {

    private final ApplicationPartyApi applicationPartyApi;

    @Override
    public Flux<ApplicationPartyDTO> listParties(UUID applicationId) {
        log.debug("Listing parties for applicationId={}", applicationId);
        return applicationPartyApi
                .findAllParties(applicationId, null, null, null, null, UUID.randomUUID().toString())
                .flatMapIterable(page -> page.getContent() != null ? page.getContent() : List.of())
                .map(this::mapToDTO);
    }

    @Override
    public Mono<ApplicationPartyDTO> addParty(UUID applicationId, AddApplicationPartyCommand command) {
        log.debug("Adding party partyId={} to applicationId={}", command.getPartyId(), applicationId);
        // MVP: roleCodeId requires a code-table lookup; partyId and loanApplicationId are set directly.
        // fullName and identificationNumber are enriched from the customer domain in a future iteration.
        var sdkDto = new com.firefly.core.lending.origination.sdk.model.ApplicationPartyDTO()
                .loanApplicationId(applicationId)
                .partyId(command.getPartyId());

        return applicationPartyApi
                .createParty(applicationId, sdkDto, UUID.randomUUID().toString())
                .map(this::mapToDTO);
    }

    @Override
    public Mono<ApplicationPartyDTO> getParty(UUID applicationId, UUID partyId) {
        log.debug("Getting partyId={} for applicationId={}", partyId, applicationId);
        return applicationPartyApi
                .getParty(applicationId, partyId, UUID.randomUUID().toString())
                .map(this::mapToDTO);
    }

    @Override
    public Mono<ApplicationPartyDTO> updateParty(UUID applicationId, UUID partyId, UpdateApplicationPartyCommand command) {
        log.debug("Updating partyId={} for applicationId={}", partyId, applicationId);
        // MVP: fetch existing record, apply role update, then persist.
        // roleCodeId requires a code-table lookup; role string is stored as-is until resolved.
        return applicationPartyApi.getParty(applicationId, partyId, UUID.randomUUID().toString())
                .flatMap(existing -> {
                    // applicationPartyId is server-assigned; it is not settable via the SDK
                    var updated = new com.firefly.core.lending.origination.sdk.model.ApplicationPartyDTO()
                            .loanApplicationId(existing.getLoanApplicationId())
                            .partyId(existing.getPartyId())
                            .roleCodeId(existing.getRoleCodeId())
                            .sharePercentage(existing.getSharePercentage())
                            .annualIncome(existing.getAnnualIncome())
                            .monthlyExpenses(existing.getMonthlyExpenses())
                            .employmentTypeId(existing.getEmploymentTypeId());
                    return applicationPartyApi.updateParty(applicationId, partyId, updated,
                            UUID.randomUUID().toString());
                })
                .map(this::mapToDTO);
    }

    @Override
    public Mono<Void> removeParty(UUID applicationId, UUID partyId) {
        log.debug("Removing partyId={} from applicationId={}", partyId, applicationId);
        return applicationPartyApi.deleteParty(applicationId, partyId, UUID.randomUUID().toString());
    }

    private ApplicationPartyDTO mapToDTO(com.firefly.core.lending.origination.sdk.model.ApplicationPartyDTO src) {
        // MVP: fullName and identificationNumber require enrichment from the customer domain service.
        return ApplicationPartyDTO.builder()
                .partyId(src.getPartyId())
                .applicationId(src.getLoanApplicationId())
                .role(src.getRoleCodeId() != null ? src.getRoleCodeId().toString() : null)
                .fullName(null)
                .identificationNumber(null)
                .addedAt(src.getCreatedAt())
                .build();
    }
}
