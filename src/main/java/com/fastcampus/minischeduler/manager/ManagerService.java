package com.fastcampus.minischeduler.manager;

import com.fastcampus.minischeduler.core.exception.Exception500;
import com.fastcampus.minischeduler.core.utils.AES256Utils;
import com.fastcampus.minischeduler.user.Role;
import com.fastcampus.minischeduler.user.User;
import com.fastcampus.minischeduler.user.UserRepository;
import com.fastcampus.minischeduler.user.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.security.GeneralSecurityException;

import static com.fastcampus.minischeduler.core.exception.ErrorCode.FAIL_DECODING;

@Service
@RequiredArgsConstructor
public class ManagerService {

    private final ManagerRepository managerRepository;
    private final UserRepository userRepository;

    private final AES256Utils aes256Utils;

    public boolean isNotExistId(String username) {
        return managerRepository.findByUsername(username) == null;
    }

    public Manager login(ManagerRequest.LoginRequestDTO loginRequestDto) {
        return managerRepository
                .findByUsernameAndPassword(loginRequestDto.getUsername(), loginRequestDto.getPassword());
    }

    public Page<UserResponse.UserDto> getUserListByRole(String role, Pageable pageable) {

        Page<User> userPage = null;

        if (role == null || role.equals("ALL")) userPage =
                userRepository.findAll(pageable);
        if (role != null && !role.equals("ALL")) userPage =
                managerRepository.findUsersByRole(Role.valueOf(role), pageable);

        return userPage.map(m -> {
            try {
                return UserResponse.UserDto.builder()
                        .id(m.getId())
                        .email(aes256Utils.decryptAES256(m.getEmail()))
                        .profileImage(m.getProfileImage())
                        .role(m.getRole())
                        .fullName(aes256Utils.decryptAES256(m.getFullName()))
                        .sizeOfTicket(m.getSizeOfTicket())
                        .build();
            } catch (GeneralSecurityException gse) {
                throw new Exception500(FAIL_DECODING.getMessage());
            }
        });
    }

    public void updateRoleByUserId(Long userId, String role) {

        if (role.equals("USER")) role = "ADMIN";
        else role = "USER";

        managerRepository.updateRoleByUserId(userId, Role.valueOf(role));
    }
}
