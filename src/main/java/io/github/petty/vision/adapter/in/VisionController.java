package io.github.petty.vision.adapter.in;

import io.github.petty.vision.entity.VisionUsage;
import io.github.petty.vision.helper.ImageValidator;
import io.github.petty.vision.helper.ImageValidator.ValidationResult;
import io.github.petty.vision.port.in.VisionUseCase;
import io.github.petty.vision.service.VisionServiceImpl;
import io.github.petty.vision.service.VisionUsageService;
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
import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Controller
@RequestMapping("/vision")
@RequiredArgsConstructor
public class VisionController {
    private final VisionUseCase vision;
    private final VisionServiceImpl visionService;
    private final ImageValidator imageValidator;
    private final UserService userService;
    private final VisionUsageService visionUsageService;

    // 세션 fallback용 상수
    private static final int DEFAULT_DAILY_LIMIT = 3;
    private static final String SESSION_USAGE_COUNT = "vision_daily_usage_count";
    private static final String SESSION_USAGE_DATE = "vision_usage_date";

    @GetMapping("/upload")
    public String page(Model model, HttpSession session, HttpServletRequest request) {
        // 인증 상태 확인 (리프레시 토큰 고려)
        AuthenticationResult authResult = checkAuthenticationWithRefresh(request);
        if (!authResult.isAuthenticated()) {
            log.info(" Vision 페이지 접근 실패: 인증 필요");
            return "redirect:/login";
        }

        UUID userId = authResult.getUserId();

        // DB 사용량 조회 시도 (실패 시 세션 fallback)
        int remainingUsage;
        int todayUsed;
        int dailyLimit = DEFAULT_DAILY_LIMIT;
        boolean canUse;
        String dataSource = "DB";

        try {
            VisionUsage todayUsage = visionUsageService.getTodayUsage(userId);
            remainingUsage = todayUsage.getRemainingUsage();
            todayUsed = todayUsage.getUsageCount();
            dailyLimit = todayUsage.getDailyLimit();
            canUse = todayUsage.canUse();

            log.info(" DB에서 사용량 조회 성공: 사용자={}, 남은횟수={}/{}",
                    authResult.getUsername(), remainingUsage, dailyLimit);
        } catch (Exception e) {
            log.warn(" DB 사용량 조회 실패, 세션으로 fallback: 사용자={}, 오류={}",
                    authResult.getUsername(), e.getMessage());

            // 세션 기반 fallback
            Map<String, Integer> sessionUsage = getSessionUsage(session);
            todayUsed = sessionUsage.get("used");
            remainingUsage = Math.max(0, DEFAULT_DAILY_LIMIT - todayUsed);
            canUse = remainingUsage > 0;
            dataSource = "Session";
        }

        model.addAttribute("remainingUsage", remainingUsage);
        model.addAttribute("canUse", canUse);
        model.addAttribute("dailyLimit", dailyLimit);
        model.addAttribute("todayUsed", todayUsed);
        model.addAttribute("username", authResult.getUsername());
        model.addAttribute("dataSource", dataSource); // 디버그용

        log.info(" Vision 페이지 접근: 사용자={}, 남은횟수={}/{}, 데이터소스={}",
                authResult.getUsername(), remainingUsage, dailyLimit, dataSource);

        return "visionUpload";
    }

