package org.solcation.solcation_be.repository;

import org.solcation.solcation_be.domain.group.dto.GroupListDTO;
import org.solcation.solcation_be.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

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
    GROUP BY g.groupPk, g.groupLeader, g.totalMembers, g.gcPk, g.groupImage
    """)
    List<Object[]> getGroupList(@Param("userId") String userId);

}