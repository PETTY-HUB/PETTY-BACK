package io.github.petty;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * HttpSecurity 객체를 사용하여 CSRF 보호, 폼 로그인, HTTP Basic 인증 및 로그아웃 기능을 비활성화하고,
     * 모든 HTTP 요청을 허용하는 SecurityFilterChain 빈을 생성합니다.
     *
     * @param http Spring Security 설정을 구성하기 위한 HttpSecurity 객체
     * @return 구성된 SecurityFilterChain 객체
     * @throws Exception HttpSecurity 구성 중 발생할 수 있는 예외
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // CSRF 비활성화
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // 모든 요청 허용
                )
                .formLogin(form -> form.disable()) // 로그인 비활성화
                .httpBasic(basic -> basic.disable()) // HTTP Basic 인증 비활성화
                .logout(logout -> logout.disable()); // 로그아웃 비활성화

        return http.build();
    }
}
