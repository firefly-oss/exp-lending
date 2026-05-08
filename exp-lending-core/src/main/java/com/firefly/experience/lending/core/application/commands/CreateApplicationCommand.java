package com.firefly.experience.lending.core.application.commands;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Command to create a new loan application for a given product, amount, term, and purpose.
 *
 * <p>The {@code simulationId} carries the soft link back to the simulation that produced this
 * application (table {@code simulation}, V14). It is nullable for backward compatibility with
 * callers that bypass the simulator, but the BE-3 traceability spec expects it to be populated
 * whenever the front-end has run a simulator pass before posting the application.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateApplicationCommand {

    /**
     * Soft link to the simulation that produced this application. Nullable to preserve
     * backward compatibility — but required for the simulation-to-application traceability
     * defined by BE-3.
     */
    private UUID simulationId;

    @NotNull(message = "productId is required")
    private UUID productId;

    @NotNull(message = "requestedAmount is required")
    @DecimalMin(value = "0.01", message = "requestedAmount must be strictly positive")
    private BigDecimal requestedAmount;

    @NotNull(message = "term is required")
    @Min(value = 1, message = "term must be at least 1 month")
    private Integer term;

    private String purpose;
}
