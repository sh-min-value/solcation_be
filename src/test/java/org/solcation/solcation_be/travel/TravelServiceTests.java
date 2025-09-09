package org.solcation.solcation_be.travel;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.solcation.solcation_be.domain.travel.dto.TravelReqDTO;
import org.solcation.solcation_be.domain.travel.dto.TravelResDTO;
import org.solcation.solcation_be.domain.travel.service.TravelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.AuditorAware;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
@SpringBootTest
class TravelServiceTests {

    @Autowired
    private TravelService travelService;

    // 앱 본체에 @EnableJpaAuditing 있음: 테스트에선 Auditor만 mock으로 주입
    @MockBean
    AuditorAware<Long> auditorAware;

    @org.junit.jupiter.api.BeforeEach
    void setUpAuditor() {
        org.mockito.Mockito.when(auditorAware.getCurrentAuditor())
                .thenReturn(java.util.Optional.of(1L));
    }

    @Test
    @Transactional
    void createTravelReq() {
        MockMultipartFile photo = new MockMultipartFile(
                "photo", "test.jpg", "image/jpeg", "dummy-image-content".getBytes()
        );

        TravelReqDTO req = TravelReqDTO.builder()
                .groupPk(13L)
                .country("대한민국")
                .city("제주시")
                .title("한라봉 푸파")
                .startDate(java.time.LocalDate.of(2025, 9, 20))
                .endDate(java.time.LocalDate.of(2025, 9, 22))
                .categoryPk(3L)
                .photo(photo)
                .participant(2)
                .build();

        Long travelPk = travelService.create(req);
        assertNotNull(travelPk, "여행 생성 후 travelPk가 null이면 안됨");

        TravelResDTO res = travelService.getTravelById(travelPk);
        assertNotNull(res, "조회 결과가 null이면 안됨");
        assertEquals("한라봉 푸파", res.getTitle());   // ★ 입력값과 동일하게 검증
        log.info("조회 결과: {}", res);
    }
}
