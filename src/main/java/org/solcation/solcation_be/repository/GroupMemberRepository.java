package org.solcation.solcation_be.repository;

import org.solcation.solcation_be.entity.Group;
import org.solcation.solcation_be.entity.GroupMember;
import org.solcation.solcation_be.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    List<GroupMember> findTop8ByUser_UserPkAndIsAcceptedTrueAndIsOutFalseOrderByGroup_GroupPkDesc(Long userPk);
    boolean existsByGroup_GroupPkAndUser_UserPkAndIsAcceptedTrueAndIsOutFalse(Long groupPk, Long userPk);

    // 속한 그룹 전부 찾기
    List<GroupMember> findByUser_UserPkAndIsAcceptedTrueAndIsOutFalse(Long userPk);

    @Query("""
    SELECT g.user
    FROM GroupMember g
    WHERE g.group.groupPk = :groupPk AND g.role = :role AND g.isAccepted = :isAccepted
    ORDER BY g.user.userPk ASC
    """)
    List<User> findByGroup_GroupPkAndRoleAndIsAcceptedOrderByUser_UserPkAsc(@Param("groupPk") Long groupPk, @Param("role") Boolean role, @Param("isAccepted") Boolean isAccepted);

    @Query("""
    SELECT g.user
    FROM GroupMember g
    WHERE g.group.groupPk = :groupPk AND g.isAccepted is null
    ORDER BY g.user.userPk ASC
    """)
    List<User> findByGroup_GroupPkAndPending(@Param("groupPk") Long groupPk);

    @Query("""
    SELECT g.user
    FROM GroupMember g
    WHERE g.group.groupPk = :groupPk AND (g.isAccepted = true OR g.isAccepted is null)
    ORDER BY g.user.userPk ASC
    """)
    List<User> findByGroup_GroupPkAndNotRejected(@Param("groupPk") Long groupPk);

    @Query("""
    SELECT COALESCE(COUNT(g), 0)
    FROM GroupMember g
    WHERE g.group.groupPk = :groupPk AND (g.isAccepted = true OR g.isAccepted is null)
    """)
    Long findTotalNumByGroup_GroupPkAndNotRejected(@Param("groupPk") Long groupPk);

    GroupMember findByUserAndGroup(@Param("user") User user, @Param("group") Group group);

    Optional<GroupMember> findByGroup_GroupPkAndUser_UserPkAndIsAcceptedIsNull(@Param("groupPk") Long groupPk, @Param("userPk") Long userPk);

    // 그룹 멤버 수
    long countByGroup_GroupPkAndIsAcceptedTrueAndIsOutFalse(Long groupPk);
}