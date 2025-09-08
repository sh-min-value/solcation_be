package org.solcation.solcation_be.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.solcation.solcation_be.entity.converter.TravelStateConverter;
import org.solcation.solcation_be.entity.enums.TRAVELSTATE;

import java.time.LocalDate;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Table(name = "travel_plan_tb",
        indexes = {
                @Index(name = "idx_travel_group", columnList = "group_pk"),
                @Index(name = "idx_travel_start", columnList = "tp_start")
        })
public class Travel extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tp_pk")
    private Long tpPk;

    @Column(name = "tp_title", nullable = false, length = 100)
    private String tpTitle;

    @Column(name = "tp_location", nullable = false, length = 100)
    private String tpLocation;

    @Column(name = "tp_start", nullable = false)
    private LocalDate tpStart;

    @Column(name = "tp_end", nullable = false)
    private LocalDate tpEnd;

    @Column(name = "tp_image", nullable = false)
    private String tpImage;

    @Convert(converter = TravelStateConverter.class)
    @Column(name = "tp_state", nullable = false)
    private TRAVELSTATE tpState;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tpc_pk", nullable = false)
    private TravelCategory travelCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_pk", nullable = false)
    private Group group;
}
