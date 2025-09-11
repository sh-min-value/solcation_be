package org.solcation.solcation_be.repository;

import org.solcation.solcation_be.entity.TermsAgreement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TermsAgreementRepository extends JpaRepository<TermsAgreement, Long>  {
    List<TermsAgreement> findByGroup_GroupPk(Long groupPk);
}
