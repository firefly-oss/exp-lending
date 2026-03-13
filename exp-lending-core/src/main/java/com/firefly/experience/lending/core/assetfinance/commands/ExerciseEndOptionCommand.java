package com.firefly.experience.lending.core.assetfinance.commands;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Command to exercise a lease-end option (e.g. purchase the asset) on an asset finance agreement.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseEndOptionCommand {

    private UUID optionId;
}
