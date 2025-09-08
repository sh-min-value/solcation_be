package org.solcation.solcation_be.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "terms_agreement_tb")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TermsAgreement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ta_pk")
    private Long taPk;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terms_pk", referencedColumnName = "terms_pk")
    private TermsCategory termsPk;

    @Column(name = "is_agree", nullable = false)
    private Boolean isAgree;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_pk", referencedColumnName = "group_pk", nullable = false)
    private Group group;
}
