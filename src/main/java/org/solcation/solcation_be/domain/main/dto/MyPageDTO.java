package org.solcation.solcation_be.domain.main.dto;

import lombok.*;

@Getter
@Builder
public class MyPageDTO {
    private Long userPk;
    private String userId;
    private String userName;
    private String email;
    private String tel;
}
