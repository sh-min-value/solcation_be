package org.solcation.solcation_be.domain.wallet.account.dto;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SharedAccountLoginReqDTO {
    @Pattern(regexp="\\d{6}", message = "비밀번호는 6자리입니다.")
    private String saPw;
}
