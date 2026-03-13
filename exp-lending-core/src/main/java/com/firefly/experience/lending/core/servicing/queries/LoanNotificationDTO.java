package com.firefly.experience.lending.core.servicing.queries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A notification sent to a customer in connection with their active loan.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanNotificationDTO {

    private UUID notificationId;
    private String message;
    private String channel;
    private LocalDateTime sentAt;
}
