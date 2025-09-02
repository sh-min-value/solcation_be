package org.solcation.solcation_be.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Table(name = "group_category_tb")
public class GroupCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "gc_pk", nullable = false)
    private Long gcPk;

    @Column(name = "gc_name", nullable = false)
    private String gcName;

    @Column(name = "gc_icon", nullable = false)
    private String gcIcon;
}
