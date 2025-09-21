package org.solcation.solcation_be.domain.category;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.solcation.solcation_be.domain.category.dto.AlarmCategoryDTO;
import org.solcation.solcation_be.domain.category.dto.TravelPlanCategoryDTO;
import org.solcation.solcation_be.domain.group.GroupService;
import org.solcation.solcation_be.domain.category.dto.GroupCategoryDTO;
import org.solcation.solcation_be.domain.notification.NotificationService;
import org.solcation.solcation_be.domain.wallet.transaction.TransactionService;
import org.solcation.solcation_be.domain.wallet.transaction.dto.TransactionCategoryDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "카테고리 컨트롤러")
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/categories")
public class CategoryController {
    private final CategoryService categoryService;

    /* 그룹 카테고리 받아오기 */
    @Operation(summary = "그룹 카테고리 조회")
    @GetMapping("/group")
    public List<GroupCategoryDTO> getGroupCategory() {
        return categoryService.getGroupCategory();
    }

    /* 알림 카테고리 받아오기 */
    @Operation(summary = "알림 카테고리 조회")
    @GetMapping("/alarm")
    public List<AlarmCategoryDTO> getAlarmCategory() {
        return categoryService.getAlarmCategory();
    }

    /* 동의사항 카테고리 받아오기 */
    @Operation(summary = "동의사항 카테고리 조회")
    @GetMapping("/terms")
    public List<AlarmCategoryDTO> getTermsCategory() {
        return categoryService.getAlarmCategory();
    }

    /* 거래내역 카테고리 받아오기 */
    @Operation(summary = "거래내역 카테고리 조회")
    @GetMapping("/transaction")
    public List<TransactionCategoryDTO> getTransactionCategory() {
        return categoryService.getTransactionCategories();
    }

    /* 여행 카테고리 받아오기 */
    @Operation(summary = "여행 카테고리 조회")
    @GetMapping("/travel")
    public List<TravelPlanCategoryDTO> getTravelCategory() {
        return categoryService.getTravelPlanCategories();
    }
}
