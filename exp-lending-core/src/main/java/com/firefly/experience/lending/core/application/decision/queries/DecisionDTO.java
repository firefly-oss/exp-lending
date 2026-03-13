package com.firefly.experience.lending.core.application.decision.queries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Underwriting decision for a loan application, including the outcome and reason codes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DecisionDTO {

    private UUID applicationId;

    /** Underwriting outcome: APPROVED, REJECTED, or CONDITIONAL. */
    private String result;

    /** Human-readable reason codes explaining the decision. */
    private List<String> reasons;

    private LocalDateTime decidedAt;
}
