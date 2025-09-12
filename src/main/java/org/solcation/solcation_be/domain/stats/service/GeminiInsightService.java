package org.solcation.solcation_be.domain.stats.service;

import lombok.RequiredArgsConstructor;
import org.solcation.solcation_be.domain.stats.dto.CategoryPlannedCompareDTO;
import org.solcation.solcation_be.domain.stats.dto.CategorySpentDTO;
import org.solcation.solcation_be.domain.stats.dto.TravelSpendCompareDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GeminiInsightService {

    private final WebClient geminiWebClient;
    private final StatsService statsService;

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
        long actual = statsService.getTravelTotalSpent(tpPk);
        long planned = statsService.getTravelPlannedTotal(tpPk);
        List<CategorySpentDTO> actualCats = statsService.getCategorySpentByTravel(tpPk);
        TravelSpendCompareDTO compare = statsService.getCompareTravelSpend(tpPk);
        List<CategoryPlannedCompareDTO> planVsActual = statsService.getPlanActualComparison(tpPk);

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