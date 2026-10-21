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

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Underwriting decision for a loan application, including the outcome and reason codes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DecisionDTO {

    private UUID applicationId;

    /** Underwriting outcome: APPROVED, REJECTED, or CONDITIONAL. */
    private String result;

    /** Human-readable reason codes explaining the decision. */
    private List<String> reasons;

    private LocalDateTime decidedAt;
}
