package com.firefly.experience.lending.core.application.services.impl;

import com.firefly.domain.lending.loan.origination.sdk.api.LoanOriginationApi;
import com.firefly.domain.lending.loan.origination.sdk.model.LoanApplicationDTO;
import com.firefly.domain.lending.loan.origination.sdk.model.RegisterLoanApplicationCommand;
import com.firefly.domain.lending.loan.origination.sdk.model.SubmitApplicationCommand;
import com.firefly.experience.lending.core.application.commands.CreateApplicationCommand;
import com.firefly.experience.lending.core.application.commands.UpdateApplicationCommand;
import com.firefly.experience.lending.core.application.queries.ApplicationDetailDTO;
import com.firefly.experience.lending.core.application.queries.ApplicationStatusHistoryDTO;
import com.firefly.experience.lending.core.application.queries.ApplicationSummaryDTO;
import com.firefly.experience.lending.core.application.services.ApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Default implementation of {@link ApplicationService}, delegating to the Loan Origination SDK
 * for application lifecycle operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private final LoanOriginationApi loanOriginationApi;

    @Override
    public Mono<ApplicationDetailDTO> createApplication(CreateApplicationCommand command) {
        log.debug("Creating application for productId={}", command.getProductId());

        var registerCmd = new RegisterLoanApplicationCommand()
                .loanApplicationId(UUID.randomUUID())
                .applicationDate(LocalDate.now())
                .loanPurpose(command.getPurpose());

        var submitCmd = new SubmitApplicationCommand()
                .application(registerCmd);

        return loanOriginationApi.submitApplication(submitCmd, UUID.randomUUID().toString())
                .flatMap(response -> {
                    UUID applicationId = extractUuid(response instanceof Map<?, ?> m ? m : Map.of(),
                            "loanApplicationId");
                    if (applicationId == null) {
                        applicationId = registerCmd.getLoanApplicationId();
                    }
                    return loanOriginationApi.getApplication(applicationId, UUID.randomUUID().toString());
                })
                .map(dto -> mapToDetail(dto, command.getRequestedAmount(), command.getTerm(),
                        command.getPurpose()));
    }

    @Override
    public Flux<ApplicationSummaryDTO> listApplications() {
        // MVP: domain-lending-loan-origination has no list-all endpoint.
        // Replace when upstream adds a GET collection endpoint.
        log.debug("Listing applications - returning empty (no upstream list endpoint in MVP)");
        return Flux.empty();
    }

    @Override
    public Mono<ApplicationDetailDTO> getApplication(UUID applicationId) {
        log.debug("Getting application applicationId={}", applicationId);
        return loanOriginationApi.getApplication(applicationId, UUID.randomUUID().toString())
                .map(dto -> mapToDetail(dto, null, null, null));
    }

    @Override
    public Mono<ApplicationDetailDTO> updateApplication(UUID applicationId, UpdateApplicationCommand command) {
        // MVP: domain-lending-loan-origination has no PATCH endpoint.
        // Fetches current state and returns it. Replace when upstream adds an update endpoint.
        log.debug("Updating application applicationId={}", applicationId);
        return loanOriginationApi.getApplication(applicationId, UUID.randomUUID().toString())
                .map(dto -> mapToDetail(dto,
                        command.getRequestedAmount() != null ? command.getRequestedAmount() : null,
                        command.getTerm(),
                        command.getPurpose() != null ? command.getPurpose() : dto.getLoanPurpose()));
    }

    @Override
    public Mono<Void> submitApplication(UUID applicationId) {
        // MVP: no dedicated "submit for review" endpoint; delegates to approveApplication.
        // Replace when upstream adds a submit/transition endpoint.
        log.debug("Submitting application applicationId={}", applicationId);
        return loanOriginationApi.approveApplication(applicationId, UUID.randomUUID().toString()).then();
    }

    @Override
    public Mono<Void> withdrawApplication(UUID applicationId) {
        log.debug("Withdrawing application applicationId={}", applicationId);
        return loanOriginationApi.withdrawApplication(applicationId, UUID.randomUUID().toString()).then();
    }

    @Override
    public Mono<ApplicationStatusHistoryDTO> getStatusHistory(UUID applicationId) {
        // MVP: domain-lending-loan-origination has no status-history endpoint.
        // Replace when upstream exposes the status history resource.
        log.debug("Getting status history for applicationId={}", applicationId);
        return Mono.just(ApplicationStatusHistoryDTO.builder()
                .entries(List.of())
                .build());
    }

    private ApplicationDetailDTO mapToDetail(LoanApplicationDTO dto, java.math.BigDecimal requestedAmount,
                                              Integer term, String purpose) {
        return ApplicationDetailDTO.builder()
                .applicationId(dto.getLoanApplicationId())
                .status(dto.getApplicationStatusId() != null ? dto.getApplicationStatusId().toString() : null)
                .requestedAmount(requestedAmount)
                .term(term)
                .purpose(purpose != null ? purpose : dto.getLoanPurpose())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }

    private UUID extractUuid(Map<?, ?> map, String key) {
        Object value = map.get(key);
        if (value instanceof String s) return UUID.fromString(s);
        if (value instanceof UUID u) return u;
        return null;
    }
}
