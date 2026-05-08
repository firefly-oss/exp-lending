package com.firefly.experience.lending.core.application.services.impl;

import com.firefly.domain.lending.loan.origination.sdk.api.LoanOriginationApi;
import com.firefly.domain.lending.loan.origination.sdk.model.ApplicationPartyDTO;
import com.firefly.domain.lending.loan.origination.sdk.model.EmploymentDataPatchRequest;
import com.firefly.domain.lending.loan.origination.sdk.model.LoanApplicationDTO;
import com.firefly.domain.lending.loan.origination.sdk.model.RegisterLoanApplicationCommand;
import com.firefly.domain.lending.loan.origination.sdk.model.SubmitApplicationCommand;
import com.firefly.experience.lending.core.application.commands.CreateApplicationCommand;
import com.firefly.experience.lending.core.application.commands.UpdateApplicationCommand;
import com.firefly.experience.lending.core.application.commands.UpdateEmploymentDataCommand;
import com.firefly.experience.lending.core.application.queries.ApplicationDetailDTO;
import com.firefly.experience.lending.core.application.queries.ApplicationStatusHistoryDTO;
import com.firefly.experience.lending.core.application.queries.ApplicationSummaryDTO;
import com.firefly.experience.lending.core.application.services.ApplicationService;
import com.firefly.experience.lending.core.util.IdempotencyKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fireflyframework.web.error.exceptions.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
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
        return Mono.fromCallable(() -> validateCreateCommand(command))
                .flatMap(validated -> {
                    log.debug("Creating application productId={} simulationId={} requestedAmount={} term={}",
                            validated.getProductId(),
                            validated.getSimulationId(),
                            validated.getRequestedAmount(),
                            validated.getTerm());

                    // The client mints loanApplicationId up-front; treat it as
                    // the natural transaction key for this createApplication
                    // call so a retry of the same logical request reuses the
                    // same downstream idempotency keys.
                    UUID loanApplicationId = UUID.randomUUID();
                    var registerCmd = new RegisterLoanApplicationCommand()
                            .loanApplicationId(loanApplicationId)
                            .applicationDate(LocalDate.now())
                            .loanPurpose(validated.getPurpose())
                            .simulationId(validated.getSimulationId());

                    var submitCmd = new SubmitApplicationCommand()
                            .application(registerCmd);

                    String submitKey = IdempotencyKeys.of(
                            "exp-lending", "create-application", "submit",
                            loanApplicationId.toString());

                    return loanOriginationApi.submitApplication(submitCmd, submitKey)
                            .flatMap(response -> {
                                UUID applicationId = extractUuid(
                                        response instanceof Map<?, ?> m ? m : Map.of(),
                                        "loanApplicationId");
                                if (applicationId == null) {
                                    applicationId = registerCmd.getLoanApplicationId();
                                }
                                String getKey = IdempotencyKeys.of(
                                        "exp-lending", "create-application", "get",
                                        applicationId.toString());
                                return loanOriginationApi.getApplication(applicationId, getKey);
                            })
                            .map(dto -> mapToDetail(dto,
                                    validated.getRequestedAmount(),
                                    validated.getTerm(),
                                    validated.getPurpose(),
                                    validated.getSimulationId()));
                });
    }

    private CreateApplicationCommand validateCreateCommand(CreateApplicationCommand command) {
        if (command == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED",
                    "createApplication command is required");
        }
        if (command.getProductId() == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED",
                    "productId is required");
        }
        if (command.getRequestedAmount() == null
                || command.getRequestedAmount().signum() <= 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED",
                    "requestedAmount must be strictly positive");
        }
        if (command.getTerm() == null || command.getTerm() < 1) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED",
                    "term must be at least 1 month");
        }
        return command;
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
                .map(dto -> mapToDetail(dto, null, null, null, null));
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
                        command.getPurpose() != null ? command.getPurpose() : dto.getLoanPurpose(),
                        null));
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

    @Override
    public Mono<ApplicationPartyDTO> updateEmploymentData(UUID applicationId,
                                                          UpdateEmploymentDataCommand command) {
        log.debug("Updating employment data for applicationId={}", applicationId);
        // applicationId is the stable natural key for this PATCH-style upsert:
        // retrying the same logical update must not produce duplicate downstream
        // employment-data rows.
        String idempotencyKey = IdempotencyKeys.of(
                "exp-lending", "update-employment-data", applicationId.toString());
        return Mono.fromCallable(() -> buildPatch(command))
                .flatMap(patch -> loanOriginationApi.updateApplicationEmploymentData(
                        applicationId, patch, idempotencyKey));
    }

    private EmploymentDataPatchRequest buildPatch(UpdateEmploymentDataCommand cmd) {
        return new EmploymentDataPatchRequest()
                .employmentStatus(toUpper(cmd.getEmploymentStatus()))
                .employmentTypeLabel(toUpper(cmd.getEmploymentType()))
                .employer(cmd.getEmployer())
                .position(cmd.getPosition())
                .employmentStartDate(parseMonthYear(cmd.getEmploymentStartDate()))
                .annualPaydays(toInteger(cmd.getAnnualPaydays()))
                .monthlySalary(cmd.getMonthlySalary())
                .housingType(toUpper(cmd.getHousingType()))
                .housingCost(cmd.getHousingCost())
                .housingStartDate(parseMonthYear(cmd.getHousingStartDate()))
                .existingLoans(toInteger(cmd.getExistingLoans()))
                .otherDebts(cmd.getOtherDebts());
    }

    private static Integer toInteger(Short value) {
        return value == null ? null : value.intValue();
    }

    private LocalDate parseMonthYear(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        // Bean Validation already enforces MM/YYYY shape; defend against bypass.
        String[] parts = value.split("/");
        if (parts.length != 2) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_DATE_FORMAT",
                    "date must be in MM/YYYY format, was: " + value);
        }
        int month = Integer.parseInt(parts[0]);
        int year = Integer.parseInt(parts[1]);
        return LocalDate.of(year, month, 1);
    }

    private String toUpper(String s) {
        return s == null ? null : s.toUpperCase(Locale.ROOT);
    }

    private ApplicationDetailDTO mapToDetail(LoanApplicationDTO dto, java.math.BigDecimal requestedAmount,
                                              Integer term, String purpose, UUID simulationIdOverride) {
        UUID simulationId = simulationIdOverride != null
                ? simulationIdOverride
                : dto.getSimulationId();
        return ApplicationDetailDTO.builder()
                .applicationId(dto.getLoanApplicationId())
                .simulationId(simulationId)
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
