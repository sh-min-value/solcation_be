package org.solcation.solcation_be.repository.main;

import org.solcation.solcation_be.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupShortRepository extends JpaRepository<Group, Long> {

    List<Group> findTop8ByGroupLeaderOrderByGroupPkDesc(Long userPk);
}