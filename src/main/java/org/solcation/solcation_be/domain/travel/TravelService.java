package org.solcation.solcation_be.domain.travel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.solcation.solcation_be.common.CustomException;
import org.solcation.solcation_be.common.ErrorCode;
import org.solcation.solcation_be.domain.travel.dto.TravelReqDTO;
import org.solcation.solcation_be.domain.travel.dto.TravelResDTO;
import org.solcation.solcation_be.entity.*;
import org.solcation.solcation_be.entity.enums.TRAVELSTATE;
import org.solcation.solcation_be.repository.GroupRepository;
import org.solcation.solcation_be.repository.TravelCategoryRepository;
import org.solcation.solcation_be.repository.TravelRepository;
import org.solcation.solcation_be.util.s3.S3Utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class TravelService {
    private final TravelRepository travelRepository;
    private final TravelCategoryRepository travelCategoryRepository;
    private final GroupRepository groupRepository;
    private final S3Utils s3Utils;

    @Value("${cloud.s3.bucket.upload.profile.travel}")
    private String travelFolder;

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
    protected Long create(TravelReqDTO dto) {
        Group group = groupRepository.findById(dto.getGroupPk())
                .orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST, "존재하지 않는 그룹입니다. groupId="+dto.getGroupPk()));

        TravelCategory category = travelCategoryRepository.findById(dto.getCategoryPk())
                .orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST, "존재하지 않는 카테고리입니다. travelCategoryId="+dto.getCategoryPk()));

        MultipartFile photo = dto.getPhoto();
        String savedName = s3Utils.uploadObject(photo, photo.getOriginalFilename(), travelFolder);
        if (savedName == null) {
            throw new RuntimeException("이미지 업로드에 실패했습니다.");
        }

        String location = dto.getCountry() + " " + dto.getCity();

        Travel travel = Travel.builder()
                .tpTitle(dto.getTitle())
                .tpLocation(location)
                .tpStart(dto.getStartDate())
                .tpEnd(dto.getEndDate())
                .tpImage(savedName)
                .tpState(TRAVELSTATE.BEFORE)
                .travelCategory(category)
                .group(group)
                .build();
        travelRepository.save(travel);
        return group.getGroupPk();
    }

    // 단일 여행 조회
    public TravelResDTO getTravelById(Long travelPk) {
        return travelRepository.findById(travelPk)
                .map(this::toDto)
                .orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST, "존재하지 않는 여행입니다. travelId=" + travelPk));
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
                .build();
    }

}
