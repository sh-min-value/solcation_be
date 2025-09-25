package org.solcation.solcation_be.domain.travel;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.solcation.solcation_be.domain.travel.service.SnapshotCommitService;
import org.solcation.solcation_be.domain.travel.service.TravelService;
import org.solcation.solcation_be.entity.enums.TRAVELSTATE;
import org.solcation.solcation_be.domain.travel.dto.PlanDetailDTO;
import org.solcation.solcation_be.domain.travel.dto.TravelReqDTO;
import org.solcation.solcation_be.domain.travel.dto.TravelResDTO;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "여행 계획 컨트롤러")
@RequiredArgsConstructor
@RestController
@RequestMapping("api/group/{groupId:\\d+}/travel")
public class TravelController {

    private final TravelService travelService;

    @Operation(summary = "그룹 여행 조회", description = "status가 없으면 전체, 있으면 상태별 필터")
    @GetMapping("/list")
    public List<TravelResDTO> getTravels(
            @PathVariable Long groupId,
            @RequestParam(name = "status", required = false) TRAVELSTATE status
    ) {
        if (status == null) {
            return travelService.getTravelsByGroup(groupId);
        }
        return travelService.getTravelsByGroupAndStatus(groupId, status);
    }

    @Operation(summary = "그룹 여행 생성")
    @PostMapping(value = "/new", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void createTravel(@PathVariable Long groupId, @Valid @ModelAttribute TravelReqDTO dto) {
        dto.setGroupPk(groupId);
        travelService.create(dto);
    }

    @Operation(summary = "단일 여행 조회")
    @GetMapping("/{tpPk:\\d+}")
    public TravelResDTO getTravel(@PathVariable Long groupId, @PathVariable Long tpPk) {
        return travelService.getTravelById(tpPk);
    }

    //세부계획
    @Operation(summary = "여행 세부계획 조회")
    @GetMapping("/{travelId}/plans")
    public List<PlanDetailDTO> getTravelPlans(
            @PathVariable Long groupId,
            @PathVariable Long travelId
    ) {
        return travelService.getTravelPlans(travelId);
    }

    @Operation(summary = "여행 삭제")
    @DeleteMapping("/{travelId}")
    public void deleteTravel(@PathVariable Long groupId, @PathVariable Long travelId){
        travelService.deleteTravel(travelId);
        return;
    }
}