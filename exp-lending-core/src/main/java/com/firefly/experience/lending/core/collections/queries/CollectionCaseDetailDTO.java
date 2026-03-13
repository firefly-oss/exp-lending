package com.firefly.experience.lending.core.collections.queries;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

/**
 * Full detail view of a collection case, including all recorded collection actions.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CollectionCaseDetailDTO extends CollectionCaseSummaryDTO {

    private List<CollectionActionDTO> actions;
}
