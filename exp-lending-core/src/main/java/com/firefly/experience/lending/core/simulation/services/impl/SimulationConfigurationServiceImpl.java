package com.firefly.experience.lending.core.simulation.services.impl;

import com.firefly.domain.core.pricing.engine.sdk.api.PricingEngineSimulationApi;
import com.firefly.domain.core.pricing.engine.sdk.model.CalculateSimulationCommand;
import com.firefly.domain.core.pricing.engine.sdk.model.SimulationCalculationResultDTO;
import com.firefly.domain.lending.loan.origination.sdk.api.LoanOriginationApi;
import com.firefly.domain.lending.loan.origination.sdk.model.PersistSimulationCommand;
import com.firefly.domain.lending.loan.origination.sdk.model.SimulationDTO;
import com.firefly.experience.lending.core.simulation.commands.ConfigureSimulationCommand;
import com.firefly.experience.lending.core.simulation.queries.ConfiguredSimulationDTO;
import com.firefly.experience.lending.core.simulation.services.SimulationConfigurationService;
import com.firefly.experience.lending.core.util.IdempotencyKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fireflyframework.web.error.exceptions.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Default implementation of {@link SimulationConfigurationService}: chains a
 * pricing-engine calculation with a persistence call into domain-lending-loan-origination.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SimulationConfigurationServiceImpl implements SimulationConfigurationService {

    private static final String PRODUCT_TYPE_PERSONAL_LOAN = "PERSONAL_LOAN";
    private static final String PRODUCT_TYPE_LEASING = "LEASING";

    private final PricingEngineSimulationApi pricingEngineSimulationApi;
    private final LoanOriginationApi loanOriginationApi;

    @Override
    public Mono<ConfiguredSimulationDTO> configure(ConfigureSimulationCommand cmd) {
        return Mono.fromCallable(() -> validate(cmd))
                .then(Mono.defer(() -> {
                    log.debug("Configuring simulation: productType={}, productId={}",
                            cmd.getProductType(), cmd.getProductId());

                    var calcCmd = new CalculateSimulationCommand()
                            .productId(cmd.getProductId())
                            .productType(cmd.getProductType())
                            .requestedAmount(cmd.getRequestedAmount())
                            .term(cmd.getTerm())
                            .purpose(cmd.getPurpose())
                            .sector(cmd.getSector())
                            .assetType(cmd.getAssetType());

                    // Bucket by minute + hash of input fields: identical
                    // simulation requests that retry within the same minute
                    // collapse to the same key, preventing duplicate persisted
                    // rows downstream. Distinct user requests in the same
                    // minute with identical inputs are intentionally
                    // deduplicated -- a safer default than random keys when
                    // upstream retries occur on the same logical request.
                    String sessionBucket = simulationSessionBucket(cmd);
                    String calcKey = IdempotencyKeys.of(
                            "exp-lending", "configure-simulation", "calculate", sessionBucket);

                    return pricingEngineSimulationApi
                            .calculateSimulation(calcCmd, calcKey)
                            .flatMap(calc -> persist(cmd, calc, sessionBucket))
                            .map(this::mapToConfigured);
                }));
    }

    private Mono<SimulationDTO> persist(ConfigureSimulationCommand cmd,
                                        SimulationCalculationResultDTO calc,
                                        String sessionBucket) {
        var persistCmd = new PersistSimulationCommand()
                .productId(calc.getProductId() != null ? calc.getProductId() : cmd.getProductId())
                .productType(calc.getProductType() != null ? calc.getProductType() : cmd.getProductType())
                .requestedAmount(calc.getRequestedAmount() != null
                        ? calc.getRequestedAmount() : cmd.getRequestedAmount())
                .term(calc.getTerm() != null ? calc.getTerm() : cmd.getTerm())
                .purpose(cmd.getPurpose())
                .sector(cmd.getSector())
                .assetType(cmd.getAssetType())
                .monthlyPayment(calc.getMonthlyPayment())
                .tin(calc.getTin())
                .tae(calc.getTae())
                .totalAmount(calc.getTotalAmount())
                .currency(calc.getCurrency());

        String persistKey = IdempotencyKeys.of(
                "exp-lending", "configure-simulation", "persist", sessionBucket);

        return loanOriginationApi.persistLendingSimulation(persistCmd, persistKey);
    }

    /**
     * Builds a stable per-request session discriminator by hashing the
     * inputs that define a logical simulation request, bucketed to the
     * minute. Two retries of the same logical request within the same
     * minute collapse to the same bucket and therefore reuse the same
     * idempotency keys downstream.
     */
    private String simulationSessionBucket(ConfigureSimulationCommand cmd) {
        String minuteBucket = Instant.now().truncatedTo(ChronoUnit.MINUTES).toString();
        return IdempotencyKeys.of(
                Objects.toString(cmd.getProductId(), "null"),
                Objects.toString(cmd.getProductType(), "null"),
                Objects.toString(cmd.getRequestedAmount(), "null"),
                Objects.toString(cmd.getTerm(), "null"),
                Objects.toString(cmd.getPurpose(), "null"),
                Objects.toString(cmd.getSector(), "null"),
                Objects.toString(cmd.getAssetType(), "null"),
                minuteBucket);
    }

    private ConfiguredSimulationDTO mapToConfigured(SimulationDTO persisted) {
        return ConfiguredSimulationDTO.builder()
                .simulationId(persisted.getSimulationId())
                .productType(persisted.getProductType())
                .productId(persisted.getProductId())
                .requestedAmount(persisted.getRequestedAmount())
                .term(persisted.getTerm())
                .monthlyPayment(persisted.getMonthlyPayment())
                .tin(persisted.getTin())
                .tae(persisted.getTae())
                .totalAmount(persisted.getTotalAmount())
                .currency(persisted.getCurrency())
                .build();
    }

    private ConfigureSimulationCommand validate(ConfigureSimulationCommand cmd) {
        String productType = cmd.getProductType();
        if (PRODUCT_TYPE_PERSONAL_LOAN.equals(productType)) {
            if (isBlank(cmd.getPurpose())) {
                throw new BusinessException(
                        HttpStatus.BAD_REQUEST,
                        "SIMULATION_INVALID_CONFIG",
                        "purpose is required when productType=PERSONAL_LOAN");
            }
        } else if (PRODUCT_TYPE_LEASING.equals(productType)) {
            if (isBlank(cmd.getSector()) || isBlank(cmd.getAssetType())) {
                throw new BusinessException(
                        HttpStatus.BAD_REQUEST,
                        "SIMULATION_INVALID_CONFIG",
                        "sector and assetType are required when productType=LEASING");
            }
        }
        return cmd;
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
