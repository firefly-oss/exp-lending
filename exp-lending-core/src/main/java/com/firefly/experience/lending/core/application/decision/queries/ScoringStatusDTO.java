package com.firefly.experience.lending.core.application.decision.queries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Current credit scoring status for a loan application, including the computed score when available.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScoringStatusDTO {

    private UUID applicationId;

    /** Current scoring status: PENDING, IN_PROGRESS, or COMPLETED. */
    private String status;

    /** Credit score value — null when status is PENDING or IN_PROGRESS. */
    private Integer score;
}
