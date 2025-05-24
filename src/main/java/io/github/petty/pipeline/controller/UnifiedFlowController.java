package io.github.petty.pipeline.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.petty.llm.dto.RecommendResponseDTO;
import io.github.petty.llm.service.RecommendService;
import io.github.petty.pipeline.support.TogetherPromptBuilder;
import io.github.petty.vision.port.in.VisionUseCase;
import io.github.petty.vision.service.VisionServiceImpl; // VisionServiceImpl 사용 여부 확인 필요 (VisionUseCase와 중복될 수 있음)
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/flow")
@RequiredArgsConstructor
public class UnifiedFlowController {

    private final VisionUseCase visionUseCase;
    private final VisionServiceImpl visionService; // 이 서비스가 VisionUseCase의 구현체라면, 하나만 사용하거나 역할을 명확히 해야 합니다.
    private final TogetherPromptBuilder togetherPromptBuilder;
    private final RecommendService recommendService;

    // 1. 반려동물 분석 페이지 (초기 진입)
    @GetMapping("/analyze")
    public String analyzePage(HttpSession session) {
        // 새로운 분석 시작 시 이전 세션 데이터 정리
        log.info("새로운 분석 시작 - 이전 세션 데이터 정리");
        session.removeAttribute("recommendationResult");
        session.removeAttribute("visionReport");
        session.removeAttribute("lastAccessTime");
        return "analyze"; // analyze.html 반환
    }

    // 2. '내 반려동물 분석하기' 버튼 클릭 시
    @PostMapping("/analyze")
    public String performAnalysis(
            @RequestParam("file") MultipartFile file,
            @RequestParam("petName") String petName,
            RedirectAttributes redirectAttributes, // RedirectAttributes 사용
            HttpSession session
    ) {
        try {
            // 세션 한 번 더 제거
            session.removeAttribute("recommendationResult");
            session.removeAttribute("visionReport");
            session.removeAttribute("lastAccessTime");
            // interim 및 visionReport는 시간이 걸리는 작업이므로
            // 실제 구현에서는 비동기 처리 또는 로딩 페이지에서 Ajax 호출로 처리하는 것이 일반적입니다.
            // 여기서는 단순화를 위해 analyze POST 요청에서 미리 결과를 계산하고 전달합니다.

            String interim = visionUseCase.interim(file.getBytes(), petName);
            String visionReport = visionService.analyze(file, petName); // visionService.analyze 역할 확인 필요

            // 다음 페이지로 flash attribute로 전달 (URL에 노출되지 않음)
            redirectAttributes.addFlashAttribute("interim", interim);
            redirectAttributes.addFlashAttribute("petName", petName);

            // 최종 visionReport는 세션에 저장하여 다음 단계에서 재사용
            session.setAttribute("visionReport", visionReport);

            return "redirect:/flow/showInterimLoading"; // 중간 분석 로딩 페이지로 리다이렉트
        } catch (Exception e) {
            log.error("❌ 반려동물 분석 중 오류", e);
            redirectAttributes.addFlashAttribute("error", "반려동물 분석 중 오류 발생: " + e.getMessage());
            return "redirect:/flow/analyze"; // 오류 발생 시 다시 분석 페이지로
        }
    }

    // 3. 중간 분석 로딩 페이지 (interim_loading.html)
    @GetMapping("/showInterimLoading")
    public String showInterimLoading(
            @ModelAttribute("interim") String interim, // FlashAttribute로 받은 interim
            @ModelAttribute("petName") String petName, // FlashAttribute로 받은 petName
            Model model) {

        // interim이 flash attribute로 전달되지 않은 경우 (예: 새로고침)
        if (interim == null || interim.isEmpty()) {
            model.addAttribute("interim", "데이터를 불러오는 중이거나, 이전 요청이 완료되지 않았습니다. 잠시만 기다려 주세요.");
        }
        model.addAttribute("petName", petName); // petName도 전달

        return "interim_loading"; // interim_loading.html 반환
    }

