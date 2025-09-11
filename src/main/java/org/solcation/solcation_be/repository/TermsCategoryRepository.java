package org.solcation.solcation_be.repository;

import org.solcation.solcation_be.entity.TermsCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TermsCategoryRepository extends JpaRepository<TermsCategory, Long> {
    Optional<Object> findByTermsCode(String code);
}
