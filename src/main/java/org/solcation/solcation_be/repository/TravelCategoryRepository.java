package org.solcation.solcation_be.repository;

import org.solcation.solcation_be.entity.TravelCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TravelCategoryRepository extends JpaRepository<TravelCategory, Long> {
    Optional<TravelCategory> findByTpcCode(String tpcCode);
}
