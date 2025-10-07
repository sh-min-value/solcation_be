package org.solcation.solcation_be.scheduler.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.solcation.solcation_be.entity.enums.TRAVELSTATE;

import java.time.LocalDate;

@Schema(name = "여행계획 DTO")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TravelDTO {
    private Long pk;
    private String title;
    private String location;
    private LocalDate startDate;
    private LocalDate endDate;
    private String thumbnail;
    private Integer state;
    private Long categoryId;
    private String categoryName;
    private int participant;
}
