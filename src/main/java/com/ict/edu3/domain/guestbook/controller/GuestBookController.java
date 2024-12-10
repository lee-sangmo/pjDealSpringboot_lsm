package com.ict.edu3.domain.guestbook.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ict.edu3.domain.auth.vo.DataVO;
import com.ict.edu3.domain.guestbook.service.GuestBookService;
import com.ict.edu3.domain.guestbook.vo.GuestBookVO;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@RestController
@RequestMapping("/api/guestbook")
public class GuestBookController { // 게스트북 컨트롤러 클래스 정의
    @Autowired // 게스트북 서비스 자동 주입
    private GuestBookService guestBookService; // 게스트북 서비스 인스턴스 선언

    @Autowired // 비밀번호 인코더 자동 주입
    private PasswordEncoder passwordEncoder; // 비밀번호 인코더 인스턴스 선언
    
    @GetMapping("/list") // "/list" 경로에 대한 GET 요청 매핑
    public DataVO getGuestBookList() { // 게스트북 목록을 가져오는 메서드
        DataVO dataVO = new DataVO(); // 데이터 전달 객체 생성
        try {
            List<GuestBookVO> list = guestBookService.getGuestBookList(); // 게스트북 목록 조회
            dataVO.setSuccess(true); // 성공 여부 설정
            dataVO.setMessage("게스트북 조회 성공"); // 성공 메시지 설정
            dataVO.setData(list); // 조회된 목록 데이터 설정
            log.info("list : " + list); // 조회된 목록 로그 출력
        } catch (Exception e) { // 예외 발생 시
            dataVO.setSuccess(false); // 실패 여부 설정
            dataVO.setMessage("게스트북 조회 실패"); // 실패 메시지 설정
        }
        return dataVO; // 데이터 전달 객체 반환
    }

    @GetMapping("/detail/{gb_idx}") // "/detail/{gb_idx}" 경로에 대한 GET 요청 매핑
    public DataVO getGuestBookDetail(@PathVariable("gb_idx") String gb_idx) { // 특정 게스트북 상세 정보를 가져오는 메서드
        DataVO dataVO = new DataVO(); // 데이터 전달 객체 생성
        try {
            // log.info("gb_idx : " + gb_idx); // gb_idx 정보 로그 (주석 처리)
            GuestBookVO gvo = guestBookService.getGuestBookById(gb_idx); // gb_idx를 기준으로 게스트북 정보 조회
            if (gvo == null) { // 게스트북 정보가 없으면
                dataVO.setSuccess(false); // 실패 여부 설정
                dataVO.setMessage("게스트북 상세보기 실패"); // 실패 메시지 설정
                return dataVO; // 데이터 전달 객체 반환
            }
            dataVO.setSuccess(true); // 성공 여부 설정
            dataVO.setMessage("게스트북 상세보기 성공"); // 성공 메시지 설정
            dataVO.setData(gvo); // 조회된 게스트북 정보 설정
        } catch (Exception e) { // 예외 발생 시
            dataVO.setSuccess(false); // 실패 여부 설정
            dataVO.setMessage("게스트북 상세보기 실패"); // 실패 메시지 설정
        }
        return dataVO; // 데이터 전달 객체 반환
    }

