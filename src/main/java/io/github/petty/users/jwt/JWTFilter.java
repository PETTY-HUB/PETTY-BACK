package io.github.petty.users.jwt;

import io.github.petty.users.dto.CustomUserDetails;
import io.github.petty.users.entity.Users;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private static final Logger logger = LoggerFactory.getLogger(JWTFilter.class);


    public JWTFilter(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = null;

        // 1. 쿠키에서 토큰 확인
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("jwt".equals(cookie.getName())) {
                    token = cookie.getValue();
                    logger.debug("Token found in Cookie");
                    break;
                }
            }
        }

        // 2. 쿠키에 토큰이 없으면 Authorization 헤더에서 확인
        if (token == null) {
            String authorization = request.getHeader("Authorization");
            if (authorization != null && authorization.startsWith("Bearer ")) {
                token = authorization.split(" ")[1];
                logger.debug("Token found in Authorization header");
            }
        }

        // 토큰이 없는 경우 필터 체인 계속 진행
        if (token == null) {
            logger.debug("Token not found in either Cookie or Authorization header");
            filterChain.doFilter(request, response);
            return;
        }

        // 토큰 소멸 시간 검증
        if (jwtUtil.isExpired(token)) {
            logger.debug("Token expired");
            SecurityContextHolder.clearContext(); // 인증정보 삭제
            // 토큰이 만료되었을 때 401 Unauthorized 응답을 반환
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 상태 코드
            response.getWriter().write("{\"error\": \"Token has expired\"}"); // 에러 메시지
            return;
        }

        //토큰에서 username과 role 획득
        String username = jwtUtil.getUsername(token);
        String role = jwtUtil.getRole(token);

        //userEntity를 생성하여 값 set
        Users users = new Users();
        users.setUsername(username);
        users.setPassword("temppassword");
        users.setRole(role);

        //UserDetails에 회원 정보 객체 담기
        CustomUserDetails customUserDetails = new CustomUserDetails(users);

        //스프링 시큐리티 인증 토큰 생성
        Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
        //세션에 사용자 등록
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }
}