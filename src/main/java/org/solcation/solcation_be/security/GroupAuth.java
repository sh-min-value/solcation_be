package org.solcation.solcation_be.security;

import lombok.RequiredArgsConstructor;
import org.solcation.solcation_be.repository.GroupMemberRepository;
import org.springframework.stereotype.Component;

@Component("groupAuth")
@RequiredArgsConstructor
public class GroupAuth {
    private final GroupMemberRepository groupMemberRepository;

    public boolean memberOf(Long groupPk, Long userPk) {
        return groupMemberRepository.existsByGroup_GroupPkAndUser_UserPkAndIsAcceptedTrueAndIsOutFalse(groupPk, userPk);
    }
}
