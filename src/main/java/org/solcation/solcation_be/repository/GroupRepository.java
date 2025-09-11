package org.solcation.solcation_be.repository;

import org.solcation.solcation_be.domain.group.dto.GroupInfoDTO;
import org.solcation.solcation_be.domain.group.dto.GroupListDTO;
import org.solcation.solcation_be.entity.Group;
import org.solcation.solcation_be.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    @Query("""
    SELECT 
        g.groupPk,
        g.groupName,
        g.groupImage,
        g.gcPk,
        g.groupLeader,
        g.totalMembers,
        COALESCE(SUM(CASE WHEN t.tpStart > CURRENT_DATE THEN 1 ELSE 0 END), 0)
    FROM Group g
    LEFT JOIN g.travels t
    WHERE g.groupLeader.userId = :userId 
          AND (
               :searchTerm IS NULL
               OR :searchTerm = ''
               OR LOWER(g.groupName) LIKE CONCAT('%', LOWER(:searchTerm), '%')
          )
    GROUP BY g.groupPk, g.groupLeader, g.totalMembers, g.gcPk, g.groupImage
    """)
    List<Object[]> getGroupListWithSearch(@Param("userId") String userId, @Param("searchTerm") String searchTerm);

    @Query("""
    SELECT
      g.groupPk,
      g.groupName,
      g.groupImage,
      g.gcPk,
      g.groupLeader,
      g.totalMembers,
      COALESCE(SUM(CASE WHEN t.tpStart < CURRENT_DATE THEN 1 ELSE 0 END), 0),
      COALESCE(SUM(CASE WHEN t.tpStart > CURRENT_DATE THEN 1 ELSE 0 END), 0)
    FROM Group g
    LEFT JOIN g.travels t
    WHERE g.groupPk = :groupPk
    GROUP BY g.groupPk, g.groupName, g.groupImage, g.gcPk, g.groupLeader, g.totalMembers  
    """)
    Object getGroupInfoByGroupPk(@Param("groupPk") long groupPk);

    @Query("SELECT g.groupLeader FROM Group g WHERE g.groupPk = :groupPk")
    User findGroupLeaderByGroupPk(@Param("groupPk") Long groupPk);

    Group findByGroupPk(Long groupPk);
}