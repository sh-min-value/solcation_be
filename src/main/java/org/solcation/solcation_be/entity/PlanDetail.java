package org.solcation.solcation_be.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "plan_detail_tb")
public class PlanDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pd_pk")
    private Long pdPk;

    @Column(name = "pd_place", nullable = false, length = 100)
    private String pdPlace;

    @Column(name = "pd_address", nullable = false, length = 100)
    private String pdAddress;

    @Column(name = "pd_cost", nullable = false)
    private int pdCost;

    @Column(name = "pd_day", nullable = false)
    private int pdDay;

    @Column(name = "pd_order", precision = 38, scale = 18, nullable = false)
    private BigDecimal position;

    // CRDT 메타
    @Column(name = "crdt_id", nullable = false, unique = true, length = 64)
    private String crdtId;              // UUID + clientId 조합 (멱등키)

    @Column(name = "client_id", nullable = false, length = 64)
    private String clientId;            // 생성/변경 주체

    @Column(name = "op_ts", nullable = false)
    private Long opTs;

    @Column(name = "tombstone", nullable = false)
    private boolean tombstone;          // 소프트 삭제

    @Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tp_pk", nullable = false)
    private Travel travel;
}