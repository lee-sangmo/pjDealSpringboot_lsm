package com.ict.edu3.domain.members.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ict.edu3.common.util.JwtUtil;
import com.ict.edu3.domain.auth.vo.DataVO;
import com.ict.edu3.domain.auth.vo.MembersVO;
import com.ict.edu3.domain.members.service.MembersService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/members")
 // Start of Selection
public class MembersController { // 멤버 컨트롤러 클래스 선언
    @Autowired
    private MembersService membersService; // MembersService 자동 주입

    @Autowired
    private PasswordEncoder passwordEncoder; // 비밀번호 인코더 자동 주입

    @Autowired
    private JwtUtil jwtUtil; // JWT 유틸리티 자동 주입

    // private final PasswordEncoder passwordEncoder;
    // public MembersController(PasswordEncoder passwordEncoder) {
    //     this.passwordEncoder = passwordEncoder;
    // }

    @PostMapping("/join")
    public DataVO membersJoin(@RequestBody MembersVO mvo) { // 회원가입 메소드
        DataVO dataVO = new DataVO(); // 데이터 전송 객체 생성
        try {
            // String rawPassword = mvo.getM_pw();
            // String encodePassword = passwordEncoder.encode(rawPassword);
            // mvo.setM_pw(encodePassword);
            // log.info("m_pw : " + mvo.getM_pw());
    
            mvo.setM_pw(passwordEncoder.encode(mvo.getM_pw())); // 비밀번호 인코딩

            int result = membersService.getMembersJoin(mvo); // 회원가입 서비스 호출
            if (result > 0) { // 가입 성공 여부 확인
                dataVO.setSuccess(true); // 성공 설정
                dataVO.setMessage("회원가입 성공"); // 성공 메시지 설정
            }
            return dataVO; // 데이터 반환
            
        } catch (Exception e) {
            log.error("회원가입 중 오류 발생 : {}", e.getMessage()); // 오류 로그 기록
            dataVO.setMessage("회원가입 실패"); // 실패 메시지 설정
            dataVO.setSuccess(false); // 실패 설정
            return dataVO; // 데이터 반환
        }
    }

    @PostMapping("/login")
    public DataVO membersLogin(@RequestBody MembersVO mvo) { // 로그인 메소드
        DataVO dataVO = new DataVO(); // 데이터 전송 객체 생성
        try {
            log.info(mvo + ""); // 회원 정보 로그 기록
            // 사용자 정보 조회
            MembersVO membersVO = membersService.getMembersById(mvo.getM_id()); // 사용자 정보 조회
            if(membersVO == null){
                dataVO.setSuccess(false); // 실패 설정
                dataVO.setMessage("존재하지 않는 ID 입니다"); // 메시지 설정
                return dataVO; // 데이터 반환
            }
            // 비밀번호 검증 받기
            if(!passwordEncoder.matches(mvo.getM_pw(), membersVO.getM_pw())){
                dataVO.setSuccess(false); // 실패 설정
                dataVO.setMessage("비밀번호가 일치하지 않습니다"); // 메시지 설정
                return dataVO; // 데이터 반환
            }
            // JWT 토큰 생성
            String token = jwtUtil.generateToken(mvo.getM_id()); // JWT 토큰 생성
            dataVO.setData(membersVO); // 데이터 설정
            dataVO.setSuccess(true); // 성공 설정
            dataVO.setMessage("로그인 성공"); // 메시지 설정
            dataVO.setToken(token); // 토큰 설정
            return dataVO; // 데이터 반환
            
        } catch (Exception e) {
            return dataVO; // 데이터 반환
        }
    }
}