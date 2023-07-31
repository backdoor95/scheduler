package com.fastcampus.minischeduler.user;

import com.fastcampus.minischeduler.core.annotation.MyErrorLog;
import com.fastcampus.minischeduler.core.annotation.MyLog;
import com.fastcampus.minischeduler.core.auth.jwt.JwtTokenProvider;
import com.fastcampus.minischeduler.core.dto.ResponseDTO;
import com.fastcampus.minischeduler.core.exception.Exception400;
import com.fastcampus.minischeduler.core.exception.Exception401;
import com.fastcampus.minischeduler.core.exception.Exception412;
import com.fastcampus.minischeduler.user.UserRequest.UpdateUserInfoDTO;
import com.fastcampus.minischeduler.user.UserResponse.GetUserInfoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final AuthenticationManager authenticationManager;

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @MyErrorLog
    @MyLog
    @PostMapping("/join")
    public ResponseEntity<?> join(
            @RequestBody
            @Valid
            UserRequest.JoinDTO joinRequestDTO
    ) {
        // 유효성 검사
        if (userRepository.findByEmail(joinRequestDTO.getEmail()).isPresent())
            throw new Exception400("email", "이미 존재하는 이메일입니다."); // 중복 계정 검사
        // joinRequestDTO의 Enum 타입 Role 에 대한 유효성 검사는 컨트롤러 단에서 할 수 없어서 String으로 받기로.
        String role = joinRequestDTO.getRole();
        if (role == null || role.isEmpty() || role.isBlank()) throw new Exception412("권한을 입력해주세요");
        if (!role.equals("USER") && !role.equals("ADMIN")) throw new Exception412("잘못된 접근입니다. 범위 내 권한을 입력해주세요");

        return ResponseEntity.ok(new ResponseDTO<>(userService.signup(joinRequestDTO)));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody
            @Valid
            UserRequest.LoginDTO loginRequestDTO
    ) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequestDTO.getEmail(),
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

    @GetMapping("/mypage/{id}")
    public ResponseEntity<?> getUserInfo(
            @PathVariable Long id,
            @RequestParam("role") String role,
            @RequestHeader(JwtTokenProvider.HEADER) String token
    ){
        Long loginUserId = jwtTokenProvider.getUserIdFromToken(token);

        // mypage id와 로그인한 사용자 id비교
        if(!id.equals(loginUserId)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); //권한없음
        if(role.equals("admin")) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); //권한없음

        Long userId = jwtTokenProvider.getUserIdFromToken(token);

        UserResponse.GetUserInfoDTO getUserInfoDTO = userService.getUserInfo(userId);
        System.out.println("*******"+ getUserInfoDTO+"******");
        ResponseDTO<?> responseDTO = new ResponseDTO<>(getUserInfoDTO);

        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/mypage/update/{id}")
    public ResponseEntity<?> getUpdateUserInfo(
            @PathVariable Long id,
            @RequestHeader(JwtTokenProvider.HEADER) String token
    ){

        Long loginUserId = jwtTokenProvider.getUserIdFromToken(token);

        // mypage id와 로그인한 사용자 id비교
        if(!id.equals(loginUserId)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); //권한없음

        GetUserInfoDTO getUserInfoDTO = userService.getUserInfo(id);
        // user 객체를 이용한 작업 수행
        ResponseDTO<?> responseDTO = new ResponseDTO<>(getUserInfoDTO);

        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping("/mypage/update/{id}")
    public ResponseEntity<?> postUpdateUserInfo(
            @PathVariable Long id,
            @RequestHeader(JwtTokenProvider.HEADER) String token,
            @RequestBody
            @Valid
            UpdateUserInfoDTO updateUserInfoDTO,
            Errors errors
    ) {
        if (errors.hasErrors()) return null;

        Long loginUserId = jwtTokenProvider.getUserIdFromToken(token);

        // mypage update 작성자 id와 로그인한 사용자 id비교
        if (!id.equals(loginUserId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); //권한없음
        }

        User userPS = userService.updateUserInfo(updateUserInfoDTO, id);
        // user 객체를 이용한 작업 수행
        ResponseDTO<?> responseDTO = new ResponseDTO<>(userPS);

        return ResponseEntity.ok(responseDTO);
    }
}
