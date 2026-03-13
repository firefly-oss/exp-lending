package com.firefly.experience.lending.core.servicing.queries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Full history of balance snapshots for an active loan, ordered chronologically.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceHistoryDTO {

    private List<BalanceDTO> entries;
}
