package org.solcation.solcation_be.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

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

    @Column(name = "group_image", nullable = false)
    private String groupImage;

    @Column(name = "total_members", nullable = false)
    private Integer totalMembers;

    @Column(name = "is_created", nullable = false)
    private Boolean isCreated;

    @Column(name = "signature_url")
    private String signatureUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gc_pk", referencedColumnName = "gc_pk")
    private GroupCategory gcPk;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_leader", referencedColumnName = "user_pk")
    private User groupLeader;

    @Builder.Default
    @OneToMany(mappedBy = "group", fetch = FetchType.LAZY)
    private List<Travel> travels = new ArrayList<>();
}
