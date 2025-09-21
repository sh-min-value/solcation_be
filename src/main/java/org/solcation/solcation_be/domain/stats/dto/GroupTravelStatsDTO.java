package org.solcation.solcation_be.domain.stats.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupTravelStatsDTO {
    private long totalTrips;                  // 총 여행 횟수
    private long totalTripDays;               // 총 여행 기간(일수 합)
    private long totalSpent;                  // 총 지출 금액
    private List<CategoryShare> categoryShares; // 카테고리 비중 (파이차트용)
    private List<CategoryAmount> top3Categories; // 가장 지출이 많은 Top3
    private CategoryAmount leastCategory;       // 가장 적게 쓴 카테고리

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategoryShare {
        private Long tcPk;
        private String name;
        private String code;
        private long amount;
        private double ratio; // 전체 대비 %
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategoryAmount {
        private Long tcPk;
        private String name;
        private String code;
        private long amount;
    }
}
