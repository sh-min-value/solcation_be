package org.solcation.solcation_be.repository;

import org.solcation.solcation_be.entity.GroupCategory;
import org.solcation.solcation_be.entity.TransactionCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionCategoryRepository extends JpaRepository<TransactionCategory, Long> {
}
