package org.solcation.solcation_be.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.solcation.solcation_be.entity.converter.TravelStateConverter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
public class Travel {
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

    @OneToMany(mappedBy = "travel", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("pdDay ASC, pdOrder ASC")
    private List<PlanDetail> details = new ArrayList<>();

    public void addDetail(PlanDetail d) {
        details.add(d);
        d.setTravel(this);
    }
    public void removeDetail(PlanDetail d) {
        details.remove(d);
        d.setTravel(null);
    }

    // 비즈니스 규칙 체크 예시
    public void validatePeriod() {
        if (tpEnd.isBefore(tpStart)) {
            throw new IllegalArgumentException("여행 종료일은 시작일 이후여야 합니다.");
        }
    }

}
