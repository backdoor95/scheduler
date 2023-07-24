package com.fastcampus.minischeduler.user;

import com.fastcampus.minischeduler.errors.exception.Exception400;
import com.fastcampus.minischeduler.errors.exception.Exception401;
import com.fastcampus.minischeduler.security.JwtTokenProvider;
import com.fastcampus.minischeduler.security.MyUserDetails;
import com.fastcampus.minischeduler.user.log.LoginLog;
import com.fastcampus.minischeduler.user.log.LoginLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final LoginLogRepository loginLogRepository;

    @Transactional
    public UserResponse.JoinDTO signup(UserRequest.JoinDTO joinDTO) {
        // 중복 계정 검사
        if (userRepository.findByUsername(joinDTO.getUsername()).isPresent())
            throw new Exception400("username", "이미 존재하는 아이디입니다.");

        // 회원 가입
        joinDTO.setPassword(passwordEncoder.encode(joinDTO.getPassword()));
        User userPS = userRepository.save(joinDTO.toEntity());

        return new UserResponse.JoinDTO(userPS);
    }

    @Transactional(readOnly = false)
    public String signin(
            UserRequest.LoginDTO loginDTO,
            HttpServletRequest request
    ) {
        try{
            Authentication authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    loginDTO.getUsername(),
                                    loginDTO.getPassword()
                            )
                    );
            MyUserDetails myUserDetails = (MyUserDetails)authentication.getPrincipal();
            User user = myUserDetails.getUser();

            // 최종 로그인 날짜 기록
            user.onUpdate();

            // 로그 테이블 기록
            LoginLog loginLog = LoginLog.builder()
                    .userId(user.getId())
                    .userAgent(request.getHeader("User-Agent"))
                    .clientIP(request.getRemoteAddr())
                    .build();
            loginLogRepository.save(loginLog);

            return JwtTokenProvider.create(user);
        } catch (Exception e) {
            throw new Exception401("인증되지 않았습니다.");
        }
    }
}
