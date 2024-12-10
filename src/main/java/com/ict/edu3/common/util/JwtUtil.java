package com.ict.edu3.common.util;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

/**
 * JWT(Json Web Token) 생성 및 검증을 위한 유틸리티 클래스
 */
@Component
public class JwtUtil {
    // application.yml에서 설정한 비밀키를 주입받음
    @Value("${jwt.secret}")
    private String secret;

    // application.yml에서 설정한 토큰 만료 시간을 주입받음
    @Value("${jwt.expiration}")
    private long expiration;

    /**
     * JWT 서명을 위한 비밀키를 생성
     * @return SecretKey 객체
     */
    private SecretKey getKey() {
        // 비밀 키를 UTF-8 인코딩된 바이트 배열로 변환합니다.
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        // 변환된 바이트 배열을 사용하여 HMAC-SHA 키를 생성하고 반환합니다.
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 사용자 ID만을 이용하여 기본 클레임이 포함된 JWT 토큰 생성
     * @param id 사용자 ID
     * @return 생성된 JWT 토큰
     */
    public String generateToken(String id) {
            // 클레임 정보를 담을 Map 객체를 생성합니다.
            Map<String, Object> claims = new HashMap<>();
            // 클레임에 전화번호 정보를 추가합니다.
            claims.put("phone", "010-7777-9999");
            // 사용자 ID와 클레임을 기반으로 JWT 토큰을 생성하여 반환합니다.
            return generateToken(id, claims);
    }

    /**
     * 사용자 정의 클레임을 포함한 JWT 토큰 생성
     * @param username 사용자 이름
     * @param claims 추가할 클레임 정보
     * @return 생성된 JWT 토큰
     */
    public String generateToken(String username, Map<String, Object> claims) {
            // JWT 토큰을 생성하여 반환합니다.
            return Jwts.builder()
                    .setClaims(claims) // 클레임을 설정합니다.
                    .setSubject(username) // 토큰의 주제를 사용자 이름으로 설정합니다.
                    .setIssuedAt(new Date()) // 토큰의 발행 시간을 현재 시간으로 설정합니다.
                    .setExpiration(new Date(System.currentTimeMillis() + expiration)) // 토큰의 만료 시간을 설정합니다.
                    .signWith(getKey(), SignatureAlgorithm.HS256) // 비밀키와 HS256 알고리즘으로 서명합니다.
                    .compact(); // JWT 토큰을 최종적으로 생성합니다.
    }

    /**
     * JWT 토큰에서 모든 클레임 정보를 추출
     * @param token JWT 토큰
     * @return Claims 객체
     */
    private Claims extractAllClaims(String token) {
            return Jwts.parserBuilder()
                    .setSigningKey(getKey()) // JWT 서명을 검증하기 위한 비밀 키 설정
                    .build() // 파서 빌더를 이용해 파서 객체 생성
                    .parseClaimsJws(token) // 제공된 JWT 토큰을 파싱하고 서명 검증
                    .getBody(); // 토큰의 클레임(body) 부분을 반환
    }

    /**
     * JWT 토큰에서 특정 클레임 정보를 추출
     * @param token JWT 토큰
     * @param claimsResolver 클레임 추출을 위한 함수
     * @return 추출된 클레임 값
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
            // JWT 토큰에서 모든 클레임을 추출합니다.
            final Claims claims = extractAllClaims(token);
            // 클레임 리졸버를 적용하여 특정 클레임 값을 반환합니다.
            return claimsResolver.apply(claims);
    }

    /**
     * JWT 토큰에서 사용자 이름 추출
     * @param token JWT 토큰
     * @return 사용자 이름
     */
    public String extractuserName(String token) {
            // 토큰에서 사용자 이름을 추출합니다.
            return extractClaim(token, Claims::getSubject);
    }

    /**
     * JWT 토큰의 유효성 검사
     * @param token JWT 토큰
     * @param userDetails 사용자 정보
     * @return 토큰 유효 여부
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractuserName(token);
            // 사용자 이름이 일치하고 토큰이 만료되지 않았는지 확인
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    /**
     * JWT 토큰의 만료 여부 확인
     * @param token JWT 토큰
     * @return 토큰 만료 여부
     */
    public Boolean isTokenExpired(String token) {
        // 현재 시간과 토큰의 만료 시간을 비교하여, 토큰이 만료되었는지 확인
        return extractExpiration(token).before(new Date());
    }

    /**
     * JWT 토큰에서 만료 시간 추출
     * @param token JWT 토큰
     * @return 만료 시간
     */
    public Date extractExpiration(String token) {
        // 토큰에서 만료 시간을 추출하여 반환합니다.
        return extractClaim(token, Claims::getExpiration);
    }
}