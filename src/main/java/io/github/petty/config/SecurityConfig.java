package io.github.petty.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.petty.users.jwt.JWTFilter;
import io.github.petty.users.jwt.JWTUtil;
import io.github.petty.users.jwt.LoginFilter;
import io.github.petty.users.oauth2.CustomOAuth2UserService;
import io.github.petty.users.oauth2.OAuth2SuccessHandler;
import io.github.petty.users.repository.UsersRepository;
import io.github.petty.users.service.RefreshTokenService;
import io.github.petty.users.util.CookieUtils;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.time.LocalDateTime;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AuthenticationConfiguration authenticationConfiguration;
    private final JWTUtil jwtUtil;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final RefreshTokenService refreshTokenService;
    private final UsersRepository usersRepository;
    private final CookieUtils cookieUtils;

    public SecurityConfig(
            AuthenticationConfiguration authenticationConfiguration,
            JWTUtil jwtUtil,
            CustomOAuth2UserService customOAuth2UserService,
            OAuth2SuccessHandler oAuth2SuccessHandler,
            RefreshTokenService refreshTokenService,
            UsersRepository usersRepository,
            CookieUtils cookieUtils) {
        this.authenticationConfiguration = authenticationConfiguration;
        this.jwtUtil = jwtUtil;
        this.customOAuth2UserService = customOAuth2UserService;
        this.oAuth2SuccessHandler = oAuth2SuccessHandler;
        this.refreshTokenService = refreshTokenService;
        this.usersRepository = usersRepository;
        this.cookieUtils = cookieUtils;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .authorizeHttpRequests((auth) -> auth
                        // 관리자 전용
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/posts/update-counts").hasRole("ADMIN")
                        .requestMatchers("/manual-sync/**", "/embedding-batch/**").hasRole("ADMIN")

                        // 인증 필요 페이지
                        .requestMatchers("/profile/**").authenticated()
                        .requestMatchers("/posts/detail", "/posts/*/new", "/posts/*/edit").authenticated()
                        .requestMatchers("/flow/**").authenticated()

                        // 인증 필요
                        .requestMatchers(HttpMethod.POST, "/api/posts").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/posts/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/posts/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/posts/{id}/comments").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/comments/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/comments/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/posts/{id}/like").authenticated()
                        .requestMatchers("/api/images/**").authenticated()
                        .requestMatchers("/api/users/me", "/api/check-displayname").authenticated()

                        // 기본 정책
                        .anyRequest().permitAll())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            String requestURI = request.getRequestURI();

                            if (requestURI.startsWith("/api/")) {
                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                response.setContentType("application/json;charset=UTF-8");

                                ObjectMapper mapper = new ObjectMapper();
                                Map<String, Object> errorResponse = Map.of("error", "로그인이 필요합니다");

                                response.getWriter().write(mapper.writeValueAsString(errorResponse));
                            } else {
                                response.sendRedirect("/login");
                            }
                        }))
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(oAuth2SuccessHandler)
                )
                .logout(logout -> logout
                        .logoutUrl("/logout") // 클라이언트 측 로그아웃 요청 URL과 일치
                        .logoutSuccessUrl("/") // 로그아웃 성공 후 리다이렉트 URL
                        .deleteCookies("JSESSIONID", "jwt", "refresh_token") // 로그아웃 시 jwt 쿠키 삭제
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                )
                .addFilterBefore(new JWTFilter(jwtUtil), LoginFilter.class)
                .addFilterAt(new LoginFilter(authenticationManager(authenticationConfiguration), jwtUtil, refreshTokenService, usersRepository, cookieUtils), UsernamePasswordAuthenticationFilter.class)
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}