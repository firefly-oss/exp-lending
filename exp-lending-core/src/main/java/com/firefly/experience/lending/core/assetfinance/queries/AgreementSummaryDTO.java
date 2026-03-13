package com.firefly.experience.lending.core.assetfinance.queries;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Summary view of an asset finance agreement (leasing or renting) for use in list responses.
 */
@Data
@SuperBuilder
@NoArgsConstructor
public class AgreementSummaryDTO {

    private UUID agreementId;
    private String financeType;
    private String status;
    private BigDecimal totalValue;
}
