package org.solcation.solcation_be.entity;

import jakarta.persistence.*;
import lombok.*;
import org.solcation.solcation_be.entity.converter.TransactionTypeConverter;
import org.solcation.solcation_be.entity.enums.TRANSACTIONTYPE;

import java.time.LocalDateTime;

@Entity
@Table(name = "shared_account_transaction_tb")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sat_pk")
    private Long satPk;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sa_pk", referencedColumnName = "sa_pk")
    private SharedAccount saPk;

    @Column(name = "sat_time", nullable = false)
    private LocalDateTime satTime;

    @Column(name = "transaction_type", nullable = true)
    @Convert(converter = TransactionTypeConverter.class)
    private TRANSACTIONTYPE transactionType;

    @Column(name = "deposit_destination", nullable = true, length = 50)
    private String depositDestination;

    @Column(name = "withdraw_destination", nullable = true, length = 50)
    private String withdrawDestination;

    @Column(name = "sat_amount", nullable = false)
    private int satAmount;

    @Column(name = "briefs", nullable = false, length = 20)
    private String briefs;

    @Column(name = "balance", nullable = false)
    private int balance;

    @Column(name = "sat_memo", nullable = true, length = 50)
    private String satMemo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_pk", referencedColumnName = "user_pk")
    private User userPk;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tc_pk", referencedColumnName = "tc_pk")
    private TransactionCategory tcPk;

    @Column(name = "sat_location", nullable = false, length = 3)
    private String satLocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sac_pk", referencedColumnName = "sac_pk")
    private Card sacPk;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gm_pk", referencedColumnName = "gm_pk")
    private GroupMember gmPk;
}
