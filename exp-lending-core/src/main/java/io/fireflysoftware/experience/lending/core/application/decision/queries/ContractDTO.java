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
import java.util.UUID;

/**
 * Loan contract associated with a loan application, including its status and document reference.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractDTO {

    private UUID contractId;
    private UUID applicationId;

    /** Contract lifecycle status (mirrors ContractStatusEnum). */
    private String status;

    /** URL or reference to the contract document in the ECM store. */
    private String documentUrl;

    /** Timestamp when all parties completed e-signature — null until signing is complete. */
    private LocalDateTime signedAt;
}
