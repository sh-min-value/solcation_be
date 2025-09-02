package org.solcation.solcation_be.domain.main.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyPageDTO {
//    private Long userPk;
    private String userId;
    private String userName;
    private String email;
    private String tel;
}
