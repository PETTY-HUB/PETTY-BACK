package io.github.petty.users.oauth2;

import io.github.petty.users.jwt.JWTUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        // JWT 발급
        String token = jwtUtil.createJwt(oAuth2User.getEmail(), oAuth2User.getAuthorities().iterator().next().getAuthority(), 3600000L);

        // JWT 토큰을 쿠키에 저장
        Cookie jwtCookie = new Cookie("jwt", token);
        jwtCookie.setHttpOnly(true); // JavaScript 접근 방지 (XSS 방어)
        jwtCookie.setPath("/"); // 쿠키의 유효 경로
        // jwtCookie.setSecure(true); // HTTPS 환경에서만 전송 (로컬호스트에서는 생략)
        int maxAgeSeconds = (int) (3600000L / 1000); // 만료 시간을 초 단위로 변환
        jwtCookie.setMaxAge(maxAgeSeconds); // 쿠키의 만료 시간 설정
        response.addCookie(jwtCookie);

        String targetUrl = "/";

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}