package io.github.petty.pipeline.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.petty.llm.service.RecommendService;
import io.github.petty.pipeline.support.TogetherPromptBuilder;
import io.github.petty.vision.port.in.VisionUseCase;
import io.github.petty.vision.service.VisionServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/flow") // 기존 controller들과 충돌 방지
@RequiredArgsConstructor
public class UnifiedFlowController {

    private final VisionUseCase visionUseCase;
    private final VisionServiceImpl visionService;
    private final TogetherPromptBuilder togetherPromptBuilder;
    private final RecommendService recommendService;

    @GetMapping
    public String page() {
        return "unifiedFlow";
    }

    @PostMapping("/analyze")
    public String analyze(
            @RequestParam("file") MultipartFile file,
            @RequestParam("petName") String petName,
            @RequestParam("location") String location,
            Model model
    ) {
        try {
            // 1. 중간 종 추론 결과
            String interim = visionUseCase.interim(file.getBytes(), petName);

            // 2. Vision 보고서 생성
            String visionReport = visionService.analyze(file, petName);
            log.info("📄 Vision Report: {}", visionReport);
            log.info("📌 location = {}", location);

            // 3. 프롬프트 생성 및 추천 요청
            String jsonPrompt = togetherPromptBuilder.buildPrompt(visionReport, location);
            log.info("📌 location = {}", location);
            Map<String, String> promptMapper = new ObjectMapper().readValue(jsonPrompt, new TypeReference<>() {});
            String recommendation = recommendService.recommend(promptMapper);

            // 4. 화면에 전달
            model.addAttribute("interim", interim);
            model.addAttribute("visionReport", visionReport);
            model.addAttribute("recommendation", recommendation);

        } catch (Exception e) {
            log.error("❌ 분석 중 오류 발생", e);
            model.addAttribute("error", "분석 및 추천 중 오류가 발생했습니다.");
        }

        return "unifiedFlow";
    }
}
