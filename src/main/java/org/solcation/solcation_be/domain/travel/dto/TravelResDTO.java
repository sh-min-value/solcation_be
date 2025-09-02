package org.solcation.solcation_be.domain.travel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Schema(name = "여행계획 응답 DTO")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TravelResDTO {
    private Long pk;
    private String title;
    private String location;
    private LocalDate startDate;
    private LocalDate endDate;
    private String thumbnail;
    private String state;
    private Long categoryId;
    private String categoryName;



}
