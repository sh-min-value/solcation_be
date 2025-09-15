package org.solcation.solcation_be.domain.travel.dto;

import lombok.*;
import org.solcation.solcation_be.entity.PlanDetail;
import org.solcation.solcation_be.entity.TransactionCategory;
import org.solcation.solcation_be.entity.Travel;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanDetailDTO {

    // PK
    private Long pdPk;

    // 소속 Travel (엔티티 대신 id만)
    private Long travelId;

    // 일정 기본 정보
    private int pdDay;
    private String pdPlace;
    private String pdAddress;
    private int pdCost;
    private Long category;
    // 정렬/CRDT 메타
    // BigDecimal → 문자열로 노출(직렬화/정밀도 안전)
    private String position;
    private String crdtId;
    private String clientId;
    private Long opTs;
    private boolean tombstone;

    private String prevCrdtId;
    private String nextCrdtId;
    private int newDay;

    /** 엔티티 → DTO */
    public static PlanDetailDTO entityToDTO(PlanDetail e) {
        return PlanDetailDTO.builder()
                .pdPk(e.getPdPk())
                .travelId(e.getTravel() != null ? e.getTravel().getTpPk() : null)
                .pdDay(e.getPdDay())
                .pdPlace(e.getPdPlace())
                .pdAddress(e.getPdAddress())
                .pdCost(e.getPdCost())
                .category(e.getTransactionCategory().getTcPk())
                .position(e.getPosition() != null ? e.getPosition().toPlainString() : null)
                .crdtId(e.getCrdtId())
                .clientId(e.getClientId())
                .opTs(e.getOpTs())
                .tombstone(e.isTombstone())
                .build();
    }
}
