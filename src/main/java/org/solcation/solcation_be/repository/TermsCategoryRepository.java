package org.solcation.solcation_be.repository;

import org.solcation.solcation_be.domain.category.dto.TermsCategoryDTO;
import org.solcation.solcation_be.entity.TermsCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TermsCategoryRepository extends JpaRepository<TermsCategory, Long> {
    Optional<Object> findByTermsCode(String code);

    @Query("""
    select org.solcation.solcation_be.domain.category.dto.TermsCategoryDTO(
        tc.termsPk,
        tc.termsCode
    )
    FROM TermsCategory tc
    """)
    List<TermsCategoryDTO> getCategories();
}
