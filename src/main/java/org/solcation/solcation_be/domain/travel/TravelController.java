package org.solcation.solcation_be.domain.travel;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.solcation.solcation_be.domain.travel.dto.TravelResDTO;
import org.solcation.solcation_be.entity.TRAVELSTATE;
import org.solcation.solcation_be.repository.TravelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Tag(name = "여행 계획 컨트롤러")
@RequiredArgsConstructor
@RestController
@RequestMapping("/travel")
public class TravelController {
    private final TravelService travelService;

    @Operation(summary = "그룹 여행 조회", description = "status가 없으면 전체, 있으면 상태별 필터")
    @GetMapping("/{groupId}")
    public List<TravelResDTO> getTravels( @PathVariable Long groupId,
            @RequestParam(name = "status", required = false) TRAVELSTATE status
    ) {
        List<TravelResDTO> result;
        if (status == null) {
            result = travelService.getTravelsByGroup(groupId);
        } else {
            result = travelService.getTravelsByGroupAndStatus(groupId, status);
        }
        return result;
    }
}
