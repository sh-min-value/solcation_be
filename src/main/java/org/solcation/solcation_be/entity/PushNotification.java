package org.solcation.solcation_be.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "push_notification_tb")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PushNotification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pn_pk")
    private Long pnPk;

    @Column(name = "pn_title", nullable = false, length = 100)
    private String pnTitle;

    @Column(name = "pn_time", nullable = false)
    private Instant pnTime;

    @Column(name = "pn_content", nullable = false, length = 100)
    private String pnContent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ac_pk", referencedColumnName = "ac_pk")
    private AlarmCategory acPk;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_pk", referencedColumnName = "user_pk")
    private User userPk;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_pk", referencedColumnName = "group_pk")
    private Group groupPk;

    @Column(name = "is_accepted", nullable = false)
    private Boolean isAccepted;

    @Column(name = "read_at", nullable = true)
    private Instant readAt;

    public void updateIsAccepted(Boolean isAccepted, Instant readAt) {
        this.isAccepted = isAccepted;
        this.readAt = readAt;
    }
}
