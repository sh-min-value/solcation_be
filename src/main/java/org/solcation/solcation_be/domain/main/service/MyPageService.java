package org.solcation.solcation_be.domain.main.service;

import lombok.RequiredArgsConstructor;
import org.solcation.solcation_be.common.CustomException;
import org.solcation.solcation_be.common.ErrorCode;
import org.solcation.solcation_be.domain.main.dto.MyPageDTO;
import org.solcation.solcation_be.entity.User;
import org.solcation.solcation_be.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MyPageService {

    private final UserRepository userRepository;

    public MyPageDTO getMyPage(Long userPk) {
        User user = userRepository.findById(userPk)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return MyPageDTO.builder()
                .userPk(user.getUserPk())
                .userId(user.getUserId())
                .userName(user.getUserName())
                .email(user.getEmail())
                .tel(user.getTel())
                .build();
    }
}