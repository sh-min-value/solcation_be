package org.solcation.solcation_be.domain.main.service;

import lombok.RequiredArgsConstructor;
import org.solcation.solcation_be.common.CustomException;
import org.solcation.solcation_be.common.ErrorCode;
import org.solcation.solcation_be.domain.main.dto.MonthlyPlanDTO;
import org.solcation.solcation_be.entity.GroupMember;
import org.solcation.solcation_be.entity.Travel;
import org.solcation.solcation_be.repository.GroupMemberRepository;
import org.solcation.solcation_be.repository.TravelRepository;
import org.solcation.solcation_be.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MonthlyPlanService {

    private final UserRepository userRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final TravelRepository travelRepository;

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
                        .gcIcon(t.getGroup().getGcPk().getGcIcon())
                        .build())
                .toList();
    }
}
