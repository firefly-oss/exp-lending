package com.firefly.experience.lending.core.application.details.queries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Result of an identity or document verification performed during a loan application.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificationDTO {

    private UUID verificationId;
    private String type;
    private String status;
    private LocalDateTime completedAt;
}
