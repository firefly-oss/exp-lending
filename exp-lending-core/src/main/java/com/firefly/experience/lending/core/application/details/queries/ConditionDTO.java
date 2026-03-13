package com.firefly.experience.lending.core.application.details.queries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * A condition that must be satisfied before a loan application can be disbursed.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConditionDTO {

    private UUID conditionId;
    private String description;
    private String status;
    private String type;
}
