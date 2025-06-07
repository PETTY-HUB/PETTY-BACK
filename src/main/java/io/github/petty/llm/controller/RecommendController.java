package io.github.petty.llm.controller;

import io.github.petty.llm.dto.RecommendResponseDTO;
import io.github.petty.llm.service.RecommendService;
import io.github.petty.tour.service.TourService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class RecommendController {
    private final RecommendService recommendService;
    private final TourService tourService;

    // 기존 API 엔드포인트 (JSON 응답)
    @PostMapping("/api/recommend")
    @ResponseBody
    public ResponseEntity<RecommendResponseDTO> recommend(@RequestBody Map<String, String> promptMap) {
        RecommendResponseDTO result = recommendService.recommend(promptMap);
        return ResponseEntity.ok(result);
    }

}
