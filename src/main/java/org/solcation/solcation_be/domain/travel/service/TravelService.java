package org.solcation.solcation_be.domain.travel.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.solcation.solcation_be.common.CustomException;
import org.solcation.solcation_be.common.ErrorCode;
import org.solcation.solcation_be.domain.travel.dto.PlanDetailDTO;
import org.solcation.solcation_be.domain.travel.dto.TravelReqDTO;
import org.solcation.solcation_be.domain.travel.dto.TravelResDTO;
import org.solcation.solcation_be.entity.*;
import org.solcation.solcation_be.entity.enums.TRAVELSTATE;
import org.solcation.solcation_be.repository.GroupRepository;
import org.solcation.solcation_be.repository.PlanDetailRepository;
import org.solcation.solcation_be.repository.TravelCategoryRepository;
import org.solcation.solcation_be.repository.TravelRepository;
import org.solcation.solcation_be.util.s3.S3Utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TravelService {
    private final TravelRepository travelRepository;
    private final TravelCategoryRepository travelCategoryRepository;
    private final GroupRepository groupRepository;
    private final PlanDetailRepository planDetailRepository;
    private final S3Utils s3Utils;

    @Value("${cloud.s3.bucket.upload.profile.travel}")
    private String UPLOAD_PATH;

    // 여행 목록 조회 ( filter )
    public List<TravelResDTO> getTravelsByGroupAndStatus(Long groupPk, TRAVELSTATE state) {
        List<Travel> travels = travelRepository.findAllByGroup_GroupPkAndTpStateOrderByTpStartDesc(groupPk, state);
        return travels.stream().map(this::toDto).toList();
    }
    // 여행 목록 조회
    public List<TravelResDTO> getTravelsByGroup(Long groupPk) {
        List<Travel> travels = travelRepository.findAllByGroup_GroupPkOrderByTpStartDesc(groupPk);
        return travels.stream().map(this::toDto).toList();
    }

    // 여행 생성
    @Transactional
    public Long create(TravelReqDTO dto) {
        Group group = groupRepository.findById(dto.getGroupPk())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_GROUP));
        TravelCategory category = travelCategoryRepository.findById(dto.getCategoryPk())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_CATEGORY));

        String location = dto.getCountry() + " " + dto.getCity();

        MultipartFile photo = dto.getPhoto();

        //확장자 확인(png, jpeg, jpg)
        String originalFilename = photo.getOriginalFilename();

        if(!s3Utils.checkExtension(originalFilename)){
            throw new CustomException(ErrorCode.UNSUPPORTED_MEDIA_TYPE);
        }

        //이미지 업로드
        String filename = s3Utils.uploadObject(photo, originalFilename, UPLOAD_PATH);

        //DB 실패 시 이미지 삭제
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if(status == TransactionSynchronization.STATUS_ROLLED_BACK) {
                    try { s3Utils.deleteObject(filename, UPLOAD_PATH); } catch (Exception ignore) {}
                }
                TransactionSynchronization.super.afterCompletion(status);
            }
        });

        Travel travel = Travel.builder()
                .tpTitle(dto.getTitle())
                .tpLocation(location)
                .tpStart(dto.getStartDate())
                .tpEnd(dto.getEndDate())
                .tpImage(filename)
                .tpState(TRAVELSTATE.BEFORE)
                .travelCategory(category)
                .group(group)
                .participant(dto.getParticipant())
                .build();

        return travelRepository.save(travel).getTpPk();
    }

    // 단일 여행 조회
    public TravelResDTO getTravelById(Long travelPk) {
        return travelRepository.findById(travelPk)
                .map(this::toDto)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_PLAN));
    }

    // 세부계획 조회
    @Transactional(readOnly = true)
    public List<PlanDetailDTO> getTravelPlans(Long travelPk) {
        return planDetailRepository.findAliveByTravelOrderByPdDayAscPositionAsc(travelPk)
                .stream().map(PlanDetailDTO::entityToDTO).toList();
    }

    private TravelResDTO toDto(Travel t) {
        return TravelResDTO.builder()
                .pk(t.getTpPk())
                .title(t.getTpTitle())
                .location(t.getTpLocation())
                .startDate(t.getTpStart())
                .endDate(t.getTpEnd())
                .thumbnail(t.getTpImage())
                .state(t.getTpState().getLabel())
                .categoryId(t.getTravelCategory().getTpcPk())
                .categoryName(t.getTravelCategory().getTpcName())
                .participant(t.getParticipant())
                .build();
    }

}
