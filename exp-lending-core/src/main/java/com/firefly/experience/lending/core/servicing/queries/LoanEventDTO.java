package com.firefly.experience.lending.core.servicing.queries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A servicing event log entry for an active loan (e.g. payment received, status changed).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanEventDTO {

    private UUID eventId;
    private String eventType;
    private String description;
    private LocalDateTime timestamp;
}
