package org.solcation.solcation_be.entity;

import jakarta.persistence.*;
import lombok.*;
import org.solcation.solcation_be.util.security.AesGcmAttributeConverter;

import java.time.LocalDateTime;

@Entity
@Table(name = "shared_account_card_tb")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sac_pk")
    private Long sacPk;

    @Convert(converter = AesGcmAttributeConverter.class)
    @Column(name = "sac_num", nullable = false, length = 20)
    private String sacNum;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sa_pk", referencedColumnName = "sa_pk")
    private SharedAccount saPk;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expiration_period", nullable = false)
    private LocalDateTime expirationPeriod;

    @Convert(converter = AesGcmAttributeConverter.class)
    @Column(name = "sac_cvc", nullable = false, length = 3)
    private String cvc;

    @Convert(converter = AesGcmAttributeConverter.class)
    @Column(name = "sac_pw", nullable = false, length = 6)
    private String pw;

    @Column(name = "cancellation", nullable = false)
    private Boolean cancellation;

    @Column(name = "cancellation_date", nullable = true)
    private LocalDateTime cancellationDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gm_pk", referencedColumnName = "gm_pk")
    private GroupMember gmPk;
}
