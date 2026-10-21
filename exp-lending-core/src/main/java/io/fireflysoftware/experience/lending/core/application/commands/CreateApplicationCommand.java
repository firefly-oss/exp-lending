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


package io.fireflysoftware.experience.lending.core.application.commands;

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

    /**
     * Party the decision is about. Sent to {@code POST /api/v1/decisions}.
     * When omitted, the product id is used so the decision contract still has a party id.
     */
    private UUID partyId;
}
