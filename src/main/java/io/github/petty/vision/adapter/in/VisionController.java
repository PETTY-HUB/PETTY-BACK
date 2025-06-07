package io.github.petty.vision.adapter.in;

import io.github.petty.vision.helper.ImageValidator;
import io.github.petty.vision.helper.ImageValidator.ValidationResult;
import io.github.petty.vision.port.in.VisionUseCase;
import io.github.petty.vision.service.VisionServiceImpl;
import io.github.petty.users.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/vision")
@RequiredArgsConstructor
public class VisionController {
    private final VisionUseCase vision;
    private final VisionServiceImpl visionService;
    private final ImageValidator imageValidator;
    private final UserService userService;

    // 일일 사용량 제한 (설정값으로 쉽게 변경 가능)
    private static final int DAILY_LIMIT = 3;

    // 세션 키 상수
    private static final String SESSION_USAGE_COUNT = "vision_daily_usage_count";
    private static final String SESSION_USAGE_DATE = "vision_usage_date";

    @GetMapping("/upload")
    public String page(Model model, HttpSession session) {  // <-- 반드시 HttpSession 파라미터 추가
        // 로그인 확인
        if (!isAuthenticated()) {
            return "redirect:/login";
        }

        // 현재 사용자의 사용량 정보를 모델에 추가
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        int remainingUsage = getRemainingUsage(session);  // 세션 파라미터 전달

        model.addAttribute("remainingUsage", remainingUsage);
        model.addAttribute("canUse", remainingUsage > 0);
        model.addAttribute("dailyLimit", DAILY_LIMIT);
        model.addAttribute("username", auth.getName());

        log.info("📊 Vision 페이지 접근: 사용자={}, 남은횟수={}/{}",
                auth.getName(), remainingUsage, DAILY_LIMIT);

        return "visionUpload";
    }

    @PostMapping("/species")
    @ResponseBody
    public String getSpeciesInterim(
            @RequestParam("file") MultipartFile file,
            @RequestParam("petName") String petName,
            HttpSession session
    ) throws IOException {
        if (!isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        ValidationResult vr = imageValidator.validate(file);
        if (!vr.isValid()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, vr.getMessage());
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        try {
            session.setAttribute("petName", petName);
            byte[] imageBytes = file.getBytes();
            session.setAttribute("tempImageBytes", imageBytes);
            String imageBase64 = java.util.Base64.getEncoder().encodeToString(imageBytes);
            session.setAttribute("petImageBase64", "data:image/jpeg;base64," + imageBase64);
            String result = vision.interim(file.getBytes(), petName);
            log.info("🔍 종 분석 성공: 사용자={}, 반려동물={}", auth.getName(), petName);
            return result;
        } catch (Exception e) {
            log.error("❌ 종 분석 실패: 사용자={}, 오류={}", auth.getName(), e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "분석 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @PostMapping("/analyze")
    @ResponseBody
    public String analyze(
            @RequestParam("file") MultipartFile file,
            @RequestParam("petName") String petName,
            HttpSession session
    ) {
        if (!isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!canAnalyzeToday(session)) {
            int todayUsage = getTodayUsage(session);
            String errorMessage = String.format(
                    "오늘의 분석 한도(%d회)를 모두 사용하셨습니다. (사용: %d회)\n" +
                            "내일 다시 이용해주세요! 🐾", DAILY_LIMIT, todayUsage);
            log.warn("⚠️ 사용량 한도 초과: 사용자={}, 사용횟수={}/{}",
                    auth.getName(), todayUsage, DAILY_LIMIT);
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, errorMessage);
        }
        ValidationResult vr = imageValidator.validate(file);
        if (!vr.isValid()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, vr.getMessage());
        }
        try {
            String visionReport = visionService.analyze(file, petName);
            incrementUsage(session);
            session.setAttribute("visionReport", visionReport);
            session.setAttribute("petName", petName);
            log.info("✅ Vision 분석 완료: 사용자={}, 반려동물={}, 남은횟수={}/{}",
                    auth.getName(), petName, getRemainingUsage(session), DAILY_LIMIT);
            return visionReport;
        } catch (Exception e) {
            log.error("❌ Vision 분석 실패: 사용자={}, 오류={}", auth.getName(), e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "분석 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @GetMapping("/usage")
    @ResponseBody
    public Map<String, Object> getUserUsage(HttpSession session) {
        if (!isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        Map<String, Object> response = new HashMap<>();
        response.put("remainingUsage", getRemainingUsage(session));
        response.put("todayUsage", getTodayUsage(session));
        response.put("dailyLimit", DAILY_LIMIT);
        return response;
    }

    // =============== 헬퍼 메서드들 ===============

    private boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() &&
                !(auth instanceof AnonymousAuthenticationToken);
    }

    private boolean canAnalyzeToday(HttpSession session) {
        int todayUsage = getTodayUsage(session);
        return todayUsage < DAILY_LIMIT;
    }

    private int getTodayUsage(HttpSession session) {
        String today = LocalDate.now().toString();
        String sessionDate = (String) session.getAttribute(SESSION_USAGE_DATE);
        if (!today.equals(sessionDate)) {
            session.setAttribute(SESSION_USAGE_DATE, today);
            session.setAttribute(SESSION_USAGE_COUNT, 0);
            return 0;
        }
        Integer count = (Integer) session.getAttribute(SESSION_USAGE_COUNT);
        return count != null ? count : 0;
    }

    private int getRemainingUsage(HttpSession session) {
        int todayUsage = getTodayUsage(session);
        return Math.max(0, DAILY_LIMIT - todayUsage);
    }

    private void incrementUsage(HttpSession session) {
        int currentUsage = getTodayUsage(session);
        session.setAttribute(SESSION_USAGE_COUNT, currentUsage + 1);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("📈 사용량 증가: 사용자={}, 새로운사용량={}/{}",
                auth.getName(), currentUsage + 1, DAILY_LIMIT);
    }
}
