package org.solcation.solcation_be.domain.auth.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import org.solcation.solcation_be.entity.GENDER;
import org.solcation.solcation_be.entity.ROLE;
import org.solcation.solcation_be.entity.User;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignupReqDTO {
    @NotNull
    @NotBlank(message = "아이디 입력")
    @Size(max = 10)
    private String userId;

    @NotNull
    @NotBlank(message = "비밀번호 입력")
    @Size(max = 20)
    private String userPw;

    @NotNull
    @NotBlank(message = "주소 입력")
    @Size(max = 100)
    private String streetAddr;

    @NotNull
    @NotBlank(message = "상세 주소 입력")
    @Size(max = 100)
    private String addrDetail;

    @NotNull
    @NotBlank(message = "우편번호 입력")
    @Size(max = 5)
    private String postalCode;

    @NotNull
    @NotBlank(message = "전화번호 입력")
    @Size(max = 11)
    private String tel;

    @NotNull
    @NotBlank(message = "이름 입력")
    @Size(max = 30)
    private String userName;

    @NotNull
    private LocalDate dateOfBirth;

    @NotNull
    @NotBlank(message = "성별 입력")
    private String gender;

    @Email(message = "이메일 형식 오류")
    @NotNull
    @NotBlank(message = "이메일 입력")
    private String email;

    public static User toEntity(SignupReqDTO dto) {
        if(dto == null)
            return null;
        return User.builder()
                .userId(dto.getUserId())
                .userPw(dto.getUserPw())
                .streetAddr(dto.getStreetAddr())
                .addrDetail(dto.getAddrDetail())
                .postalCode(dto.getPostalCode())
                .tel(dto.getTel())
                .userName(dto.getUserName())
                .dateOfBirth(dto.getDateOfBirth())
                .gender(("m".equalsIgnoreCase(dto.getGender())) ? GENDER.M : GENDER.F)
                .role(ROLE.USER)
                .email(dto.getEmail())
                .build();
    }
}