    @GetMapping("/delete/{gb_idx}") // "/delete/{gb_idx}" 경로에 대한 GET 요청 매핑
    public DataVO getGuestBookDelete(@PathVariable("gb_idx") String gb_idx, Authentication authentication) { // 특정 게스트북 삭제를 처리하는 메서드
        DataVO dataVO = new DataVO(); // 데이터 전달 객체 생성
        try {
            // 로그인 여부 확인
            if (authentication == null) { // 인증 정보가 없으면
                dataVO.setSuccess(false); // 실패 여부 설정
                dataVO.setMessage("로그인이 필요합니다."); // 로그인 필요 메시지 설정
                return dataVO; // 데이터 전달 객체 반환
            }
            int result = guestBookService.getGuestBookDelete(gb_idx); // gb_idx를 기준으로 게스트북 삭제 시도
            if (result == 0) { // 삭제 실패 시
                dataVO.setSuccess(false); // 실패 여부 설정
                dataVO.setMessage("게스트북 삭제 실패"); // 삭제 실패 메시지 설정
                return dataVO; // 데이터 전달 객체 반환
            }
            dataVO.setSuccess(true); // 성공 여부 설정
            dataVO.setMessage("게스트북 삭제 성공"); // 삭제 성공 메시지 설정

        } catch (Exception e) { // 예외 발생 시
            dataVO.setSuccess(false); // 실패 여부 설정
            dataVO.setMessage("게스트북 삭제 오류 발생"); // 삭제 오류 메시지 설정
        }
        return dataVO; // 데이터 전달 객체 반환
    }

    @PutMapping("/update/{gb_idx}") // "/update/{gb_idx}" 경로에 대한 PUT 요청 매핑
    public DataVO getGuestBookUpdate(@PathVariable("gb_idx") String gb_idx, @RequestBody GuestBookVO gvo, Authentication authentication) { // 특정 게스트북 업데이트를 처리하는 메서드
        DataVO dataVO = new DataVO(); // 데이터 전달 객체 생성
        try {
            // 로그인 여부 확인
            if (authentication == null) { // 인증 정보가 없으면
                dataVO.setSuccess(false); // 실패 여부 설정
                dataVO.setMessage("로그인이 필요합니다."); // 로그인 필요 메시지 설정
                return dataVO; // 데이터 전달 객체 반환
            }
            
            // gb_idx 설정
            gvo.setGb_idx(gb_idx); // 요청 경로에서 받은 gb_idx를 게스트북 객체에 설정
            
            // 파라미터 유효성 검사
            if (gvo.getGb_name() == null || gvo.getGb_subject() == null || gvo.getGb_content() == null) { // 필수 필드가 누락되었는지 확인
                dataVO.setSuccess(false); // 실패 여부 설정
                dataVO.setMessage("필수 입력값이 누락되었습니다."); // 입력값 누락 메시지 설정
                return dataVO; // 데이터 전달 객체 반환
            }

            int result = guestBookService.getGuestBookUpdate(gvo); // 게스트북 업데이트 시도

            if (result == 0) { // 업데이트 실패 시
                dataVO.setSuccess(false); // 실패 여부 설정
                dataVO.setMessage("게스트북 수정 실패"); // 수정 실패 메시지 설정
                return dataVO; // 데이터 전달 객체 반환
            }
            dataVO.setSuccess(true); // 성공 여부 설정
            dataVO.setMessage("게스트북 수정 성공"); // 수정 성공 메시지 설정

        } catch (NullPointerException | IllegalArgumentException e) { // NullPointerException 또는 IllegalArgumentException 발생 시
            dataVO.setSuccess(false); // 실패 여부 설정
            dataVO.setMessage("게스트북 수정 중 잘못된 입력이 발생했습니다."); // 잘못된 입력 메시지 설정
            log.error("게스트북 수정 오류: ", e); // 오류 로그 기록
        } catch (Exception e) { // 그 외의 예외 발생 시
            dataVO.setSuccess(false); // 실패 여부 설정
            dataVO.setMessage("게스트북 수정 중 예상치 못한 오류가 발생했습니다."); // 예상치 못한 오류 메시지 설정
            log.error("게스트북 수정 오류: ", e); // 오류 로그 기록
        }
        return dataVO;  // 데이터 전달 객체 반환
    }

