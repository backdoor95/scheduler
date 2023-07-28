package com.fastcampus.minischeduler.user;

import com.fastcampus.minischeduler.core.exception.Exception400;
import com.fastcampus.minischeduler.core.exception.Exception401;
import com.fastcampus.minischeduler.core.auth.jwt.JwtTokenProvider;
import com.fastcampus.minischeduler.core.auth.session.MyUserDetails;
import com.fastcampus.minischeduler.log.LoginLog;
import com.fastcampus.minischeduler.log.LoginLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final LoginLogRepository loginLogRepository;
    private final HttpServletRequest httpServletRequest;

    /**
     * 회원가입 메서드입니다.
     * Controller에서 유효성 검사가 완료된 DTO를 받아 비밀번호를 BCrypt 인코딩 후 사용자 정보 테이블(user_tb)에 저장합니다.
     * @param joinDTO
     * @return
     */
    @Transactional
    public UserResponse.JoinDTO signup(UserRequest.JoinDTO joinDTO) {

        // 비밀번호 인코딩
        joinDTO.setPassword(passwordEncoder.encode(joinDTO.getPassword()));
        // 회원 가입
        User userPS = userRepository.save(joinDTO.toEntity());

        return new UserResponse.JoinDTO(userPS);
    }

    /**
     *
     * @param loginDTO
     * @return
     */
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
            loginUser.onUpdateLatestLogin();

            // 로그 테이블 기록
            loginLogRepository.save(
                    LoginLog.builder()
                            .userId(loginUser.getId())
                            .userAgent(httpServletRequest.getHeader("User-Agent"))
                            .clientIP(httpServletRequest.getRemoteAddr())
                            .build()
            );

            return JwtTokenProvider.create(loginUser);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw new Exception401("인증되지 않았습니다.");
        }
    }

    @Transactional
    public UserResponse.GetUserInfoDTO getUserInfo(Long userId) {
        User userPS = userRepository.findById(userId)
                .orElseThrow(()->new IllegalArgumentException("사용자 정보를 찾을 수 없습니다"));
        return new UserResponse.GetUserInfoDTO(userPS);
    }

    @Transactional
    public Optional<User> updateUserInfo(UserRequest.UpdateUserInfoDTO updateUserInfoDTO
                               , Long userId) throws DataAccessException{

        userRepository.updateUserInfo(
                passwordEncoder.encode(updateUserInfoDTO.getPassword()),
                updateUserInfoDTO.getProfileImage(),
                userId
                );
        return userRepository.findById(userId);

    }




}