    @PostMapping("/species")
    @ResponseBody
    public ResponseEntity<?> getSpeciesInterim(
            @RequestParam("file") MultipartFile file,
            @RequestParam("petName") String petName,
            HttpSession session,
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        log.info(" [DEBUG] /species 호출됨 - petName: {}, fileSize: {}", petName, file.getSize());

        // 인증 확인 (리프레시 토큰 자동 갱신 포함)
        AuthenticationResult authResult = checkAuthenticationWithRefresh(request);
        if (!authResult.isAuthenticated()) {
            log.error(" [DEBUG] 인증 실패 - 토큰 만료 또는 없음");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "인증이 필요합니다. 다시 로그인해주세요."));
        }

        log.info(" [DEBUG] 인증 성공: 사용자={}", authResult.getUsername());

        ValidationResult vr = imageValidator.validate(file);
        if (!vr.isValid()) {
            log.error(" [DEBUG] 이미지 검증 실패: {}", vr.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", convertToUserFriendlyMessage(vr.getMessage())));
        }

        log.info(" [DEBUG] 이미지 검증 성공");

        try {
            session.setAttribute("petName", petName);
            byte[] imageBytes = file.getBytes();
            session.setAttribute("tempImageBytes", imageBytes);
            String imageBase64 = java.util.Base64.getEncoder().encodeToString(imageBytes);
            session.setAttribute("petImageBase64", "data:image/jpeg;base64," + imageBase64);

            log.info(" [DEBUG] 세션 저장 완료, vision.interim 호출 시작");

            String result = vision.interim(file.getBytes(), petName);

            log.info(" [DEBUG] vision.interim 성공: {}", result);
            log.info(" 종 분석 성공: 사용자={}, 반려동물={}", authResult.getUsername(), petName);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error(" [DEBUG] vision.interim 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "분석 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    @PostMapping("/analyze")
    @ResponseBody
    public ResponseEntity<?> analyze(
            @RequestParam("file") MultipartFile file,
            @RequestParam("petName") String petName,
            HttpSession session,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        log.info(" [DEBUG] /analyze 호출됨 - petName: {}, fileSize: {}", petName, file.getSize());

        // 인증 확인 (리프레시 토큰 자동 갱신 포함)
        AuthenticationResult authResult = checkAuthenticationWithRefresh(request);
        if (!authResult.isAuthenticated()) {
            log.error(" [DEBUG] 인증 실패");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "인증이 필요합니다. 다시 로그인해주세요."));
        }

        UUID userId = authResult.getUserId();
        log.info(" [DEBUG] 인증 성공: userId={}", userId);

        // 사용량 확인 (DB 우선, 실패 시 세션)
        boolean canAnalyze = false;
        String usageCheckMethod = "DB";

        try {
            log.info(" [DEBUG] DB 사용량 확인 시작");
            canAnalyze = visionUsageService.canUseToday(userId);
            log.info(" [DEBUG] DB 사용량 확인 성공: canAnalyze={}", canAnalyze);

            if (!canAnalyze) {
                VisionUsage todayUsage = visionUsageService.getTodayUsage(userId);
                String errorMessage = String.format(
                        "오늘의 분석 한도(%d회)를 모두 사용하셨습니다. (사용: %d회)\n" +
                                "내일 다시 이용해주세요! 🐾",
                        todayUsage.getDailyLimit(), todayUsage.getUsageCount());
                log.warn(" [DEBUG] 사용량 한도 초과");
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body(Map.of("error", errorMessage));
            }
        } catch (Exception e) {
            log.warn(" [DEBUG] DB 사용량 확인 실패, 세션으로 fallback: {}", e.getMessage());

            Map<String, Integer> sessionUsage = getSessionUsage(session);
            canAnalyze = sessionUsage.get("remaining") > 0;
            usageCheckMethod = "Session";

            log.info(" [DEBUG] 세션 사용량 확인: canAnalyze={}, remaining={}",
                    canAnalyze, sessionUsage.get("remaining"));

            if (!canAnalyze) {
                String errorMessage = String.format(
                        "오늘의 분석 한도(%d회)를 모두 사용하셨습니다. (사용: %d회)\n" +
                                "내일 다시 이용해주세요! 🐾",
                        DEFAULT_DAILY_LIMIT, sessionUsage.get("used"));
                log.warn(" [DEBUG] 세션 사용량 한도 초과");
                return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                        .body(Map.of("error", errorMessage));
            }
        }

        ValidationResult vr = imageValidator.validate(file);
        if (!vr.isValid()) {
            log.error(" [DEBUG] 이미지 검증 실패: {}", vr.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", convertToUserFriendlyMessage(vr.getMessage())));
        }

        log.info(" [DEBUG] 이미지 검증 성공");

        try {
            log.info(" [DEBUG] visionService.analyze 호출 시작");

            String visionReport = visionService.analyze(file, petName);

            log.info(" [DEBUG] visionService.analyze 성공, 길이: {}", visionReport.length());

            // 사용량 증가
            try {
                if ("DB".equals(usageCheckMethod)) {
                    log.info(" [DEBUG] DB 사용량 증가 시작");
                    VisionUsage updatedUsage = visionUsageService.incrementUsage(userId);
                    log.info(" [DEBUG] DB 사용량 증가 성공: 남은횟수={}/{}",
                            updatedUsage.getRemainingUsage(), updatedUsage.getDailyLimit());
                }
            } catch (Exception e) {
                log.warn(" [DEBUG] DB 사용량 증가 실패, 세션으로 처리: {}", e.getMessage());
                incrementSessionUsage(session);
                usageCheckMethod = "Session";
            }

            if ("Session".equals(usageCheckMethod)) {
                log.info(" [DEBUG] 세션 사용량 증가");
                incrementSessionUsage(session);
            }

            // 세션에 결과 저장
            session.setAttribute("visionReport", visionReport);
            session.setAttribute("petName", petName);

            log.info(" Vision 분석 완료: 사용자={}, 반려동물={}, 사용량체크={}",
                    authResult.getUsername(), petName, usageCheckMethod);

            return ResponseEntity.ok(visionReport);
        } catch (Exception e) {
            log.error(" [DEBUG] visionService.analyze 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "분석 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    // =============== 인증 및 리프레시 토큰 처리 ===============

    /**
     * 인증 상태 확인 및 자동 토큰 갱신
     */
    private AuthenticationResult checkAuthenticationWithRefresh(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // 기본 인증 확인
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            log.debug(" 기본 인증 실패");
            return AuthenticationResult.unauthenticated();
        }

        try {
            UUID userId = userService.getCurrentUserId(auth.getPrincipal());
            String username = auth.getName();

            log.debug(" 인증 확인 성공: 사용자={}, userId={}", username, userId);

            return AuthenticationResult.authenticated(userId, username);
        } catch (Exception e) {
            log.warn(" 사용자 정보 추출 실패: {}", e.getMessage());
            return AuthenticationResult.unauthenticated();
        }
    }

    /**
     * 인증 결과를 담는 내부 클래스
     */
    private static class AuthenticationResult {
        private final boolean authenticated;
        private final UUID userId;
        private final String username;

        private AuthenticationResult(boolean authenticated, UUID userId, String username) {
            this.authenticated = authenticated;
            this.userId = userId;
            this.username = username;
        }

        public static AuthenticationResult authenticated(UUID userId, String username) {
            return new AuthenticationResult(true, userId, username);
        }

        public static AuthenticationResult unauthenticated() {
            return new AuthenticationResult(false, null, null);
        }

        public boolean isAuthenticated() { return authenticated; }
        public UUID getUserId() { return userId; }
        public String getUsername() { return username; }
    }

    @GetMapping("/usage")
    @ResponseBody
    public ResponseEntity<?> getUserUsage(HttpSession session, HttpServletRequest request) {
        AuthenticationResult authResult = checkAuthenticationWithRefresh(request);
        if (!authResult.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "인증이 필요합니다."));
        }

        UUID userId = authResult.getUserId();
        Map<String, Object> response = new HashMap<>();

        try {
            VisionUsage todayUsage = visionUsageService.getTodayUsage(userId);
            response.put("remainingUsage", todayUsage.getRemainingUsage());
            response.put("todayUsage", todayUsage.getUsageCount());
            response.put("dailyLimit", todayUsage.getDailyLimit());
            response.put("totalUsage", visionUsageService.getTotalUsage(userId));
            response.put("source", "DB");
        } catch (Exception e) {
            log.warn("DB 사용량 조회 실패, 세션 사용: {}", e.getMessage());
            Map<String, Integer> sessionUsage = getSessionUsage(session);
            response.put("remainingUsage", sessionUsage.get("remaining"));
            response.put("todayUsage", sessionUsage.get("used"));
            response.put("dailyLimit", DEFAULT_DAILY_LIMIT);
            response.put("totalUsage", sessionUsage.get("used"));
            response.put("source", "Session");
        }

        return ResponseEntity.ok(response);
    }

    // =============== 세션 기반 Fallback 메서드들 ===============

    private Map<String, Integer> getSessionUsage(HttpSession session) {
        String today = LocalDate.now().toString();
        String sessionDate = (String) session.getAttribute(SESSION_USAGE_DATE);

        if (!today.equals(sessionDate)) {
            session.setAttribute(SESSION_USAGE_DATE, today);
            session.setAttribute(SESSION_USAGE_COUNT, 0);
        }

        Integer used = (Integer) session.getAttribute(SESSION_USAGE_COUNT);
        if (used == null) used = 0;

        int remaining = Math.max(0, DEFAULT_DAILY_LIMIT - used);

        Map<String, Integer> result = new HashMap<>();
        result.put("used", used);
        result.put("remaining", remaining);
        return result;
    }

    private void incrementSessionUsage(HttpSession session) {
        Map<String, Integer> current = getSessionUsage(session);
        int newUsed = current.get("used") + 1;
        session.setAttribute(SESSION_USAGE_COUNT, newUsed);

        log.info(" 세션 사용량 증가: 새로운사용량={}/{}", newUsed, DEFAULT_DAILY_LIMIT);
    }

    /**
     * 기술적인 에러 메시지를 사용자 친화적인 메시지로 변환
     */
    private String convertToUserFriendlyMessage(String technicalMessage) {
        if (technicalMessage.contains("해상도가 너무 낮습니다")) {
            return " 이미지 해상도가 너무 낮습니다.\n\n" +
                    " 해결 방법:\n" +
                    "• 더 고해상도 이미지를 사용해주세요\n" +
                    "• 스마트폰으로 새로 촬영해보세요\n" +
                    "• 이미지를 확대하지 말고 원본을 사용해주세요";
        }

        if (technicalMessage.contains("파일이 너무 큽니다")) {
            return " 이미지 파일이 너무 큽니다. 5MB 이하로 줄여주세요.";
        }

        if (technicalMessage.contains("지원하지 않는 이미지 형식")) {
            return " JPG, PNG 형식만 지원합니다.";
        }

        return technicalMessage;
    }
}