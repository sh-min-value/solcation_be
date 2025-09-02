package org.solcation.solcation_be.domain.travel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TravelReqDTO {
    @Schema(description = "국가", example = "대한민국")
    @NotBlank(message = "국가를 선택해주세요")
    @Size(max = 60)
    private String country;

    @Schema(description = "도시", example = "제주")
    @NotBlank(message = "도시를 선택해주세요")
    @Size(max = 60)
    private String city;

    @Schema(description = "여행 제목", example = "제주도 2박3일")
    @NotBlank(message = "여행 제목을 입력해주세요")
    @Size(max = 100)
    private String title;

    @Schema(description = "시작일", example = "2025-09-20")
    @NotNull(message = "시작일을 선택해주세요")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    @Schema(description = "종료일", example = "2025-09-22")
    @NotNull(message = "종료일을 선택해주세요")
    @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;

    @Schema(description = "테마(카테고리명)", example = "힐링")
    @NotNull(message = "테마를 선택해주세요")
    @NotBlank @Size(max = 50)
    private String theme;

    @Schema(description = "대표 사진 파일")
    @NotNull(message = "이미지를 선택해주세요")
    private MultipartFile photo;
}

