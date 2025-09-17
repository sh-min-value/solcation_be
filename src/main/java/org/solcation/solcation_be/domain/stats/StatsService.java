package org.solcation.solcation_be.domain.stats;

import lombok.RequiredArgsConstructor;
import org.solcation.solcation_be.common.CustomException;
import org.solcation.solcation_be.common.ErrorCode;
import org.solcation.solcation_be.domain.stats.dto.*;
import org.solcation.solcation_be.entity.enums.TRANSACTIONTYPE;
import org.solcation.solcation_be.entity.enums.TRAVELSTATE;
import org.solcation.solcation_be.entity.Travel;
import org.solcation.solcation_be.repository.TransactionRepository;
import org.solcation.solcation_be.repository.TravelRepository;
import org.solcation.solcation_be.util.timezone.ZonedTimeRange;
import org.solcation.solcation_be.util.timezone.ZonedTimeUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final TravelRepository travelRepository;
    private final TransactionRepository transactionRepository;
    private final WebClient geminiWebClient;

    // 완료된 여행 목록 조회
    public List<FinishTravelListDTO> getFinishedTravels(Long groupPk) {
        List<Travel> travels =
                travelRepository.findByGroup_GroupPkAndTpStateOrderByTpEndDesc(groupPk, TRAVELSTATE.FINISH);

        return travels.stream()
                .map(t -> FinishTravelListDTO.builder()
                        .tpTitle(t.getTpTitle())
                        .tpLocation(t.getTpLocation())
                        .tpStart(t.getTpStart())
                        .tpEnd(t.getTpEnd())
                        .tpImage(t.getTpImage())
                        .tpcCode(t.getTravelCategory().getTpcCode())
                        .tpcName(t.getTravelCategory().getTpcName())
                        .tpPk(t.getTpPk())
                        .build())
                .toList();
    }

    // 실제 소비 총계
    public long getTravelTotalSpent(Long tpPk) {
        Travel travel = travelRepository.findById(tpPk)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_PLAN));

        ZonedTimeRange range = ZonedTimeUtil.custom(travel.getTpStart(), travel.getTpEnd());

        return transactionRepository.sumSpentTravel(
                tpPk,
                List.of(TRANSACTIONTYPE.WITHDRAW, TRANSACTIONTYPE.CARD),
                range.start(),
                range.end()
        );
    }

    // 계획 상 소비 총계
    public long getTravelPlannedTotal(Long tpPk) {
        return transactionRepository.sumPlannedTravel(tpPk);
    }

    // 카테고리 별 합계
    public List<CategorySpentDTO> getCategorySpentByTravel(Long travelId) {
        Travel travel = travelRepository.findById(travelId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_PLAN));

        ZonedTimeRange range = ZonedTimeUtil.custom(travel.getTpStart(), travel.getTpEnd());

        return transactionRepository.categorySpent(
                range.start(),
                range.end()
        );
    }

    // 다른 그룹과 비교
    public TravelSpendCompareDTO getCompareTravelSpend(Long tpPk) {
        Travel tp = travelRepository.findById(tpPk).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_PLAN));
        ZonedTimeRange range = ZonedTimeUtil.custom(tp.getTpStart(), tp.getTpEnd());
        Instant start = range.start();
        Instant endExclusive = range.end();

        List<TRANSACTIONTYPE> types = List.of(TRANSACTIONTYPE.WITHDRAW, TRANSACTIONTYPE.CARD);

        long ourTotal = transactionRepository.sumSpentTravel(tpPk, types, start, endExclusive);
        long days = ChronoUnit.DAYS.between(tp.getTpStart(), tp.getTpEnd()) + 1;
        long ourDenominator = Math.max(1, tp.getParticipant() * Math.max(1, days));
        long ourPerPersonPerDay = ourTotal / ourDenominator;

        long othersTotal = transactionRepository.sumOthersSpentBySameLocation(tpPk, types);
        long othersPersonDays = transactionRepository.sumOthersPersonDays(tpPk);
        long othersPerPersonPerDay = othersPersonDays > 0 ? (othersTotal / othersPersonDays) : 0;

        long diff = ourPerPersonPerDay - othersPerPersonPerDay;
        return new TravelSpendCompareDTO(ourPerPersonPerDay, othersPerPersonPerDay, diff);
    }

    // 다른 그룹과 카테고리 별 비교
    public List<CategorySpentCompareDTO> getCategoryCompare(Long tpPk) {
        Travel travel = travelRepository.findById(tpPk)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_PLAN));

        ZonedTimeRange range = ZonedTimeUtil.custom(travel.getTpStart(), travel.getTpEnd());

        // 내 여행 카테고리 소비
        List<CategorySpentDTO> mine = transactionRepository.categorySpent(
                range.start(),
                range.end()
        );

        // 다른 여행 평균 소비
        List<Object[]> rows = transactionRepository.categoryOthersAvgPerTravel(tpPk);
        List<CategorySpentDTO> others = rows.stream()
                .map(r -> new CategorySpentDTO(
                        ((Number) r[0]).longValue(),
                        (String) r[1],
                        (String) r[2],
                        ((Number) r[3]).longValue()
                ))
                .toList();

        Map<Long, CategorySpentDTO> mineMap = mine.stream()
                .collect(Collectors.toMap(CategorySpentDTO::getTcPk, dto -> dto));
        Map<Long, CategorySpentDTO> othersMap = others.stream()
                .collect(Collectors.toMap(CategorySpentDTO::getTcPk, dto -> dto));

        List<CategorySpentCompareDTO> result = new ArrayList<>();
        for (Long tcPk : mineMap.keySet()) {
            CategorySpentDTO m = mineMap.get(tcPk);
            CategorySpentDTO o = othersMap.get(tcPk);
            long myAmount = m != null ? m.getTotalAmount() : 0L;
            long othersAvg = o != null ? o.getTotalAmount() : 0L;
            long diff = myAmount - othersAvg;
            result.add(CategorySpentCompareDTO.builder()
                    .tcPk(tcPk)
                    .tcName(m != null ? m.getTcName() : o.getTcName())
                    .tcCode(m != null ? m.getTcCode() : o.getTcCode())
                    .myAmount(myAmount)
                    .othersAvg(othersAvg)
                    .diff(diff)
                    .build());
        }
        return result;
    }

    // 여행 계획과 실제 소비 카테고리 별 비교
    public List<CategoryPlannedCompareDTO> getPlanActualComparison(Long travelId) {
        Travel travel = travelRepository.findById(travelId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_PLAN));

        ZonedTimeRange range = ZonedTimeUtil.custom(travel.getTpStart(), travel.getTpEnd());

        List<CategorySpentDTO> planned = transactionRepository.plannedCategorySpentOfTravel(travelId);
        List<CategorySpentDTO> actual = transactionRepository.categorySpent(range.start(), range.end());

        Map<Long, CategorySpentDTO> plannedMap = planned.stream()
                .collect(Collectors.toMap(CategorySpentDTO::getTcPk, x -> x));
        Map<Long, CategorySpentDTO> actualMap = actual.stream()
                .collect(Collectors.toMap(CategorySpentDTO::getTcPk, x -> x));

        List<Long> keys = Stream.concat(plannedMap.keySet().stream(), actualMap.keySet().stream())
                .distinct()
                .sorted()
                .toList();

        List<CategoryPlannedCompareDTO> result = new ArrayList<>();
        for (Long k : keys) {
            CategorySpentDTO p = plannedMap.get(k);
            CategorySpentDTO a = actualMap.get(k);
            long plannedAmount = p != null ? p.getTotalAmount() : 0L;
            long actualAmount = a != null ? a.getTotalAmount() : 0L;
            long diff = actualAmount - plannedAmount;
            String name = p != null ? p.getTcName() : (a != null ? a.getTcName() : null);
            String code = p != null ? p.getTcCode() : (a != null ? a.getTcCode() : null);
            result.add(CategoryPlannedCompareDTO.builder()
                    .tcPk(k)
                    .tcName(name)
                    .tcCode(code)
                    .plannedAmount(plannedAmount)
                    .actualAmount(actualAmount)
                    .diff(diff)
                    .build());
        }
        return result;
    }

    //Gemini 인사이트 출력
    public String generateSimpleInsight(String userText) {
        Map<String, Object> body = Map.of(
                "contents", new Object[]{
                        Map.of("parts", new Object[]{
                                Map.of("text", userText)
                        })
                }
        );
        return geminiWebClient.post()
                .uri("/gemini-2.5-flash:generateContent")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .map(this::extractTextOrFallback)
                .onErrorResume(e -> Mono.just("요청 실패: " + e.getMessage()))
                .block();
    }

    public String generateTravelInsight(Long tpPk) {
        long actual = getTravelTotalSpent(tpPk);
        long planned = getTravelPlannedTotal(tpPk);
        List<CategorySpentDTO> actualCats = getCategorySpentByTravel(tpPk);
        TravelSpendCompareDTO compare = getCompareTravelSpend(tpPk);
        List<CategoryPlannedCompareDTO> planVsActual = getPlanActualComparison(tpPk);

        String actualCatsJson = actualCats.stream()
                .collect(Collectors.groupingBy(CategorySpentDTO::getTcName,
                        Collectors.summingLong(CategorySpentDTO::getTotalAmount)))
                .entrySet().stream()
                .map(e -> String.format("{\"name\":\"%s\",\"amount\":%d}", esc(e.getKey()), e.getValue()))
                .collect(Collectors.joining(",", "[", "]"));

        String planActualJson = planVsActual.stream()
                .map(x -> String.format("{\"name\":\"%s\",\"planned\":%d,\"actual\":%d,\"diff\":%d}",
                        esc(x.getTcName()), x.getPlannedAmount(), x.getActualAmount(), x.getDiff()))
                .collect(Collectors.joining(",", "[", "]"));

        String prompt = """
                너는 여행 예산/소비 분석가야. 아래 데이터를 근거로 한국어 마크다운 인사이트를 8~12줄로 작성해.
                - 말투는 어디서 얼마나 많이 썼어요, 어디서 얼마를 아낄 수 있어요, 어디서 평균적으로 많은 비용이 발생해요 등 존대, 핵심 단어 위주
                - 포함: 전체 총평 1줄, 과다/과소 지출 포인트 3개, 다음 여행 절감 팁 3개, 다른 그룹 대비 한줄 코멘트
                - 금액은 원화로 천단위 구분기호 사용, 카테고리명 그대로
                데이터:
                {
                  "total": { "planned": %d, "actual": %d },
                  "perCategoryActual": %s,
                  "planVsActual": %s,
                  "compareOthers": { "ourPerPersonPerDay": %d, "othersPerPersonPerDay": %d, "diff": %d }
                }
                출력은 마크다운만. 코드블록과 JSON 금지.
                """.formatted(
                planned, actual, actualCatsJson, planActualJson,
                compare.getOurPayPerDay(), compare.getAveragePayPerDay(), compare.getDifference()
        );

        Map<String, Object> body = Map.of(
                "contents", new Object[]{
                        Map.of("parts", new Object[]{
                                Map.of("text", prompt)
                        })
                }
        );

        return geminiWebClient.post()
                .uri("/gemini-2.5-flash:generateContent")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .map(this::extractTextOrFallback)
                .onErrorResume(e -> Mono.just("요청 실패: " + e.getMessage()))
                .block();
    }

    private String extractTextOrFallback(Map res) {
        try {
            var candidates = (java.util.List<Map<String, Object>>) res.get("candidates");
            if (candidates == null || candidates.isEmpty()) return "빈 응답: " + res;
            var content = (Map<String, Object>) candidates.get(0).get("content");
            var parts = (java.util.List<Map<String, Object>>) content.get("parts");
            if (parts == null || parts.isEmpty()) return "parts 없음: " + res;
            Object text = parts.get(0).get("text");
            return text != null ? text.toString() : "text 없음: " + res;
        } catch (Exception e) {
            return "응답 파싱 실패: " + res;
        }
    }

    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}