    @PostMapping("/write") // "/write" 경로에 대한 POST 요청 매핑
    public DataVO getGuestBookWrite(
        @ModelAttribute("data") GuestBookVO gvo, // 모델 속성 "data"로부터 GuestBookVO 객체 바인딩
        Authentication authentication) { // 인증 정보 매개변수

        DataVO dataVO = new DataVO(); // 데이터 전달 객체 생성
        try {
            // 로그인 여부 확인
            if (authentication == null) { // 인증 정보가 없으면
                dataVO.setSuccess(false); // 실패 여부 설정
                dataVO.setMessage("로그인이 필요합니다."); // 로그인 필요 메시지 설정
                return dataVO; // 데이터 전달 객체 반환
            }
            // 로그인 한 사람의 id 추출
            gvo.setGb_id(authentication.getName()); // 인증된 사용자의 이름을 게스트북 객체에 설정
            gvo.setGb_pw(passwordEncoder.encode(gvo.getGb_pw())); // 게스트북 비밀번호를 인코딩하여 설정

            MultipartFile file = gvo.getFile(); // 업로드된 파일 가져오기
            if (file.isEmpty()){
                gvo.setGb_filename(""); // 파일이 없으면 빈 문자열로 설정
            } else {
                UUID uuid = UUID.randomUUID(); // 고유한 UUID 생성
                String f_name = uuid.toString() + "_" + file.getOriginalFilename(); // UUID와 원본 파일 이름을 결합하여 새로운 파일 이름 생성
                gvo.setGb_filename(f_name); // 생성된 파일 이름을 게스트북 객체에 설정

                // windows 외부 경로 설정
                String path = "D:\\upload"; // 파일 업로드 경로 설정
                File uploadDir = new File(path); // 업로드 디렉토리 파일 객체 생성
                // application.yml 수정 : file.upload.dir=D:/upload

                // 디렉토리가 없으면 생성
                if (!uploadDir.exists()) { // 업로드 디렉토리가 존재하지 않으면
                    uploadDir.mkdirs(); // 디렉토리 생성
                }

                // 파일 저장
                file.transferTo(new File(uploadDir, f_name)); // 파일을 지정된 경로에 저장
            }
            // 게스트북 쓰기
            int result = guestBookService.getGuestBookWrite(gvo); // 게스트북 작성 시도

            if (result == 0) { // 작성 실패 시
                dataVO.setSuccess(false); // 실패 여부 설정
                dataVO.setMessage("게스트북 쓰기 실패"); // 쓰기 실패 메시지 설정
                return dataVO; // 데이터 전달 객체 반환
            }
            dataVO.setSuccess(true); // 성공 여부 설정
            dataVO.setMessage("게스트북 쓰기 성공"); // 쓰기 성공 메시지 설정

        } catch (Exception e) { // 예외 발생 시
            log.info("Exception : " + e); // 예외 정보 로그 출력
            dataVO.setSuccess(false); // 실패 여부 설정
            dataVO.setMessage("게스트북 쓰기 오류 발생"); // 쓰기 오류 메시지 설정
        } 
        return dataVO; // 데이터 전달 객체 반환
    }

    @GetMapping("/download/{filename}") // "/download/{filename}" 경로에 대한 GET 요청 매핑
    public ResponseEntity<Resource> downloadFile(@PathVariable("filename") String filename) { // 파일 다운로드를 처리하는 메서드
        try {
            Path filePath = Paths.get("D:/upload/").resolve(filename).normalize(); // 파일 경로 설정
            Resource resource = new UrlResource(filePath.toUri()); // 파일 리소스 생성
            if (!resource.exists()) { // 파일이 존재하지 않으면
                throw new FileNotFoundException("File not found: " + filename); // 파일 없음 예외 발생
            }

            return ResponseEntity.ok() // HTTP 200 OK 응답
                    .contentType(MediaType.APPLICATION_OCTET_STREAM) // 컨텐츠 타입을 바이너리 스트림으로 설정
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"") // 파일 다운로드를 위한 헤더 설정
                    .body(resource); // 응답 바디에 파일 리소스 설정
        } catch (Exception e) { // 예외 발생 시
            log.info("Exception : " + e); // 예외 정보 로그 출력
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // HTTP 404 Not Found 응답 반환
        }
    }

}