/*
 * Copyright 2025 Firefly Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package io.fireflysoftware.experience.lending.core.application.decision.queries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Current credit scoring status for a loan application, including the computed score when available.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScoringStatusDTO {

    private UUID applicationId;

    /** Current scoring status: PENDING, IN_PROGRESS, or COMPLETED. */
    private String status;

    /** Credit score value — null when status is PENDING or IN_PROGRESS. */
    private Integer score;
}
