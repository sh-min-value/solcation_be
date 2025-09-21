package org.solcation.solcation_be.domain.category;

import lombok.RequiredArgsConstructor;
import org.solcation.solcation_be.domain.category.dto.AlarmCategoryDTO;
import org.solcation.solcation_be.domain.category.dto.GroupCategoryDTO;
import org.solcation.solcation_be.domain.category.dto.TermsCategoryDTO;
import org.solcation.solcation_be.domain.category.dto.TravelPlanCategoryDTO;
import org.solcation.solcation_be.domain.wallet.transaction.dto.TransactionCategoryDTO;
import org.solcation.solcation_be.entity.AlarmCategory;
import org.solcation.solcation_be.entity.GroupCategory;
import org.solcation.solcation_be.entity.TransactionCategory;
import org.solcation.solcation_be.entity.TravelCategory;
import org.solcation.solcation_be.repository.TermsCategoryRepository;
import org.solcation.solcation_be.util.category.AlarmCategoryLookup;
import org.solcation.solcation_be.util.category.GroupCategoryLookup;
import org.solcation.solcation_be.util.category.TransactionCategoryLookup;
import org.solcation.solcation_be.util.category.TravelCategoryLookup;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final AlarmCategoryLookup  alarmCategoryLookup;
    private final TravelCategoryLookup travelCategoryLookup;
    private final TermsCategoryRepository termsCategoryRepository;
    private final TransactionCategoryLookup  transactionCategoryLookup;
    private final GroupCategoryLookup groupCategoryLookup;

    /* 알림 카테고리 받아오기 */
    public List<AlarmCategoryDTO> getAlarmCategory() {
        List<AlarmCategory> list = alarmCategoryLookup.getList();
        List<AlarmCategoryDTO> result = new ArrayList<>();

        list.forEach(i -> result.add(
                AlarmCategoryDTO.builder()
                        .acPk(i.getAcPk())
                        .acCode(i.getAcCode())
                        .acDest(i.getAcDest())
                        .build()
        ));

        return result;
    }

    /* 여행 카테고리 받아오기 */
    public List<TravelPlanCategoryDTO> getTravelPlanCategories() {
        List<TravelCategory> travelCategories = travelCategoryLookup.getList();
        List<TravelPlanCategoryDTO> result = new ArrayList<>();

        travelCategories.forEach(i -> result.add(
                TravelPlanCategoryDTO.builder()
                        .tpcPk(i.getTpcPk())
                        .tpcCode(i.getTpcCode())
                        .tpcName(i.getTpcName())
                        .tpcIcon(i.getTpcIcon())
                        .build()
        ));

        return result;
    }

    /* 약관 카테고리 받아오기 */
    public List<TermsCategoryDTO> getTermsCategory() {
        return termsCategoryRepository.getCategories();
    }


    /* 카테고리 목록 렌더링 */
    @Transactional(readOnly = true)
    public List<TransactionCategoryDTO> getTransactionCategories() {
        List<TransactionCategory> list = transactionCategoryLookup.getList();
        List<TransactionCategoryDTO> result = new ArrayList<>();
        list.forEach(i -> result.add(TransactionCategoryDTO.builder()
                .tcPk(i.getTcPk())
                .tcName(i.getTcName())
                .tcIcon(i.getTcIcon())
                .tcCode(i.getTcCode())
                .build()));
        return result;
    }


    /* 그룹 카테고리 조회 */
    public List<GroupCategoryDTO> getGroupCategory() {
        List<GroupCategory> list = groupCategoryLookup.getList();
        List<GroupCategoryDTO> result = new ArrayList<>();

        list.forEach(i -> result.add(
                GroupCategoryDTO.builder()
                        .gcPk(i.getGcPk())
                        .gcCode(i.getGcCode())
                        .gcName(i.getGcName())
                        .gcIcon(i.getGcIcon())
                        .build()
        ));
        return result;
    }
}
