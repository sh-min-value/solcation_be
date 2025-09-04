package org.solcation.solcation_be.repository;

import org.solcation.solcation_be.entity.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    // 그룹 pk 기준으로 8개만 찾기
    List<GroupMember> findTop8ByUser_UserPkAndIsAcceptedTrueAndIsOutFalseOrderByGroup_GroupPkDesc(Long userPk);

    boolean existsByGroup_GroupPkAndUser_UserPkAndIsAcceptedTrueAndIsOutFalse(Long groupPk, Long userPk);

    // 속한 그룹 전부 찾기
    List<GroupMember> findByUser_UserPkAndIsAcceptedTrueAndIsOutFalse(Long userPk);
}
