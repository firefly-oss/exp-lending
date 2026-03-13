package com.firefly.experience.lending.core.collections.queries;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Summary view of a collection case for use in list responses.
 */
@Data
@SuperBuilder
@NoArgsConstructor
public class CollectionCaseSummaryDTO {

    private UUID caseId;
    private UUID loanId;
    private String status;
    private BigDecimal overdueAmount;
    private Integer daysPastDue;
}
