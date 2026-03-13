package com.firefly.experience.lending.core.application.decision.queries;

import com.firefly.experience.lending.core.application.details.queries.FeeDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.List;

/**
 * Full detail view of a proposed loan offer, including total cost, itemised fees, and conditions.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OfferDetailDTO extends OfferSummaryDTO {

    /** Total repayment cost: principal + interest + fees over the full term. */
    private BigDecimal totalCost;

    /** Itemised fees attached to this offer. */
    private List<FeeDTO> fees;

    /** Conditions that must be met before the offer can be disbursed. */
    private List<String> conditions;
}
