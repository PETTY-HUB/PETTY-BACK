package io.github.petty.llm.controller;

import io.github.petty.llm.common.AreaCode;
import io.github.petty.llm.dto.RecommendResponseDTO;
import io.github.petty.llm.service.RecommendService;
import io.github.petty.llm.service.VectorStoreService;
import io.github.petty.tour.dto.DetailCommonDto;
import io.github.petty.tour.service.TourService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
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

    // 새로운 상세 페이지 엔드포인트 (HTML 응답)
    @GetMapping("/recommend/detail/{contentId}")
    public String getRecommendDetail(@PathVariable String contentId, Model model) {
        try {
            Long id = Long.parseLong(contentId);
            DetailCommonDto contentDetail = tourService.getContentDetailById(id);
            model.addAttribute("contentDetail", contentDetail);
            return "recommend_detail";
        } catch (NumberFormatException e) {
            model.addAttribute("error", "잘못된 콘텐츠 ID입니다: " + contentId);
            return "recommend_detail";
        } catch (Exception e) {
            model.addAttribute("error", "콘텐츠 정보를 불러오는데 실패했습니다: " + e.getMessage());
            return "recommend_detail";
        }
    }
}
