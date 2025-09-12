package org.solcation.solcation_be.scheduler.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardExpiryDTO {
    private Long sacPk;
    private Instant nowIns;
}
