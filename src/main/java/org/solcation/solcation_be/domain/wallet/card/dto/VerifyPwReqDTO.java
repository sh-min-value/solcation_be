package org.solcation.solcation_be.domain.wallet.card.dto;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class VerifyPwReqDTO {
        @Pattern(regexp="\\d{6}", message = "비밀번호는 6자리입니다.")
        String sacPw;
}
