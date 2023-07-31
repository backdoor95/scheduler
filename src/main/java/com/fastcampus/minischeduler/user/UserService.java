package com.fastcampus.minischeduler.user;

import com.fastcampus.minischeduler.core.auth.jwt.JwtTokenProvider;
import com.fastcampus.minischeduler.core.auth.session.MyUserDetails;
import com.fastcampus.minischeduler.log.LoginLog;
import com.fastcampus.minischeduler.log.LoginLogRepository;
import com.fastcampus.minischeduler.user.UserResponse.GetUserInfoDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.Calendar;
import java.util.List;
import java.util.NoSuchElementException;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final LoginLogRepository loginLogRepository;
    private final HttpServletRequest httpServletRequest;

    /**
     * 회원가입 메서드입니다.
     * Controller에서 유효성 검사가 완료된 DTO를 받아 비밀번호를 BCrypt 인코딩 후 사용자 정보 테이블(user_tb)에 저장합니다.
     *
     * @param joinDTO
     * @return
     */
    @Transactional
    public UserResponse.JoinDTO signup(UserRequest.JoinDTO joinDTO) {

        // 비밀번호 인코딩
        joinDTO.setPassword(passwordEncoder.encode(joinDTO.getPassword()));

        // 회원 가입
        User userPS = userRepository.save(joinDTO.toEntity());

        // USER 는 티켓 제공, ADMIN 은 제공 안함
        if (userPS.getRole().equals(Role.USER)) userPS.setSizeOfTicket(12 - Calendar.getInstance().get(Calendar.MONTH));
        if (userPS.getRole().equals(Role.ADMIN)) userPS.setSizeOfTicket(null);

        return new UserResponse.JoinDTO(userPS);
    }

    @Transactional
    public GetUserInfoDTO getUserInfo(Long userId) {
        User userPS = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다"));
        return new UserResponse.GetUserInfoDTO(userPS);
    }

    /**
     * 로그인합니다.
     * @param authentication
     * @return
     */
    @Transactional
    public String signin(Authentication authentication) {

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
    }

    @Transactional
    public User updateUserInfo(
            UserRequest.UpdateUserInfoDTO updateUserInfoDTO,
            Long userId) throws DataAccessException {

        User userPS = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("사용자 정보를 찾을 수 없습니다"));

        userPS.updateUserInfo(
                passwordEncoder.encode(updateUserInfoDTO.getPassword()),
                updateUserInfoDTO.getProfileImage()
        );

        User updatedUser = userRepository.save(userPS); // 업데이트된 User 객체를 DB에 반영합니다.

        return updatedUser; // 업데이트되고 DB에 반영된 User 객체를 반환합니다.
    }

    /**
     * 모든 사용자를 조회합니다.
     * @return
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
