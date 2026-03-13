package com.firefly.experience.lending.core.application.queries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Status transition history for a loan application, containing an ordered list of status entries.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationStatusHistoryDTO {

    private List<StatusEntryDTO> entries;
}
