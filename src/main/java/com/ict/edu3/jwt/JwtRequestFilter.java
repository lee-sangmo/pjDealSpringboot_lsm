package com.ict.edu3.jwt;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.ict.edu3.common.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

// 토큰을 추출하고, 유효성 검사하여 , 유효한 경우만 사용자 정보를 로드,
// 즉, 보호된 리소스에 대한 접근이 가능한 사용자인지 확인 
@Slf4j
@Component
public class JwtRequestFilter extends OncePerRequestFilter { // OncePerRequestFilter를 상속받아 JWT 요청 필터 클래스 정의
    @Autowired
    private JwtUtil jwtUtil; // JwtUtil 빈 자동 주입

    @Autowired
    private UserDetailsService userDetailsService; // UserDetailsService 빈 자동 주입하여 사용자 정보 로딩

    @Override
    protected void doFilterInternal(@SuppressWarnings("null") HttpServletRequest request, @SuppressWarnings("null") HttpServletResponse response, @SuppressWarnings("null") FilterChain filterChain)
            throws ServletException, IOException { // 필터 체인 내의 필터 작업을 수행하는 메소드 오버라이드

        log.info("JwtRequestFilter 호출\n"); // JwtRequestFilter 호출 로그 출력
        final String requestTokenHeader = request.getHeader("Authorization"); // 요청 헤더에서 "Authorization" 값 가져오기
        String username = null; // 사용자 이름 변수 초기화
        String jwtToken = null; // JWT 토큰 변수 초기화

        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) { // 요청 헤더에 토큰이 존재하고 "Bearer "로 시작하는지 확인
            jwtToken = requestTokenHeader.substring(7); // "Bearer " 제거 후 JWT 토큰 추출
            log.info("JwtRequestFilter 추출메서드\n"); // 토큰 추출 로그 출력
            try {
                username = jwtUtil.extractuserName(jwtToken); // JWT 토큰에서 사용자 이름 추출
                log.info("username : " + username + "\n"); // 추출된 사용자 이름 로그 출력

            } catch (Exception e) {
                logger.warn("JWT Token error"); // JWT 토큰 처리 중 오류 발생 시 경고 로그 출력
            }
        } else {
            logger.warn("JWT Token empty"); // 토큰이 없거나 "Bearer "로 시작하지 않을 경우 경고 로그 출력
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) { // 사용자 이름이 존재하고 현재 인증 정보가 없는 경우

            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username); // 사용자 이름으로 UserDetails 로드
            log.info("userDetails.username : " + userDetails.getUsername() + "\n"); // 로드된 사용자 이름 로그 출력
            log.info("userDetails.password : " + userDetails.getPassword() + "\n"); // 로드된 사용자 비밀번호 로그 출력

            if (jwtUtil.validateToken(jwtToken, userDetails)) { // JWT 토큰의 유효성 검사
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()); // 사용자 상세 정보로 인증 토큰 생성

                usernamePasswordAuthenticationToken
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request)); // 인증 토큰에 요청 세부 정보 설정

                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken); // 인증 정보를 SecurityContext에 설정
            }
        }
        filterChain.doFilter(request, response); // 필터 체인의 다음 필터로 요청 및 응답 전달

    }

}