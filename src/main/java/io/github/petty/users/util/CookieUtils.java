package io.github.petty.users.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CookieUtils {

    @Value("${security.cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${security.cookie.same-site:Lax}")
    private String sameSite;

    // 액세스 토큰과 리프레시 토큰 쿠키 설정
    public void setTokenCookies(HttpServletResponse response, String accessToken, UUID refreshToken) {
        // 액세스 토큰을 쿠키에 저장
        Cookie jwtCookie = new Cookie("jwt", accessToken);
        jwtCookie.setHttpOnly(true); // JavaScript 접근 방지 (XSS 방어)
        jwtCookie.setPath("/"); // 쿠키의 유효 경로
        jwtCookie.setSecure(cookieSecure); // HTTPS 환경에서만 전송
        jwtCookie.setMaxAge(3600);// 쿠키의 만료 시간 설정 (1시간)
        response.addCookie(jwtCookie);

        // 리프레시 토큰을 쿠키에 저장
        Cookie refreshCookie = new Cookie("refresh_token", refreshToken.toString());
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/");
        refreshCookie.setSecure(cookieSecure);
        refreshCookie.setMaxAge(7 * 24 * 60 * 60); // 7일
        response.addCookie(refreshCookie);
    }
}
