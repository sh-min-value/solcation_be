package org.solcation.solcation_be.domain.wallet.card.dto;

import jakarta.validation.constraints.Pattern;

public record VerifyPwReq(
        @Pattern(regexp="\\d{6}")
        String sacPw
) {}
