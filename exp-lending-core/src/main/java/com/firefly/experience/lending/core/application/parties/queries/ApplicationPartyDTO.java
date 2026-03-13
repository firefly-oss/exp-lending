package com.firefly.experience.lending.core.application.parties.queries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Party associated with a loan application (applicant, co-holder, guarantor, etc.).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationPartyDTO {

    private UUID partyId;
    private UUID applicationId;
    private String role;
    private String fullName;
    private String identificationNumber;
    private LocalDateTime addedAt;
}
