package com.fastcampus.minischeduler.user;

import com.fastcampus.minischeduler.core.auth.jwt.JwtTokenProvider;
import com.fastcampus.minischeduler.core.auth.session.MyUserDetails;
import com.fastcampus.minischeduler.core.utils.AES256Utils;
import com.fastcampus.minischeduler.log.LoginLog;
import com.fastcampus.minischeduler.log.LoginLogRepository;
import com.fastcampus.minischeduler.user.UserResponse.GetUserInfoDTO;
import lombok.RequiredArgsConstructor;
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
    private final AES256Utils aes256Utils;

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
    public UserResponse.JoinDTO signup(UserRequest.JoinDTO joinDTO) throws Exception {

        // 인코딩
        joinDTO.setPassword(passwordEncoder.encode(joinDTO.getPassword()));
        joinDTO.setEmail(aes256Utils.encryptAES256(joinDTO.getEmail()));
        joinDTO.setFullName(aes256Utils.encryptAES256(joinDTO.getFullName()));

        // 회원 가입
        User userPS = userRepository.save(joinDTO.toEntity());

        // USER 는 티켓 제공, ADMIN 은 제공 안함
        if (userPS.getRole().equals(Role.USER)) userPS.setSizeOfTicket(12 - Calendar.getInstance().get(Calendar.MONTH));
        if (userPS.getRole().equals(Role.ADMIN)) userPS.setSizeOfTicket(null);

        UserResponse.JoinDTO response = new UserResponse.JoinDTO(userPS);
        response.setFullName(aes256Utils.decryptAES256(response.getFullName()));
        response.setEmail(aes256Utils.decryptAES256(response.getEmail()));

        return response;
    }

    @Transactional
    public GetUserInfoDTO getUserInfo(Long userId) throws Exception {

        User userPS = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다"));

        GetUserInfoDTO getUserInfoDTO = GetUserInfoDTO.builder()
                .email(aes256Utils.decryptAES256(userPS.getEmail()))
                .fullName(aes256Utils.decryptAES256(userPS.getFullName()))
                .build();

        return getUserInfoDTO;
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
    public GetUserInfoDTO updateUserInfo(
            UserRequest.UpdateUserInfoDTO updateUserInfoDTO,
            Long userId) throws Exception {

        User userPS = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("사용자 정보를 찾을 수 없습니다"));

        userPS.updateUserInfo(
                passwordEncoder.encode(updateUserInfoDTO.getPassword()),
                updateUserInfoDTO.getProfileImage()
        );

        User updatedUser = userRepository.save(userPS); // 업데이트된 User 객체를 DB에 반영합니다.
        GetUserInfoDTO responseUser = GetUserInfoDTO.builder()
                .fullName(aes256Utils.decryptAES256(updatedUser.getFullName()))
                .email(aes256Utils.decryptAES256(updatedUser.getEmail()))
                .profileImage(updatedUser.getProfileImage())
                .usedTicket(updatedUser.getUsedTicket())
                .leftTicket(updatedUser.getSizeOfTicket())
                .updatedAt(updatedUser.getUpdatedAt())
                .createdAt(updatedUser.getCreatedAt())
                .build();

        return responseUser; // 업데이트되고 DB에 반영된 User 객체를 반환합니다.
    }

    /**
     * 모든 사용자를 조회합니다.
     * @return
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
