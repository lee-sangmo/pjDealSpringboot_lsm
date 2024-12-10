package com.ict.edu3.domain.auth.service;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.ict.edu3.domain.auth.mapper.AuthMapper;
import com.ict.edu3.domain.auth.vo.MembersVO;
import com.ict.edu3.domain.auth.vo.UserVO;
import com.ict.edu3.domain.members.mapper.MembersMapper;

import lombok.extern.slf4j.Slf4j;
    
@Slf4j
@Service
 // Start of Selection
public class MyUserDetailService implements UserDetailsService { // UserDetailsService 인터페이스 구현

    @Autowired // AuthMapper 빈 자동 주입
    private AuthMapper authMapper; // 인증 매퍼 인스턴스 선언

    @Autowired // MembersMapper 빈 자동 주입
    private MembersMapper membersMapper; // 멤버 매퍼 인스턴스 선언


    @Override // 사용자 이름으로 사용자 정보 로드
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException { 
        UserVO member = authMapper.selectMember(username); // 사용자 이름으로 사용자 정보 조회
        if (member == null) { // 사용자가 존재하지 않으면
            throw new UsernameNotFoundException("없는 아이디 입니다."); // 예외 발생
        }
        return new User(member.getM_id(), member.getM_pw(), new ArrayList<>()); // 사용자 ID와 비밀번호로 User 객체 생성 후 반환
    }

    // DB에서 개인 정보 추출
    public UserVO getUserDetail(String m_id) { 
        return authMapper.selectMember(m_id); // 사용자 ID로 사용자 정보 조회 및 반환
    }

    // OAuth2 사용자 정보를 기반으로 UserDetails 로드
    public UserDetails loadUserByOAuth2User(OAuth2User oAuth2User, String provider) {
        String email = oAuth2User.getAttribute("email"); // OAuth2 사용자 이메일 추출
        String name = oAuth2User.getAttribute("name"); // OAuth2 사용자 이름 추출

        // 카카오의 ID는 Long 타입이며 String으로 변환되지 않음
        String id = ""; // 사용자 ID 초기화
        MembersVO mvo = new MembersVO(); // MembersVO 객체 생성
        if (provider.equals("kakao")) { // 제공자가 카카오인 경우
            Long kakaoId = oAuth2User.getAttribute("id"); // 카카오 사용자 ID 추출
            id = String.valueOf(kakaoId); // 카카오 ID를 String으로 변환
            mvo.setSns_email_kakao(email); // 카카오 이메일 설정
            mvo.setM_name(name); // 사용자 이름 설정
            mvo.setM_id(id); // 사용자 ID 설정
            mvo.setSns_provider("kakao"); // 제공자 설정

            // log.info("Kakao 로그인 시도 - ID: {}, Email: {}, Name: {}", id, email, name); // 카카오 로그인 시도 로그

        } else if (provider.equals("naver")) { // 제공자가 네이버인 경우
            id = oAuth2User.getAttribute("id"); // 네이버 사용자 ID 추출
            mvo.setSns_email_naver(email); // 네이버 이메일 설정
            mvo.setM_name(name); // 사용자 이름 설정
            mvo.setM_id(id); // 사용자 ID 설정
            mvo.setSns_provider("naver"); // 제공자 설정

            // log.info("Naver 로그인 시도 - ID: {}, Email: {}, Name: {}", id, email, name); // 네이버 로그인 시도 로그
        }
        // 아이디가 존재하면 DB에 있는 것이고, 그렇지 않으면 새로운 사용자임
        MembersVO mvo2 = membersMapper.findUserByProvider(mvo); // 제공자 정보를 기반으로 사용자 검색
        if (mvo2 == null) { // 사용자가 DB에 없으면
            membersMapper.insertUser(mvo); // 사용자 정보 DB에 삽입
            // log.info("신규 사용자로 데이터베이스에 저장 - ID: {}", mvo.getM_id()); // 신규 사용자 저장 로그
        } else { // 사용자가 이미 DB에 있으면
            // log.info("기존 사용자 - ID: {}", mvo2.getM_id()); // 기존 사용자 로그
        }
        return new User(mvo.getM_id(), "", new ArrayList<>()); // 사용자 ID로 User 객체 생성 후 반환
    }
}