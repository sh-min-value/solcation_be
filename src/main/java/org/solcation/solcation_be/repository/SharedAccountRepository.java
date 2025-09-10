package org.solcation.solcation_be.repository;

import org.solcation.solcation_be.entity.Group;
import org.solcation.solcation_be.entity.SharedAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SharedAccountRepository extends JpaRepository<SharedAccount, Long> {
    //그룹의 계좌 정보 조회
    Optional<SharedAccount> findByGroup(@Param("group") Group group);
}
