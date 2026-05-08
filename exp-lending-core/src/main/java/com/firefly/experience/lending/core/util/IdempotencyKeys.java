package com.firefly.experience.lending.core.util;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Derives deterministic idempotency keys from stable business inputs so that
 * retries (network blips, downstream-side retries, replays) do not produce
 * duplicate downstream rows.
 *
 * <p>The contract is: <em>same logical operation, same inputs → same key</em>.
 * Implementation uses {@link UUID#nameUUIDFromBytes(byte[])} (RFC&nbsp;4122 v3)
 * over a {@code ":"}-joined input string in UTF-8 so the output is always a
 * valid UUID string and fits the standard {@code Idempotency-Key} HTTP header.</p>
 */
public final class IdempotencyKeys {

    private IdempotencyKeys() {
        // utility
    }

    /**
     * Derives a deterministic UUID-shaped idempotency key from the given parts.
     * Null parts are coerced to the literal string {@code "null"} so the call
     * never throws on missing inputs.
     *
     * @param parts identifying inputs, joined with {@code ":"}
     * @return a stable v3 UUID string derived from the parts
     */
    public static String of(String... parts) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                sb.append(':');
            }
            sb.append(parts[i] == null ? "null" : parts[i]);
        }
        return UUID.nameUUIDFromBytes(sb.toString().getBytes(StandardCharsets.UTF_8)).toString();
    }
}
