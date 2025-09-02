package org.solcation.solcation_be.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
    @Column(name = "tp_pk")
    public Long tpPk;

    @Column(
            name = "tp_title",
            nullable = false,
            length = 100
    )
    public String tpTitle;

    @Column(
            name = "tp_start",
            nullable = false
    )
    public LocalDate tpStart;

    @Column(
            name = "tp_end",
            nullable = false
    )
    public LocalDate tpEnd;

    @Column(
            name = "tp_image",
            nullable = false
    )
    public String tpImage;

    @Convert(converter = TravelStateConverter.class)
    public TRAVELSTATE tpState;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tpc_pk", nullable = false)
    public TravelCategory tpcPk;



}
