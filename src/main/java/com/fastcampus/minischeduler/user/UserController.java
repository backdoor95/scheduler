package com.fastcampus.minischeduler.user;

import com.fastcampus.minischeduler.core.annotation.MyErrorLog;
import com.fastcampus.minischeduler.core.annotation.MyLog;
import com.fastcampus.minischeduler.core.auth.jwt.JwtTokenProvider;
import com.fastcampus.minischeduler.core.dto.ResponseDTO;
import com.fastcampus.minischeduler.core.exception.*;
import com.fastcampus.minischeduler.core.utils.AES256Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final AuthenticationManager authenticationManager;
    private final AES256Utils aes256Utils;

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @GetMapping("/api")
    public ResponseEntity<?> api(@RequestHeader(JwtTokenProvider.HEADER) String token) {

        UserResponse.UserDto response = jwtTokenProvider.getUserInfo(token);
        try {
            response.setEmail(aes256Utils.decryptAES256(response.getEmail()));
            response.setFullName(aes256Utils.decryptAES256(response.getFullName()));

            return ResponseEntity.ok(new ResponseDTO<>(response));
        } catch (GeneralSecurityException gse) {
            throw new Exception500("디코딩에 실패하였습니다");
        }
    }

    /**
     * 회원가입
     * @param joinRequestDTO : 회원가입 시 입력한 정보
     * @param image          : 프로필 사진
     * @return               : UserResponse.JoinDTO
     */
    @MyErrorLog
    @MyLog
    @PostMapping("/join")
    public ResponseEntity<?> join(
            @Valid @RequestPart(value = "dto") UserRequest.JoinDTO joinRequestDTO,
            @RequestPart(value = "file", required = false) MultipartFile image
    ) {
        try { // 중복검사 시 Exception이 발동하는지 확인 2023-08-06
            // 유효성 검사
            if (userRepository.findByEmail(aes256Utils.encryptAES256(joinRequestDTO.getEmail())).isPresent())
                throw new Exception400(joinRequestDTO.getEmail(), "이미 존재하는 이메일입니다"); // 중복 계정 검사
            // joinRequestDTO의 Enum 타입 Role 에 대한 유효성 검사는 컨트롤러 단에서 할 수 없어서 String으로 받기로.
            String role = joinRequestDTO.getRole();
            if (role == null || role.isEmpty() || role.isBlank()) throw new Exception412("권한을 입력해주세요");
            if (!role.equals("USER") && !role.equals("ADMIN")) throw new Exception404("잘못된 요청입니다");
            if (image != null && image.getSize() > 10000000)
                throw new Exception413(String.valueOf(image.getSize()), "파일이 너무 큽니다");

            return ResponseEntity.ok(new ResponseDTO<>(userService.signup(joinRequestDTO, image)));
        } catch (GeneralSecurityException gse) {
            throw new Exception500("디코딩에 실패하였습니다");
        } catch (IOException ioe) {
            throw new Exception500("이미지 파일 전송에 실패하였습니다");
        }
    }

    /**
     * 로그인
     * @param loginRequestDTO : 로그인 요청 정보
     * @return                : 토큰 및 로그인한 사용자 정보
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody @Valid UserRequest.LoginDTO loginRequestDTO
    ) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequestDTO.getEmail(),
                            loginRequestDTO.getPassword()
                    )
            );

            // 프론트 요청으로 사용자 정보 함께 리턴
            Map<String, Object> response = userService.signin(authentication);

            return ResponseEntity.ok()
                    .header(JwtTokenProvider.HEADER, (String) response.get("token"))
                    .body(new ResponseDTO<>(response.get("userInfo")));
        } catch (AuthenticationException ae) {
            throw new Exception401("아이디와 비밀번호를 확인해주세요");
        } catch (GeneralSecurityException gse) {
            throw new Exception500("디코딩에 실패하였습니다");
        }
    }

    /**
     * 마이페이지
     * @param role       : 사용자 권한
     * @param token      : 현재 헤더에 저장되어있는 토큰
     * @return           : UserResponse.getUserInfoDTO
     */
    @GetMapping("/mypage")
    public ResponseEntity<?> getUserInfo(
            @RequestParam(required = false) String role,
            @RequestHeader(JwtTokenProvider.HEADER) String token
    ) {
        if (role == null || role.isBlank()) throw new Exception400("role", "'기획사' 또는 '팬'을 선택해주세요");
        if(!role.equals("ADMIN") && !role.equals("USER")) throw new Exception404("잘못된 요청입니다");

        try {
            if (role.equals("ADMIN"))
                return ResponseEntity.ok(new ResponseDTO<>(userService.getRoleAdminInfo(token)));
            return ResponseEntity.ok(new ResponseDTO<>(userService.getRoleUserInfo(token)));
        } catch (GeneralSecurityException gse) {
            throw new Exception500("디코딩에 실패하였습니다");
        }
    }

    /**
     * 사용자 정보 수정 페이지
     * @param token : 사용자 토큰
     * @return      : GetUserInfoDTO
     */
    @GetMapping("/mypage/update")
    public ResponseEntity<?> getUpdateUserInfo(
            @RequestHeader(JwtTokenProvider.HEADER) String token
    ) {
        try {
            return ResponseEntity.ok(new ResponseDTO<>(userService.getUserInfo(token)));
        } catch (GeneralSecurityException gse) {
            throw new Exception500("디코딩에 실패하였습니다");
        }
    }

    /**
     * 사용자 정보 변경
     * @param token             : 현재 헤더에 저장되어 있는 토큰
     * @param updateUserInfoDTO : 변경하려는 정보
     * @return                  : UserResponse.getUserInfoDTO
     */
    @PostMapping("/mypage/update")
    public ResponseEntity<?> postUpdateUserInfo(
            @RequestHeader(JwtTokenProvider.HEADER) String token,
            @RequestBody @Valid UserRequest.UpdateUserInfoDTO updateUserInfoDTO
    ) {
        try {
            return ResponseEntity.ok(new ResponseDTO<>(userService.updateUserInfo(updateUserInfoDTO, token)));
        } catch (GeneralSecurityException gse) {
            throw new Exception500("디코딩에 실패하였습니다");
        }
    }

    /**
     * 프로필 이미지 등록&변경
     * @param token : 사용자 토큰
     * @param file  : 프로필 이미지
     * @return      : GetUserInfoDTO
     */
    @PostMapping("/mypage/update/image")
    public ResponseEntity<?> postUpdateUserProfileImage(
            @RequestHeader(JwtTokenProvider.HEADER) String token,
            @RequestParam("file") MultipartFile file
    ) {
        // 이미지 파일을 넣지 않았을경우 디폴트 이미지로 변경 필요.
        if (file.isEmpty()) {
            String defaultNameURL = "https://miniproject12storage.s3.ap-northeast-2.amazonaws.com/default.jpg";
            User user = userService.findById(token);
            user.updateUserProfileImage(defaultNameURL);

            return ResponseEntity.ok(new ResponseDTO<>(user));
        }
        try {
            // user 객체를 이용한 작업 수행
            return ResponseEntity.ok(new ResponseDTO<>(userService.updateUserProfileImage(file, token)));
        } catch (GeneralSecurityException gse) {
            throw new Exception500("디코딩에 실패하였습니다");
        } catch (IOException ioe) {
            throw new Exception500("이미지 파일 전송에 실패하였습니다");
        }
    }

    /**
     * 프로필 이미지를 삭제합니다.
     * @param token : 사용자 토큰
     * @return      : GetUserInfoDTO
     */
    @PostMapping("/mypage/delete/image")
    public ResponseEntity<?> postDeleteUserProfileImage(
            @RequestHeader(JwtTokenProvider.HEADER) String token
    ) {
        try {
            return ResponseEntity.ok(new ResponseDTO<>(userService.deleteUserProfileImage(token)));
        } catch (GeneralSecurityException gse) {
            throw new Exception500("디코딩에 실패하였습니다");
        }
    }
}
