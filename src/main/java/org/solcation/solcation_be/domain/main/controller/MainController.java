package org.solcation.solcation_be.domain.main.controller;

import lombok.RequiredArgsConstructor;
import org.solcation.solcation_be.common.CustomException;
import org.solcation.solcation_be.common.ErrorCode;
import org.solcation.solcation_be.domain.main.dto.GroupShortDTO;
import org.solcation.solcation_be.domain.main.dto.MonthlyPlanDTO;
import org.solcation.solcation_be.domain.main.dto.MyPageDTO;
import org.solcation.solcation_be.domain.main.dto.NotificationPreviewDTO;
import org.solcation.solcation_be.domain.main.service.GroupShortService;
import org.solcation.solcation_be.domain.main.service.MonthlyPlanService;
import org.solcation.solcation_be.domain.main.service.MyPageService;
import org.solcation.solcation_be.domain.main.service.NotificationPreviewService;
import org.solcation.solcation_be.entity.User;
import org.solcation.solcation_be.repository.UserRepository;
import org.solcation.solcation_be.security.JwtPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/main")
@RequiredArgsConstructor
public class MainController {

    private final GroupShortService groupShortService;
    private final MyPageService myPageService;
    private final UserRepository userRepository;
    private final NotificationPreviewService notificationPreviewService;
    private final MonthlyPlanService monthlyPlanService;

    @GetMapping("/my-groups")
    public List<GroupShortDTO> getMyGroups(
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        Long userPk = userRepository.findByUserId(principal.userId())
                .map(User::getUserPk)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return groupShortService.getUserGroups(userPk);
    }

    @GetMapping("/mypage")
    public MyPageDTO getMyPage(
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        Long userPk = userRepository.findByUserId(principal.userId())
                .map(User::getUserPk)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return myPageService.getMyPage(userPk);
    }

    @GetMapping("/notification-preview")
    public List<NotificationPreviewDTO> getNotificationPreview(
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        Long userPk = userRepository.findByUserId(principal.userId())
                .map(User::getUserPk)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return notificationPreviewService.getNotificationPreview(userPk);
    }

    @GetMapping("/monthly-plans")
    public List<MonthlyPlanDTO> getMonthlyPlans(
            @RequestParam int year,
            @RequestParam int month,
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        Long userPk = userRepository.findByUserId(principal.userId())
                .map(User::getUserPk)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return monthlyPlanService.getMonthlyPlans(userPk, year, month);
    }
}
