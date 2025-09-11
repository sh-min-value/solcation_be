package org.solcation.solcation_be.repository;

import org.solcation.solcation_be.entity.Group;
import org.solcation.solcation_be.entity.SharedAccount;
import org.solcation.solcation_be.entity.enums.DEPOSITCYCLE;
import org.solcation.solcation_be.entity.enums.DEPOSITDAY;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface SharedAccountRepository extends JpaRepository<SharedAccount, Long> {
    //그룹의 계좌 정보 조회
    Optional<SharedAccount> findByGroup(@Param("group") Group group);

    SharedAccount findByGroup_GroupPk(Long groupId);
    @Modifying
    @Transactional
    @Query("UPDATE SharedAccount sa " +
            "SET sa.depositAlarm = :depositAlarm, " +
            "    sa.depositCycle = :depositCycle, " +
            "    sa.depositDate = :depositDate, " +
            "    sa.depositDay = :depositDay, " +
            "    sa.depositAmount = :depositAmount " +
            "WHERE sa.saPk = :saPk")
    void updateDepositCycle(Long saPk, Boolean depositAlarm, DEPOSITCYCLE depositCycle,
                           Integer depositDate, DEPOSITDAY depositDay, int depositAmount);


}
