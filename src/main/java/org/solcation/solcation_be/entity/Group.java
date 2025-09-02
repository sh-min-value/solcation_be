package org.solcation.solcation_be.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "group_tb")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "group_pk")
    private Long groupPk;

    @Column(name = "group_name", nullable = false, length = 100)
    private String groupName;

    @Column(name = "group_image")
    private String groupImage;

    @Column(name = "total_members", nullable = false)
    private Integer totalMembers;

    @Column(name = "is_created", nullable = false)
    private Boolean isCreated;

    @Column(name = "signature_url")
    private String signatureUrl;

    @Column(name = "gc_pk", nullable = false)
    private Long gcPk;

    @Column(name = "group_leader", nullable = false)
    private Long groupLeader;
}
