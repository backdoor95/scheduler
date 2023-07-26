package com.fastcampus.minischeduler.user;

import com.fastcampus.minischeduler.core.exception.Exception400;
import com.fastcampus.minischeduler.core.exception.Exception401;
import com.fastcampus.minischeduler.core.auth.jwt.JwtTokenProvider;
import com.fastcampus.minischeduler.core.auth.session.MyUserDetails;
import com.fastcampus.minischeduler.log.LoginLog;
import com.fastcampus.minischeduler.log.LoginLogRepository;
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
    private final HttpServletRequest httpServletRequest;

    @Transactional
    public UserResponse.JoinDTO signup(UserRequest.JoinDTO joinDTO) {
        // 중복 계정 검사
        if (userRepository.findByEmail(joinDTO.getEmail()).isPresent())
            throw new Exception400("username", "이미 존재하는 이메일입니다.");

        // 회원 가입
        joinDTO.setPassword(passwordEncoder.encode(joinDTO.getPassword()));
        User userPS = userRepository.save(joinDTO.toEntity());

        return new UserResponse.JoinDTO(userPS);
    }

    @Transactional(readOnly = false)
    public String signin(UserRequest.LoginDTO loginDTO) {
        try{
            Authentication authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    loginDTO.getEmail(),
                                    loginDTO.getPassword()
                            )
                    );
            MyUserDetails myUserDetails = (MyUserDetails) authentication.getPrincipal();
            User loginUser = myUserDetails.getUser();

            // 최종 로그인 날짜 기록
            loginUser.onUpdate();

            // 로그 테이블 기록
            LoginLog loginLog = LoginLog.builder()
                    .userId(loginUser.getId())
                    .userAgent(httpServletRequest.getHeader("User-Agent"))
                    .clientIP(httpServletRequest.getRemoteAddr())
                    .build();
            loginLogRepository.save(loginLog);

            return JwtTokenProvider.create(loginUser);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new Exception401("인증되지 않았습니다.");
        }
    }

}
