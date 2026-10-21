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


package io.fireflysoftware.experience.lending.core.product;

import java.util.UUID;

/**
 * Result of try. An outbound try returns {@code challengeId} and {@code challengeValue}.
 * An internal try has neither. The value is the sandbox delivery of the challenge
 * because no SMS adapter is connected. Confirm accepts only that value.
 */
public record PaymentTry(UUID paymentId, PaymentKind kind, PaymentPath path, UUID challengeId, String challengeValue) {
}
