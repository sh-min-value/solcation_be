package org.solcation.solcation_be.entity;

import jakarta.persistence.*;
import lombok.*;
import org.solcation.solcation_be.entity.converter.TravelStateConverter;

import java.time.LocalDate;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
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

    @Column(name = "pd_order", nullable = false)
    private int pdOrder;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tp_pk", nullable = false)
    private Travel travel;

}