    // 4. 최종 Vision 보고서 페이지 (vision_report.html) - interim_loading.html의 JS에서 호출됨
    @GetMapping("/showVisionReport")
    public String showVisionReport(Model model, HttpSession session,
                                   @ModelAttribute("petName") String petName) { // petName 전달 받기

        String visionReport = (String) session.getAttribute("visionReport");

        if (visionReport == null) {
            model.addAttribute("error", "세션에 Vision 보고서가 없습니다. 다시 분석을 시작해 주세요.");
            return "analyze"; // Vision 보고서 없으면 분석 시작 페이지로
        }

        model.addAttribute("visionReport", visionReport);
        model.addAttribute("petName", petName); // petName 전달

        return "vision_report"; // vision_report.html 반환
    }

    // 5. '여행지 추천 받기' 버튼 클릭 시 (vision_report.html에서 POST 요청)
    @PostMapping("/report")
    public String generateRecommendation(
            @RequestParam("petName") String petName,
            @RequestParam("location") String location,
            @RequestParam("info") String info,
           //  @RequestParam("is_danger") String isDanger,
            RedirectAttributes redirectAttributes,
            HttpSession session
    ) {
        try {
            // 세션에서 visionReport 가져오기
            String visionReport = (String) session.getAttribute("visionReport");
            if (visionReport == null) {
                redirectAttributes.addFlashAttribute("error", "세션에 Vision 보고서가 없습니다. 다시 분석을 시작해 주세요.");
                return "redirect:/flow/analyze"; // Vision 보고서 없으면 분석 시작 페이지로
            }

            // 프롬프트 빌딩 및 추천 서비스 호출 (시간 소요)
            // 실제 구현에서는 비동기 처리 또는 로딩 페이지에서 Ajax 호출로 처리하는 것이 일반적입니다.
            String jsonPrompt = togetherPromptBuilder.buildPrompt(visionReport, location, info);
            Map<String, String> promptMapper = new ObjectMapper().readValue(jsonPrompt, new TypeReference<>() {});
            RecommendResponseDTO recommendation = recommendService.recommend(promptMapper);

            // 추천 결과는 세션에 저장
            session.setAttribute("recommendationResult", recommendation);

            // 다음 페이지로 필요한 데이터 전달
            redirectAttributes.addFlashAttribute("petName", petName);
            redirectAttributes.addFlashAttribute("location", location);
            redirectAttributes.addFlashAttribute("info", info); // info도 전달 (로깅 등 필요할 경우)

            return "redirect:/flow/showRecommendLoading"; // 추천 로딩 페이지로 리다이렉트
        } catch (Exception e) {
            log.error("❌ 추천 생성 중 오류", e);
            redirectAttributes.addFlashAttribute("error", "추천 생성 중 오류 발생: " + e.getMessage());
            // 오류 발생 시 다시 Vision 보고서 페이지로 (데이터는 세션에서 가져와야 함)
            return "redirect:/flow/showVisionReport";
        }
    }

    // 6. 여행지 추천 로딩 페이지 (recommend_loading.html)
    @GetMapping("/showRecommendLoading")
    public String showRecommendLoading(
            @ModelAttribute("petName") String petName,
            @ModelAttribute("location") String location,
            @ModelAttribute("info") String info,
            Model model) {
        // 로딩 메시지만 보여주는 페이지
        model.addAttribute("petName", petName);
        model.addAttribute("location", location);
        model.addAttribute("info", info);
        return "recommend_loading"; // recommend_loading.html 반환
    }

    // 7. 추천 여행지 결과 페이지 (recommendation_result.html) - recommend_loading.html의 JS에서 호출됨
    @GetMapping("/showRecommendationResult")
    public String showRecommendationResult(Model model, HttpSession session) {

        RecommendResponseDTO recommendation = (RecommendResponseDTO) session.getAttribute("recommendationResult");

        if (recommendation == null) {
            model.addAttribute("error", "세션에 추천 여행지 결과가 없습니다. 다시 시도해 주세요.");
            return "analyze"; // 결과 없으면 분석 시작 페이지로
        }

        model.addAttribute("recommendation", recommendation);
        // 필요에 따라 petName, location 등도 세션에서 가져와 모델에 추가할 수 있습니다.
        // model.addAttribute("petName", session.getAttribute("petName"));

//        // 사용 후 세션에서 제거 (선택 사항, 메모리 관리)
//        session.removeAttribute("recommendationResult");
//        session.removeAttribute("visionReport");

        return "recommendation_result"; // recommendation_result.html 반환
    }
}