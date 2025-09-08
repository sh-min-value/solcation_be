package org.solcation.solcation_be.repository;

import org.solcation.solcation_be.entity.TermsAgreement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TermsAgreementRepository extends JpaRepository<TermsAgreement, Long>  {
}
