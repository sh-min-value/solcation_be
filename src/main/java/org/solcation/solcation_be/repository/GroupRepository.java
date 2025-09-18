package org.solcation.solcation_be.repository;

import org.solcation.solcation_be.domain.group.dto.GroupInfoDTO;
import org.solcation.solcation_be.domain.group.dto.GroupListDTO;
import org.solcation.solcation_be.domain.group.dto.GroupMemberDTO;
import org.solcation.solcation_be.entity.Group;
import org.solcation.solcation_be.entity.User;
import org.solcation.solcation_be.entity.enums.TRAVELSTATE;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    @Query("""
    select new org.solcation.solcation_be.domain.group.dto.GroupListDTO(
        g.groupPk,
        g.groupName,
        g.groupImage,
        gc.gcCode,
        leader.userName,
        g.totalMembers,
        (select count(t2) from Travel t2 where t2.group = g and t2.tpState = :state)
    )
    FROM Group g
    JOIN g.gcPk gc
    JOIN g.groupLeader leader
    WHERE
        exists (
          select 1
          from GroupMember gm
          where gm.group = g
            and gm.user.userId = :userId
            and gm.isAccepted = true
        )
          AND (
               :searchTerm IS NULL
               OR :searchTerm = ''
               OR LOWER(g.groupName) LIKE CONCAT('%', LOWER(:searchTerm), '%')
          )
    ORDER BY g.groupPk desc
    """)
    List<GroupListDTO> getGroupListWithSearch(@Param("userId") String userId, @Param("searchTerm") String searchTerm, @Param("state") TRAVELSTATE state);

    @Query("""
    select new org.solcation.solcation_be.domain.group.dto.GroupInfoDTO(
      g.groupPk,
      g.groupName,
      g.groupImage,
      gc.gcCode,
      leader.userName,
      g.totalMembers,
      (select count(t2) from Travel t2 where t2.group = g and t2.tpState = :beforeState),
      (select count(t2) from Travel t2 where t2.group = g and t2.tpState = :finishState),
      (select count(gm) from GroupMember gm where gm.group = g and gm.isAccepted is null)
    )
    FROM Group g
    JOIN g.gcPk gc
    JOIN g.groupLeader leader
    WHERE g.groupPk = :groupPk
    GROUP BY g.groupPk, g.groupName, g.groupImage, g.gcPk, g.groupLeader, g.totalMembers  
    """)
    GroupInfoDTO getGroupInfoByGroupPk(@Param("groupPk") long groupPk, @Param("beforeState") TRAVELSTATE beforeState, @Param("finishState") TRAVELSTATE finishState);

    Group findByGroupPk(Long groupPk);
}