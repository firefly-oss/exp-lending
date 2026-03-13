package com.firefly.experience.lending.core.application.details.queries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

/**
 * A task that must be completed as part of a loan application workflow.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDTO {

    private UUID taskId;
    private String description;
    private String status;
    private LocalDate dueDate;
}
