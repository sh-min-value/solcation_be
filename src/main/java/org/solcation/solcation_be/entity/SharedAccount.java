package org.solcation.solcation_be.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.Instant;

import lombok.*;

import org.solcation.solcation_be.entity.converter.DepositCycleConverter;
import org.solcation.solcation_be.entity.converter.DepositDayConverter;
import org.solcation.solcation_be.entity.enums.DEPOSITCYCLE;
import org.solcation.solcation_be.entity.enums.DEPOSITDAY;
import org.solcation.solcation_be.util.security.AesGcmAttributeConverter;
import org.springframework.data.domain.Auditable;


@Entity
@Table(name = "shared_account_tb")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SharedAccount extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sa_pk")
    private Long saPk;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_pk", referencedColumnName = "group_pk")
    private Group group;

    @Column(name = "balance", nullable = false)
    private int balance;

    @Column(name = "deposit_alarm", nullable = false)
    private Boolean depositAlarm;

    @Column(name = "deposit_cycle", nullable = true)
    @Convert(converter = DepositCycleConverter.class)
    private DEPOSITCYCLE depositCycle;

    @Column(name = "deposit_date", nullable = true)
    private Integer depositDate;

    @Column(name = "deposit_day", nullable = true)
    @Convert(converter = DepositDayConverter.class)
    private DEPOSITDAY depositDay;

    @Column(name = "deposit_amount", nullable = true)
    private int depositAmount;

    @Convert(converter = AesGcmAttributeConverter.class)
    @Column(name = "account_num", nullable = false)
    private String accountNum;

    @Convert(converter = AesGcmAttributeConverter.class)
    @Column(name = "sa_pw", nullable = false)
    private String saPw;

    @Column(name="created_at")
    private Instant createdAt;

    public void disableAlarm() {
        this.depositAlarm = false;
        this.depositCycle = null;
        this.depositDate = null;
        this.depositDay = null;
        this.depositAmount = 0;
    }
}
