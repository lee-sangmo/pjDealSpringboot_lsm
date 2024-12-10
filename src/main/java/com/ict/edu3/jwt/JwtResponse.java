package com.ict.edu3.jwt;

 public class JwtResponse { // JwtResponse 클래스 선언
     private final String token; // JWT 토큰을 저장하는 필드

     public JwtResponse(String token) { // 생성자: JWT 토큰을 초기화
         this.token = token; // 토큰 필드에 전달받은 토큰 할당
    }

     public String getToken() { // 토큰을 반환하는 메서드
        return token; // 저장된 JWT 토큰 반환
    }
}