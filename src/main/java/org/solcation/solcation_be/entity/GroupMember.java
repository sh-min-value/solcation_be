package org.solcation.solcation_be.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "group_member_tb")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class GroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "gm_pk")
    private Long gmPk;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_pk", referencedColumnName = "group_pk", nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_pk", referencedColumnName = "user_pk", nullable = false)
    private User user;

    @Column(name = "role", nullable = false)
    private Boolean role;

    @Column(name = "is_accepted", nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean isAccepted;

    @Column(name = "is_out", nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean isOut;

    @Column(name = "has_card", nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean hasCard;

}
