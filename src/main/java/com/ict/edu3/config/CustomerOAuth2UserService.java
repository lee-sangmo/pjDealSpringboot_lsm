package com.ict.edu3.config;

import java.util.Map;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
// SNS에게 사용자 정보 요청을 처리하고, 사용자 정보를 수신한다. OAuth2User 객체 생성
public class CustomerOAuth2UserService extends DefaultOAuth2UserService {

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("CustomerOAuth2UserService"); // CustomerOAuth2UserService 호출 로그 기록

        // 부모 클래스의 loadUser 메서드를 호출하여 기본 사용자 정보를 가져온다.
        OAuth2User oAuth2User = super.loadUser(userRequest);
        // 사용자 속성 가져오기
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 어떤 제공자인지 알 수 있다.(kakao, naver)
        String provider = userRequest.getClientRegistration().getRegistrationId();
        if (provider.equals("kakao")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account"); // 카카오 계정 정보 추출
            if (kakaoAccount == null) {
                throw new OAuth2AuthenticationException("Kakao error"); // 카카오 계정 정보가 없을 경우 예외 발생
            }
            String email = (String) kakaoAccount.get("email"); // 카카오 이메일 추출

            @SuppressWarnings("unchecked")
            Map<String, Object> properties = (Map<String, Object>) attributes.get("properties"); // 카카오 속성 정보 추출
            if (properties == null) {
                throw new OAuth2AuthenticationException("Kakao error"); // 카카오 속성 정보가 없을 경우 예외 발생
            }
            String name = (String) properties.get("nickname"); // 카카오 닉네임 추출

            log.info("kakao email : " + email); // 카카오 이메일 로그 기록
            log.info("kakao name : " + name); // 카카오 이름 로그 기록

            // 새로운 DefaultOAuth2User 객체 생성 및 반환
            return new DefaultOAuth2User(oAuth2User.getAuthorities(), Map.of(
                    "email", email,
                    "name", name,
                    "id", attributes.get("id")), "email");

        } else if (provider.equals("naver")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = (Map<String, Object>) attributes.get("response"); // 네이버 응답 정보 추출
            if (response == null) {
                throw new OAuth2AuthenticationException("Kakao error"); // 네이버 응답 정보가 없을 경우 예외 발생 (오류 메시지 수정 필요)
            }
            String name = (String) response.get("name"); // 네이버 이름 추출
            String email = (String) response.get("email"); // 네이버 이메일 추출

            log.info("naver email : " + email); // 네이버 이메일 로그 기록
            log.info("naver name : " + name); // 네이버 이름 로그 기록
            // 새로운 DefaultOAuth2User 객체 생성 및 반환
            return new DefaultOAuth2User(oAuth2User.getAuthorities(), Map.of(
                    "email", email,
                    "name", name,
                    "id", response.get("id")), "email");

        }
        return oAuth2User; // 지원되지 않는 제공자의 경우 기본 OAuth2User 반환
    }

}