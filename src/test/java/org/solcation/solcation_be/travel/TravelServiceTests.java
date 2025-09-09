package org.solcation.solcation_be.travel;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.solcation.solcation_be.domain.travel.dto.TravelReqDTO;
import org.solcation.solcation_be.domain.travel.dto.TravelResDTO;
import org.solcation.solcation_be.domain.travel.service.TravelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@Transactional   // 테스트 후 롤백
public class TravelServiceTests {

    @Autowired
    private TravelService travelService;


    @Test
    void 여행_생성_및_조회() {
        // given
        MockMultipartFile photo = new MockMultipartFile(
                "photo",                         // form field name
                "test.jpg",                      // original filename
                "image/jpeg",                    // content type
                "dummy-image-content".getBytes() // file content
        );

        TravelReqDTO req = TravelReqDTO.builder()
                .groupPk(13L)
                .categoryPk(1L)
                .title("제주도 여행")
                .photo(photo)
                .build();

        // when : 생성
        Long travelPk = travelService.create(req);
        assertNotNull(travelPk, "여행 생성 후 travelPk가 null이면 안됨");

        // then : 조회
        TravelResDTO res = travelService.getTravelById(travelPk);
        assertNotNull(res, "조회 결과가 null이면 안됨");
        assertEquals("제주도 여행", res.getTitle());
        log.info("조회 결과: {}", res);
    }
}
