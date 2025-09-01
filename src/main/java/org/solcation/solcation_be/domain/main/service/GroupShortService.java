package org.solcation.solcation_be.domain.main.service;

import lombok.RequiredArgsConstructor;
import org.solcation.solcation_be.domain.main.dto.GroupShortDTO;
import org.solcation.solcation_be.entity.Group;
import org.solcation.solcation_be.repository.main.GroupShortRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupShortService {

    private final GroupShortRepository groupShortRepository;

    public List<GroupShortDTO> getUserGroups(Long userPk, int limit) {
        List<Group> groups = groupShortRepository
                .findTop8ByGroupLeaderOrderByGroupPkDesc(userPk);

        return groups.stream()
                .limit(limit)
                .map(g -> GroupShortDTO.builder()
                        .groupPk(g.getGroupPk())
                        .groupName(g.getGroupName())
                        .groupImage(g.getGroupImage())
                        .categoryName("임시카테고리") // gc_pk → category name 변환은 추후
                        .totalMembers(g.getTotalMembers())
                        .build())
                .collect(Collectors.toList());
    }
}