package com.firefly.experience.lending.core.application.queries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * A single status transition entry in a loan application's status history.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatusEntryDTO {

    private String status;
    private LocalDateTime timestamp;
    private String reason;
}
