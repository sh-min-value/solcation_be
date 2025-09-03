package org.solcation.solcation_be.domain.main.service;

import lombok.RequiredArgsConstructor;
import org.solcation.solcation_be.domain.main.dto.GroupShortDTO;
import org.springframework.stereotype.Service;
import org.solcation.solcation_be.repository.GroupMemberRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GroupShortService {

    private final GroupMemberRepository groupMemberRepository;

    public List<GroupShortDTO> getUserGroups(Long userPk) {
        var memberships = groupMemberRepository
                .findTop8ByUser_UserPkAndIsAcceptedTrueAndIsOutFalseOrderByGroup_GroupPkDesc(userPk);

        return memberships.stream()
                .map(m -> GroupShortDTO.builder()
                        .groupPk(m.getGroup().getGroupPk())
                        .groupName(m.getGroup().getGroupName())
                        .groupImage(m.getGroup().getGroupImage())
                        .build()
                )
                .toList();
    }
}
