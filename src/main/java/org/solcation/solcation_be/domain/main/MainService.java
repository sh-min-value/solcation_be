package org.solcation.solcation_be.domain.main;

import lombok.RequiredArgsConstructor;
import org.solcation.solcation_be.common.CustomException;
import org.solcation.solcation_be.common.ErrorCode;
import org.solcation.solcation_be.domain.main.dto.GroupShortDTO;
import org.solcation.solcation_be.domain.main.dto.MonthlyPlanDTO;
import org.solcation.solcation_be.domain.main.dto.MyPageDTO;
import org.solcation.solcation_be.domain.main.dto.NotificationPreviewDTO;
import org.solcation.solcation_be.entity.GroupMember;
import org.solcation.solcation_be.entity.Travel;
import org.solcation.solcation_be.entity.User;
import org.solcation.solcation_be.repository.GroupMemberRepository;
import org.solcation.solcation_be.repository.PushNotificationRepository;
import org.solcation.solcation_be.repository.TravelRepository;
import org.solcation.solcation_be.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MainService {

    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final TravelRepository travelRepository;
    private final PushNotificationRepository pushNotificationRepository;
    
    // 메인 페이지 그룹 렌더링
    public List<GroupShortDTO> getUserGroups(Long userPk) {
        var memberships = groupMemberRepository
                .findTop8ByUser_UserPkAndIsAcceptedTrueAndIsOutFalseOrderByGroup_GroupPkDesc(userPk);

        return memberships.stream()
                .map(m -> GroupShortDTO.builder()
                        .groupPk(m.getGroup().getGroupPk())
                        .groupName(m.getGroup().getGroupName())
                        .groupImage(m.getGroup().getGroupImage())
                        .build())
                .toList();
    }

    // 메인페이지 월간 계획 렌더링
    public List<MonthlyPlanDTO> getMonthlyPlans(Long userPk, int year, int month) {
        userRepository.findById(userPk)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<GroupMember> memberships = groupMemberRepository
                .findByUser_UserPkAndIsAcceptedTrueAndIsOutFalse(userPk);

        List<Long> groupPks = memberships.stream()
                .map(gm -> gm.getGroup().getGroupPk())
                .toList();

        LocalDate monthStart = LocalDate.of(year, month, 1);
        LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());

        List<Travel> travels = travelRepository
                .findAllByGroup_GroupPkInAndTpStartLessThanEqualAndTpEndGreaterThanEqualOrderByTpStartAsc(groupPks, monthEnd, monthStart);

        return travels.stream()
                .map(t -> MonthlyPlanDTO.builder()
                        .tpStart(t.getTpStart())
                        .tpEnd(t.getTpEnd())
                        .tpTitle(t.getTpTitle())
                        .groupName(t.getGroup().getGroupName())
                        .groupPk(t.getGroup().getGroupPk())
                        .gcCode(t.getGroup().getGcPk().getGcCode())
                        .travelPk(t.getTpPk())
                        .build())
                .toList();
    }

    // 메인페이지 개인정보 렌더링
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

    // 메인 페이지 알림 렌더링
    public List<NotificationPreviewDTO> getNotificationPreview(Long userPk) {
        return pushNotificationRepository.findPreviewByUser(userPk, PageRequest.of(0, 2));
    }
}
