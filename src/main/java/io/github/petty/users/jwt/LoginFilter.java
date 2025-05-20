package io.github.petty.users.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.petty.users.dto.CustomUserDetails;
import io.github.petty.users.dto.LoginDTO;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

public class LoginFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;

    // expirationTime 주입
    @Value("${jwt.expiration-time}")
    private long expirationTime;

    public LoginFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil) {

        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;

    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        String username;
        String password;

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            LoginDTO loginDTO = objectMapper.readValue(request.getInputStream(), LoginDTO.class);

            username = loginDTO.getUsername();
            password = loginDTO.getPassword();

        } catch (IOException e) {
            throw new AuthenticationServiceException("Failed to parse JSON request", e);
        }

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, password, null);
        authToken.setDetails(this.authenticationDetailsSource.buildDetails(request));

        return this.authenticationManager.authenticate(authToken);
    }

    //로그인 성공시 (여기서 JWT를 발급)
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) {
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

        String username = customUserDetails.getUsername();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();

        String role = auth.getAuthority();
        String token = jwtUtil.createJwt(username, role, 3600000L); // expirationTime

        // JWT 토큰을 쿠키에 저장
        Cookie jwtCookie = new Cookie("jwt", token);
        jwtCookie.setHttpOnly(true); // JavaScript 접근 방지 (XSS 방어)
        jwtCookie.setPath("/"); // 쿠키의 유효 경로
        // jwtCookie.setSecure(true); // HTTPS 환경에서만 전송 (로컬호스트에서는 생략)
        int maxAgeSeconds = (int) (3600000L / 1000); // 만료 시간을 초 단위로 변환
        jwtCookie.setMaxAge(maxAgeSeconds); // 쿠키의 만료 시간 설정
        response.addCookie(jwtCookie);

//      response.addHeader("Authorization", "Bearer " + token);
    }

    //로그인 실패시
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) {
        response.setStatus(401);
    }
}