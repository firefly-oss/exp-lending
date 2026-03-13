package com.firefly.experience.lending.core.collections.queries;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A collection action recorded against a collection case (e.g. call made, letter sent).
 */
@Data
@SuperBuilder
@NoArgsConstructor
public class CollectionActionDTO {

    private UUID actionId;
    private String actionType;
    private String description;
    private LocalDateTime performedAt;
}
