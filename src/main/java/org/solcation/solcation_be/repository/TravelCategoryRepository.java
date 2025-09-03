package org.solcation.solcation_be.repository;

import org.solcation.solcation_be.entity.TRAVELSTATE;
import org.solcation.solcation_be.entity.Travel;
import org.solcation.solcation_be.entity.TravelCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TravelCategoryRepository extends JpaRepository<TravelCategory, Long> {
}
