package com.ict.edu3.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.ict.edu3.common.util.JwtUtil;
import com.ict.edu3.domain.auth.service.MyUserDetailService;
import com.ict.edu3.jwt.JwtRequestFilter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
    // Start of Selection
    public class SecurityConfig { // SecurityConfig 클래스 정의
    
        private final JwtRequestFilter jwtRequestFilter; // JwtRequestFilter 필드 선언
        private final JwtUtil jwtUtil; // JwtUtil 필드 선언
        private final MyUserDetailService userDetailService; // MyUserDetailService 필드 선언
    
        public SecurityConfig(JwtRequestFilter jwtRequestFilter, JwtUtil jwtUtil, MyUserDetailService userDetailService) { // 생성자 정의
            log.info("SecurityConfig 호출\n"); // 로그에 SecurityConfig 호출 기록
            this.jwtRequestFilter = jwtRequestFilter; // jwtRequestFilter 필드 초기화
            this.jwtUtil = jwtUtil; // jwtUtil 필드 초기화
            this.userDetailService = userDetailService; // userDetailService 필드 초기화
        }
    
        // 서버에 들어는 모든 요청은 SecurityFilterChain을 거친다.
        // addFilterBefore 때문에 JwtRequestFilter가 먼저 실행된다.
    
        // 클라이언트에서 http://localhost:8080/oauth2/authorization/kakao 클릭하면
        // SecurityFilter가 자동으로 OAuthAuthorizationRequestRedirectFilter를 특정 URL에 오면
        // 자동으로 application.yml에 등록을 보고 자동 처리
        @Bean // Bean으로 등록
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception { // SecurityFilterChain 빈 정의
            log.info("SecurityFilterChain 호출\n"); // 로그에 SecurityFilterChain 호출 기록
            http
                    .cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS 설정 적용
                    .csrf(csrf -> csrf.disable()) // CSRF 비활성화
                    // 요청별 권한 설정
                    .authorizeHttpRequests(authorize -> authorize
                            .requestMatchers("/upload/**").permitAll() // /upload/** 경로에 대한 접근 허용
                            .requestMatchers("/oauth2/**").permitAll() // /oauth2/** 경로에 대한 접근 허용
                            // 특정 URL에 인증 없이 허용
                            .requestMatchers("/api/members/join", "/api/members/login",
                                    "/api/guestbook/list", "/api/guestbook/detail/**", "api/guestbook/download/**")
                            .permitAll() // 나열된 API 경로에 대한 접근 허용
                            // 나머지는 인증 필요
                            .anyRequest().authenticated()) // 다른 모든 요청은 인증 필요
    
                    // OAuth2 로그인 설정
                    // successHandler => 로그인 성공 시 호출
                    // userInfoEndpoint => 인증 과정에서 인증된 사용자에 대한 정보를 제공하는 API 엔드포인트
                    // (사용자 정보를 가져오는 역할을 한다.)
                    .oauth2Login(oauth2 -> oauth2
                            .successHandler(oAth2AuthenticationSuccessHandler()) // 로그인 성공 핸들러 설정
                            .userInfoEndpoint(userInfo -> userInfo.userService(oAuth2UserService()))) // 사용자 정보 서비스 설정
                    .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class); // JwtRequestFilter를 UsernamePasswordAuthenticationFilter 앞에 추가
    
            return http.build(); // SecurityFilterChain 빌드 후 반환
        }
    
        @Bean // Bean으로 등록
        OAth2AuthenticationSuccessHandler oAth2AuthenticationSuccessHandler() { // OAuth2 인증 성공 핸들러 빈 정의
            return new OAth2AuthenticationSuccessHandler(jwtUtil, userDetailService); // 핸들러 인스턴스 반환
        }
    
        @Bean // Bean으로 등록
        OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService() { // OAuth2UserService 빈 정의
            return new CustomerOAuth2UserService(); // 커스텀 OAuth2UserService 인스턴스 반환
        }
    
        @Bean // Bean으로 등록
        CorsConfigurationSource corsConfigurationSource() { // CORS 설정 소스 빈 정의
            CorsConfiguration corsConfig = new CorsConfiguration(); // CorsConfiguration 객체 생성
    
            // 허용할 Origin 설정
            corsConfig.setAllowedOrigins(Arrays.asList("http://localhost:3000")); // 허용할 Origin 목록 설정
            // 허용할 HTTP 메서드 설정
            corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS")); // 허용할 HTTP 메서드 목록 설정
            // 허용할 헤더 설정
            corsConfig.setAllowedHeaders(Arrays.asList("*")); // 모든 헤더 허용
            // 인증정보 허용
            corsConfig.setAllowCredentials(true); // 인증 정보 허용 설정
    
            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource(); // URL 기반 CorsConfigurationSource 생성
            source.registerCorsConfiguration("/**", corsConfig); // 모든 경로에 대한 CORS 설정 등록
            return source; // CORS 설정 반환
        }
    
        @Bean // Bean으로 등록
        public PasswordEncoder passwordEncoder() { // PasswordEncoder 빈 정의
            return new BCryptPasswordEncoder(); // BCryptPasswordEncoder 인스턴스 반환
        }
    
        @Bean // Bean으로 등록
        public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception { // AuthenticationManager 빈 정의
            return authConfig.getAuthenticationManager(); // AuthenticationManager 반환
        }
}