package org.solcation.solcation_be.repository;

import org.solcation.solcation_be.entity.AlarmCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AlarmCategoryRepository extends JpaRepository<AlarmCategory,Long> {
    Optional<AlarmCategory> findByAcName(String acName);
}
