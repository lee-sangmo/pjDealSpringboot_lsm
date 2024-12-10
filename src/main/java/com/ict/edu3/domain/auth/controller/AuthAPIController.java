package com.ict.edu3.domain.auth.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ict.edu3.common.util.JwtUtil;
import com.ict.edu3.domain.auth.vo.DataVO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/auth")
    // Start of Selection
    public class AuthAPIController { // AuthAPIController 클래스 정의

        @Autowired // JwtUtil 빈 자동 주입
        private JwtUtil jwtUtil; // JwtUtil 인스턴스 선언

        @GetMapping("/test") // "/test" 경로에 대한 GET 요청 매핑
        public String getMethodName() { // getMethodName 메서드 정의
            return "Hello, Spring Boot"; // "Hello, Spring Boot" 문자열 반환
        }

        @PostMapping("/generate-token") // "/generate-token" 경로에 대한 POST 요청 매핑
        public String generateToken(@RequestBody Map<String, String> request) { // generateToken 메서드 정의, 요청 본문을 Map으로 받음

            // 클라이언트가 "username" 키로 정보를 보냈다고 가정
            String username = request.get("username"); // 요청에서 "username" 값을 추출

            // JWT를 생성할 때 추가할 클레임 정보 설정
            Map<String, Object> claims = new HashMap<>(); // 클레임 정보를 담을 Map 생성
            claims.put("role", "USER"); // 클레임에 "role" 키로 "USER" 값 추가

            return jwtUtil.generateToken(username, claims); // JwtUtil을 사용하여 토큰 생성 후 반환
        }

        @PostMapping("/validate-token") // "/validate-token" 경로에 대한 POST 요청 매핑
        public DataVO validateToken(@RequestBody Map<String, String> request) { // validateToken 메서드 정의, 요청 본문을 Map으로 받음
            String token = request.get("token"); // 요청에서 "token" 값을 추출
            log.info("token : ", token); // 토큰 값을 로그에 기록
            return null; // 현재는 null 반환 (추후 구현 필요)
        }
    
}