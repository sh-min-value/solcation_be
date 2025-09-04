package org.solcation.solcation_be.domain.travel;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.solcation.solcation_be.domain.travel.dto.PlanDetailDTO;
import org.solcation.solcation_be.domain.travel.dto.TravelReqDTO;
import org.solcation.solcation_be.domain.travel.dto.TravelResDTO;
import org.solcation.solcation_be.entity.enums.TRAVELSTATE;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "여행 계획 컨트롤러")
@RequiredArgsConstructor
@RestController
@RequestMapping("/group/{groupId:\\d+}/travel")
public class TravelController {

    private final TravelService travelService;
    private final PlanDetailService planDetailService;

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
    public Long createTravel(@PathVariable Long groupId, @Valid @ModelAttribute TravelReqDTO dto) {
        dto.setGroupPk(groupId);
        return travelService.create(dto);
    }

    @Operation(summary = "단일 여행 조회")
    @GetMapping("/{travelId}")
    public TravelResDTO getTravel(@PathVariable Long groupId, @PathVariable Long travelId) {
        return travelService.getTravelById(travelId);
    }

    //세부계획
    @Operation(summary = "여행 세부계획 조회")
    @GetMapping("/{travelId}/plans")
    public List<PlanDetailDTO> getTravelPlans(
            @PathVariable Long groupId,
            @PathVariable Long travelId
    ) {
        return planDetailService.getTravelPlans(travelId);
    }

    @Operation(summary = "여행 세부계획 삽입")
    @PostMapping("/{travelId}/insert")
    public PlanDetailDTO insertPlanDetail(
            @PathVariable Long groupId,
            @PathVariable Long travelId,
            @Valid @RequestBody InsertReq req
    ) {
        return planDetailService.insertBetween(
                travelId, req.pdDay(),
                req.prevCrdtId(), req.nextCrdtId(),
                req.pdPlace(), req.pdAddress(), req.pdCost(), req.tcPk(),
                req.clientId(), req.opTs()
        );
    }

    @Operation(summary = "같은 날 내 순서 이동")
    @PostMapping("/{travelId}/move/within")
    public PlanDetailDTO moveWithinDay(
            @PathVariable Long groupId,
            @PathVariable Long travelId,
            @Valid @RequestBody MoveWithinReq req
    ) {
        return planDetailService.moveWithinDay(
                req.crdtId(), req.prevCrdtId(), req.nextCrdtId(),
                req.clientId(), req.opTs()
        );
    }

    @Operation(summary = "다른 날로 이동")
    @PostMapping("/{travelId}/move/day")
    public PlanDetailDTO moveToAnotherDay(
            @PathVariable Long groupId,
            @PathVariable Long travelId,
            @Valid @RequestBody MoveDayReq req
    ) {
        return planDetailService.moveToAnotherDay(
                req.crdtId(), req.newDay(),
                req.prevCrdtId(), req.nextCrdtId(),
                req.clientId(), req.opTs()
        );
    }

    @Operation(summary = "세부계획 내용 수정")
    @PatchMapping("/{travelId}/update")
    public PlanDetailDTO updatePlanDetail(
            @PathVariable Long groupId,
            @PathVariable Long travelId,
            @Valid @RequestBody UpdateReq req
    ) {
        return planDetailService.updateFields(
                req.crdtId(), req.pdPlace(), req.pdAddress(), req.pdCost(), req.tcPk(),
                req.clientId(), req.opTs()
        );
    }

    @Operation(summary = "세부계획 삭제(소프트)")
    @DeleteMapping("/{travelId}/{crdtId}")
    public ResponseEntity<Void> deletePlanDetail(
            @PathVariable Long groupId,
            @PathVariable Long travelId,
            @PathVariable String crdtId,
            @RequestParam String clientId,
            @RequestParam Long opTs
    ) {
        planDetailService.softDelete(crdtId, clientId, opTs);
        return ResponseEntity.noContent().build();
    }
}