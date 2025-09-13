package org.solcation.solcation_be.domain.stats.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.solcation.solcation_be.domain.stats.service.GeminiInsightService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Gemini 컨트롤러")
@RestController
@RequestMapping("/groups/{groupId:\\d+}/stats")
@RequiredArgsConstructor
public class GeminiController {

    private final GeminiInsightService geminiInsightService;

    @GetMapping("/api/{travelId}/insight")
    public String getTravelInsight(@PathVariable Long groupId, @PathVariable Long travelId) {
        return geminiInsightService.generateTravelInsight(travelId);
    }
}
