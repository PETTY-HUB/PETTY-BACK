package io.github.petty.users.controller;

import io.github.petty.users.dto.EmailVerificationRequest;
import io.github.petty.users.dto.RefreshTokenResponseDTO;
import io.github.petty.users.dto.VerifyCodeRequest;
import io.github.petty.users.entity.Users;
import io.github.petty.users.jwt.JWTUtil;
import io.github.petty.users.repository.UsersRepository;
import io.github.petty.users.service.EmailService;
import io.github.petty.users.service.RefreshTokenService;
import io.github.petty.users.service.UserService;
import io.github.petty.users.util.CookieUtils;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class UsersApiController {

    private final JWTUtil jwtUtil;
    private final UserService userService;
    private final EmailService emailService;
    private final RefreshTokenService refreshTokenService;
    private final CookieUtils cookieUtils;
    private final UsersRepository usersRepository;

    public UsersApiController(JWTUtil jwtUtil, EmailService emailService, RefreshTokenService refreshTokenService, UserService userService, CookieUtils cookieUtils, UsersRepository usersRepository) {
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.emailService = emailService;
        this.refreshTokenService = refreshTokenService;
        this.cookieUtils = cookieUtils;
        this.usersRepository = usersRepository;
    }

    @GetMapping("/users/me")
    public ResponseEntity<Map<String, String>> getUserInfo() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() ||
                auth instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Users user = usersRepository.findByUsername(auth.getName());

        Map<String, String> userInfo = new HashMap<>();
        // TODO: 프론트엔드에서 username 키로 displayName을 사용 중(키 이름 정리 필요)
        userInfo.put("username", user.getDisplayName());
        userInfo.put("role", auth.getAuthorities().iterator().next().getAuthority());

        return ResponseEntity.ok(userInfo);
    }

    @PostMapping("/auth/send-verification")
    public ResponseEntity<Map<String, Object>> sendVerification(@RequestBody EmailVerificationRequest request) {
        // 인증 코드 생성 및 이메일 발송 로직
        boolean success = emailService.sendVerificationCode(request.getEmail());

        Map<String, Object> response = new HashMap<>();
        response.put("success", success);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/auth/verify-code")
    public ResponseEntity<Map<String, Object>> verifyCode(@RequestBody VerifyCodeRequest request) {
        // 인증 코드 검증 로직
        boolean isValid = emailService.verifyCode(request.getEmail(), request.getVerificationCode());

        Map<String, Object> response = new HashMap<>();
        response.put("success", isValid);

        return ResponseEntity.ok(response);
    }

    // 리프레시 토큰 엔드포인트 추가
    @PostMapping("/auth/refresh")
    public ResponseEntity<?> refreshToken(
            @CookieValue(value = "refresh_token", required = false) String refreshToken,
            HttpServletResponse response) {

        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "리프레시 토큰이 없습니다."));
        }

        try {
            UUID refreshTokenId = UUID.fromString(refreshToken);
            RefreshTokenResponseDTO tokenResponse = refreshTokenService.refreshAccessToken(refreshTokenId);

            // 쿠키 설정 코드
            cookieUtils.setTokenCookies(response,
                    tokenResponse.getAccessToken(),
                    UUID.fromString(tokenResponse.getRefreshToken()));

            return ResponseEntity.ok(Map.of("message", "토큰이 갱신되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "유효하지 않은 토큰 형식입니다."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // 닉네임 중복 검사 API 추가
    @GetMapping("/check-displayname")
    public ResponseEntity<Map<String, Object>> checkDisplayName(
            @RequestParam String displayName) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 현재 사용자 ID 가져오기
        UUID currentUserId = userService.getCurrentUserId(auth.getPrincipal());

        // 한 번의 서비스 호출로 모든 정보 가져오기
        Map<String, Object> result = userService.checkDisplayName(currentUserId, displayName);

        Map<String, Object> response = new HashMap<>();
        response.put("available", result.get("available"));
        response.put("message", result.get("message"));

        return ResponseEntity.ok(response);
    }
}