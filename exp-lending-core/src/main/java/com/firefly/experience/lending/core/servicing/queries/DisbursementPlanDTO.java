package com.firefly.experience.lending.core.servicing.queries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Planned disbursement schedule for an active loan, containing ordered disbursement entries.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisbursementPlanDTO {

    private List<DisbursementDTO> entries;
}
