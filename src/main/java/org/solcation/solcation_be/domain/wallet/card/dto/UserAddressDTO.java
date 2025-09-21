package org.solcation.solcation_be.domain.wallet.card.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.solcation.solcation_be.common.annotation.KstDateTime;

import java.time.Instant;

@Schema(name = "유저 주소 정보 DTO")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAddressDTO {
    @NotNull
    private String address;

    @NotNull
    private  String addressDetail;

    @NotNull
    private  String postCode;

}
