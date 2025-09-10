package org.solcation.solcation_be.domain.main;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.solcation.solcation_be.common.CustomException;
import org.solcation.solcation_be.common.ErrorCode;
import org.solcation.solcation_be.domain.main.dto.GroupShortDTO;
import org.solcation.solcation_be.domain.main.dto.MonthlyPlanDTO;
import org.solcation.solcation_be.domain.main.dto.MyPageDTO;
import org.solcation.solcation_be.domain.main.dto.NotificationPreviewDTO;
import org.solcation.solcation_be.entity.User;
import org.solcation.solcation_be.repository.UserRepository;
import org.solcation.solcation_be.security.JwtPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "메인 컨트롤러")
@RestController
@RequestMapping("/main")
@RequiredArgsConstructor
public class MainController {

//    private final MainService groupShortService;
//    private final MainService myPageService;
//    private final MainService notificationPreviewService;
//    private final MainService monthlyPlanService;
    private final MainService mainService;
    private final UserRepository userRepository;

    @Operation(summary = "사용자 그룹 조회")
    @GetMapping("/my-groups")
    public List<GroupShortDTO> getMyGroups(
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        Long userPk = userRepository.findByUserId(principal.userId())
                .map(User::getUserPk)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return mainService.getUserGroups(userPk);
    }

    @Operation(summary = "사용자 정보 조회")
    @GetMapping("/mypage")
    public MyPageDTO getMyPage(
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        Long userPk = userRepository.findByUserId(principal.userId())
                .map(User::getUserPk)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return mainService.getMyPage(userPk);
    }

    @Operation(summary = "최근 알림 2개 미리보기")
    @GetMapping("/notification-preview")
    public List<NotificationPreviewDTO> getNotificationPreview(
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        Long userPk = userRepository.findByUserId(principal.userId())
                .map(User::getUserPk)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return mainService.getNotificationPreview(userPk);
    }

    @Operation(summary = "같은 달 계획 조회")
    @GetMapping("/monthly-plans")
    public List<MonthlyPlanDTO> getMonthlyPlans(
            @RequestParam int year,
            @RequestParam int month,
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        Long userPk = userRepository.findByUserId(principal.userId())
                .map(User::getUserPk)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return mainService.getMonthlyPlans(userPk, year, month);
    }
}
