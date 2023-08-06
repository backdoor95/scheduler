package com.fastcampus.minischeduler.user;

import com.fastcampus.minischeduler.core.annotation.MyErrorLog;
import com.fastcampus.minischeduler.core.annotation.MyLog;
import com.fastcampus.minischeduler.core.auth.jwt.JwtTokenProvider;
import com.fastcampus.minischeduler.core.dto.ResponseDTO;
import com.fastcampus.minischeduler.core.exception.*;
import com.fastcampus.minischeduler.core.utils.AES256Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final AuthenticationManager authenticationManager;
    private final AES256Utils aes256Utils;

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    /**
     * 회원가입
     * @param joinRequestDTO : 회원가입 시 입력한 정보
     * @return               : UserResponse.JoinDTO
     * @throws Exception     : 디코딩 시 에러
     */
    @MyErrorLog
    @MyLog
    @PostMapping("/join")
    public ResponseEntity<?> join(
            @RequestBody
            @Valid
            UserRequest.JoinDTO joinRequestDTO
    ) throws Exception {

        // 유효성 검사
        if (userRepository.findByEmail(joinRequestDTO.getEmail()).isPresent())
            throw new Exception400("email", "이미 존재하는 이메일입니다."); // 중복 계정 검사
        // joinRequestDTO의 Enum 타입 Role 에 대한 유효성 검사는 컨트롤러 단에서 할 수 없어서 String으로 받기로.
        String role = joinRequestDTO.getRole();
        if (role == null || role.isEmpty() || role.isBlank()) throw new Exception412("권한을 입력해주세요");
        if (!role.equals("USER") && !role.equals("ADMIN")) throw new Exception412("잘못된 접근입니다. 범위 내 권한을 입력해주세요");

        return ResponseEntity.ok(new ResponseDTO<>(userService.signup(joinRequestDTO)));
    }

    /**
     * 로그인
     * @param loginRequestDTO : 로그인 요청 정보
     * @return                : 토큰
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody
            @Valid
            UserRequest.LoginDTO loginRequestDTO
    ) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            aes256Utils.encryptAES256(loginRequestDTO.getEmail()),
                            loginRequestDTO.getPassword()
                    )
            );

            return ResponseEntity.ok()
                    .header(JwtTokenProvider.HEADER, userService.signin(authentication))
                    .body(new ResponseDTO<>());

        } catch (Exception e) {
            throw new Exception401("아이디와 비밀번호를 확인해주세요");
        }
    }

    /**
     * role에 따라서 마이페이지, 매니저 페이지로 구분됨.
     * @param id         : 사용자 id
     * @param role       : 사용자 권한
     * @param token      : 현재 헤더에 저장되어있는 토큰
     * @return           : UserResponse.getUserInfoDTO
     * @throws Exception : 디코딩에 의한 에러
     */
    @GetMapping("/mypage/{id}") //
    public ResponseEntity<?> getUserInfo(
            @PathVariable Long id,
            @RequestParam("role") String role,
            @RequestHeader(JwtTokenProvider.HEADER) String token
    )throws Exception{

        Long loginUserId = jwtTokenProvider.getUserIdFromToken(token);

        // mypage id와 로그인한 사용자 id비교
        if (!id.equals(loginUserId)) throw new Exception403("권한이 없습니다"); //권한없음\

        if(!role.equals("admin")&&!role.equals("user"))
            throw new Exception400("role", "유효하지 않은 role입니다.");

        if (role.equals("admin")){
            // role == admin
            Long adminId = loginUserId;
            return ResponseEntity.ok(new ResponseDTO<>(userService.getRoleAdminInfo(adminId)));
        }else {
            // role == user
            Long userId = loginUserId;
            return ResponseEntity.ok(new ResponseDTO<>(userService.getRoleUserInfo(userId)));
        }
    }

    /**
     * mypage를 업데이트를 할때, 필요한 user 정보를 반환합니다.
     * @param id
     * @param token
     * @return
     * @throws Exception
     */

    @GetMapping("/mypage/update/{id}")
    public ResponseEntity<?> getUpdateUserInfo(
            @PathVariable Long id,
            @RequestHeader(JwtTokenProvider.HEADER) String token
    ) throws Exception {

        Long loginUserId = jwtTokenProvider.getUserIdFromToken(token);
        // mypage id와 로그인한 사용자 id비교
        if (!id.equals(loginUserId)) throw new Exception403("권한이 없습니다");

        return ResponseEntity.ok(new ResponseDTO<>(userService.getUserInfo(id)));
    }

    /**
     * 사용자 정보변경
     * @param id                : 사용자 id
     * @param token             : 현재 헤더에 저장되어 있는 토큰
     * @param updateUserInfoDTO : 변경하려는 정보
     * @return                  : UserResponse.getUserInfoDTO
     * @throws Exception        : 디코딩 시 발생할 에러
     */
    @PostMapping("/mypage/update/{id}")
    public ResponseEntity<?> postUpdateUserInfo(
            @PathVariable Long id,
            @RequestHeader(JwtTokenProvider.HEADER) String token,
            @RequestBody
            @Valid
            UserRequest.UpdateUserInfoDTO updateUserInfoDTO
    ) {
        try {
            Long loginUserId = jwtTokenProvider.getUserIdFromToken(token);

            // mypage update 작성자 id와 로그인한 사용자 id비교
            if (!id.equals(loginUserId)) throw new Exception401("권한이 없습니다");
            // user 객체를 이용한 작업 수행
            return ResponseEntity.ok(new ResponseDTO<>(userService.updateUserInfo(updateUserInfoDTO, id)));
        } catch (IOException e) {
            throw new RuntimeException("프로필 이름, 비밀번호 변경 실패");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 프로필 이미지 등록&변경 업로드.
     * @param id
     * @param token
     * @param file
     * @return
     */
    @PostMapping("/mypage/update/image/{id}")
    public ResponseEntity<?> postUpdateUserProfileImage(
            @PathVariable Long id,
            @RequestHeader(JwtTokenProvider.HEADER) String token,
            @RequestParam("file") MultipartFile file
    ) {

            Long loginUserId = jwtTokenProvider.getUserIdFromToken(token);

            // mypage update image 작성자 id와 로그인한 사용자 id비교
            if (!id.equals(loginUserId)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); //권한없음
            }

            // 이미지 파일을 넣지 않았을경우 디폴트 이미지로 변경 필요.
            if(file.isEmpty()){
                String defaultNameURL = "https://miniproject12storage.s3.ap-northeast-2.amazonaws.com/default.jpg";
                User user = userService.findById(id);
                user.updateUserProfileImage(defaultNameURL);

                return ResponseEntity.ok(new ResponseDTO<>(user));
            }

            UserResponse.GetUserInfoDTO getUserInfoDTO = null;
            try {
                getUserInfoDTO = userService.updateUserProfileImage(file, id);
            } catch (Exception e) {
                throw new Exception500("암호화 에러");
            }
            // user 객체를 이용한 작업 수행
            ResponseDTO<?> responseDTO = new ResponseDTO<>(getUserInfoDTO);

            return ResponseEntity.ok(responseDTO);

    }

    /**
     * 프로필 이미지를 삭제합니다.
     * @param id
     * @param token
     * @return
     */

    @PostMapping("/mypage/delete/image/{id}")
    public ResponseEntity<?> postDeleteUserProfileImage(
            @PathVariable Long id,
            @RequestHeader(JwtTokenProvider.HEADER) String token
    ) {
        try {
            Long loginUserId = jwtTokenProvider.getUserIdFromToken(token);

            // mypage update image 작성자 id와 로그인한 사용자 id비교
            if (!id.equals(loginUserId)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); //권한없음
            }

            UserResponse.GetUserInfoDTO getUserInfoDTO = userService.deleteUserProfileImage(id);

            // user 객체를 이용한 작업 수행
            ResponseDTO<?> responseDTO = new ResponseDTO<>(getUserInfoDTO);

            return ResponseEntity.ok(responseDTO);
        } catch (IOException e) {
            throw new RuntimeException("프로필 이름, 비밀번호 변경 실패");
        } catch (Exception e) {
            throw new RuntimeException("디코딩중 문제발생?");
        }
    }
}
