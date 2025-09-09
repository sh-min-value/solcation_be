package org.solcation.solcation_be.entity;

import jakarta.persistence.*;
import lombok.*;
import org.solcation.solcation_be.entity.converter.DepositCycleConverter;
import org.solcation.solcation_be.entity.converter.DepositDayConverter;
import org.solcation.solcation_be.entity.converter.UserRoleConverter;
import org.solcation.solcation_be.entity.enums.DEPOSITCYCLE;
import org.solcation.solcation_be.util.security.AesGcmAttributeConverter;

import java.time.LocalDateTime;

@Entity
@Table(name = "shared_account_tb")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SharedAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sa_pk")
    private Long saPk;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_pk", referencedColumnName = "group_pk")
    private Group group;

    @Column(name = "balance", nullable = false)
    private int balance;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "deposit_alarm", nullable = false)
    private Boolean depositAlarm;

    @Column(name = "deposit_cycle", nullable = true)
    @Convert(converter = DepositCycleConverter.class)
    private DEPOSITCYCLE depositCycle;

    @Column(name = "deposit_date", nullable = true)
    private LocalDateTime depositDate;

    @Column(name = "deposit_day", nullable = true)
    @Convert(converter = DepositDayConverter.class)
    private DEPOSITCYCLE depositDay;

    @Column(name = "deposit_amount", nullable = true)
    private int depositAmount;

    @Convert(converter = AesGcmAttributeConverter.class)
    @Column(name = "account_num", nullable = false, length = 20)
    private String accountNum;

    @Convert(converter = AesGcmAttributeConverter.class)
    @Column(name = "sa_pw", nullable = false, length = 6)
    private String saPw;
}
