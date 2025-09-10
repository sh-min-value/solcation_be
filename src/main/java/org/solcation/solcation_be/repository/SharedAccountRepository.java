package org.solcation.solcation_be.repository;

import org.solcation.solcation_be.domain.wallet.account.dto.DepositCycleDTO;
import org.solcation.solcation_be.entity.SharedAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SharedAccountRepository extends JpaRepository<SharedAccount, Long> {
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
    int updateDepositCycle(Long saPk, Boolean depositAlarm, String depositCycle,
                           LocalDateTime depositDate, String depositDay, int depositAmount);


}
