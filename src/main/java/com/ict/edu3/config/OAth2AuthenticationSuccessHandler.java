package com.ict.edu3.config;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import com.ict.edu3.common.util.JwtUtil;
import com.ict.edu3.domain.auth.service.MyUserDetailService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
// src/main/java/com/ict/edu3/config/OAth2AuthenticationSuccessHandler.java
public class OAth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler { // OAuth2 인증 성공 핸들러 클래스 정의
    private final JwtUtil jwtUtil; // JWT 유틸리티를 위한 변수 선언
    private final MyUserDetailService userDetailService; // 사용자 상세 정보를 위한 서비스 변수 선언

    public OAth2AuthenticationSuccessHandler(JwtUtil jwtUtil, MyUserDetailService userDetailService) { // 생성자
        this.jwtUtil = jwtUtil; // JwtUtil 초기화
        this.userDetailService = userDetailService; // MyUserDetailService 초기화
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException { // 인증 성공 시 호출되는 메서드
        try {
            log.info("OAth2AuthenticationSuccessHandler"); // 핸들러 호출 로그 기록
            if (authentication.getPrincipal() instanceof OAuth2User) { // 인증된 사용자가 OAuth2User 타입인지 확인
                OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal(); // OAuth2User로 캐스팅
                String uri = request.getRequestURI(); // 요청 URI 가져오기
                String provider = ""; // 제공자 초기화
                if (uri.contains("kakao")) { // URI에 'kakao' 포함 여부 확인
                    provider = "kakao"; // 제공자를 'kakao'로 설정
                } else if (uri.contains("naver")) { // URI에 'naver' 포함 여부 확인
                    provider = "naver"; // 제공자를 'naver'로 설정
                } else {
                    provider = "unknown"; // 제공자를 'unknown'으로 설정
                }

                UserDetails id = userDetailService.loadUserByOAuth2User(oAuth2User, provider); // OAuth2User와 제공자를 기반으로 사용자 상세 정보 로드
                String token = jwtUtil.generateToken(id.getUsername()); // 사용자 이름으로 JWT 토큰 생성

                String redirectUrl = String.format( // 클라이언트로 리다이렉트할 URL 생성
                        "http://localhost:3000/login?token=%s&username=%s&name=%s&email=%s",
                        URLEncoder.encode(token, StandardCharsets.UTF_8), // 토큰 인코딩
                        URLEncoder.encode(id.getUsername(), StandardCharsets.UTF_8), // 사용자 이름 인코딩
                        URLEncoder.encode(oAuth2User.getAttribute("name"), StandardCharsets.UTF_8), // 사용자 이름 속성 인코딩
                        URLEncoder.encode(oAuth2User.getAttribute("email"), StandardCharsets.UTF_8)); // 사용자 이메일 속성 인코딩

                response.sendRedirect(redirectUrl); // 클라이언트로 리다이렉트
            }
        } catch (Exception e) { // 예외 처리
            log.info("Exception : " + e); // 예외 로그 기록
            response.sendRedirect("/login?error"); // 에러 발생 시 로그인 페이지로 리다이렉트
        }
    }

}