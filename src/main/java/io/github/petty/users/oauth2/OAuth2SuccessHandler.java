package io.github.petty.users.oauth2;

import io.github.petty.users.entity.Users;
import io.github.petty.users.jwt.JWTUtil;
import io.github.petty.users.repository.UsersRepository;
import io.github.petty.users.service.RefreshTokenService;
import io.github.petty.users.util.CookieUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final UsersRepository usersRepository;
    private final CookieUtils cookieUtils;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        // 액세스 토큰 생성
        String token = jwtUtil.createJwt(oAuth2User.getEmail(),
                oAuth2User.getAuthorities().iterator().next().getAuthority(),
                3600000L); // 1시간

        // 사용자 조회
        Users user = usersRepository.findByUsername(oAuth2User.getEmail());

        // 리프레시 토큰 생성
        UUID refreshToken = refreshTokenService.createRefreshToken(user);

        // 쿠키 설정 코드
        cookieUtils.setTokenCookies(response, token, refreshToken);

        String targetUrl = "/";
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}