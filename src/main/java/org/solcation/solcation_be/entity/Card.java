package org.solcation.solcation_be.entity;

import jakarta.persistence.*;
import lombok.*;
import org.solcation.solcation_be.util.security.AesGcmAttributeConverter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Entity
@Table(name = "shared_account_card_tb")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Card extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sac_pk")
    private Long sacPk;

    @Convert(converter = AesGcmAttributeConverter.class)
    @Column(name = "sac_num", nullable = false)
    private String sacNum;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sa_pk", referencedColumnName = "sa_pk")
    private SharedAccount saPk;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Convert(converter = AesGcmAttributeConverter.class)
    @Column(name = "sac_cvc", nullable = false)
    private String cvc;

    @Convert(converter = AesGcmAttributeConverter.class)
    @Column(name = "sac_pw", nullable = false)
    private String pw;

    @Column(name = "cancellation", nullable = false)
    private Boolean cancellation;

    @Column(name = "cancellation_date", nullable = true)
    private Instant cancellationDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gm_pk", referencedColumnName = "gm_pk")
    private GroupMember gmPk;

    @Column(name = "expiration_year")
    private short expirationYear;

    @Column(name = "expiration_month")
    private byte expirationMonth;

    /* insert/update 전 유효성 검사 */
    @PrePersist
    @PreUpdate
    private void setExpiry() {
        if(expirationMonth < 1 || expirationMonth > 12) throw new IllegalArgumentException("expirationMonth out of range");
    }

    public YearMonth getExpiration() {
        return YearMonth.of(expirationYear, expirationMonth);
    }

    public void setExpiration(YearMonth expiration) {
        this.expirationYear = (short) expiration.getYear();
        this.expirationMonth = (byte) expiration.getMonthValue();
    }

    public void updateCancellation() {
        this.cancellation = true;
        this.cancellationDate = Instant.now();
    }
}
