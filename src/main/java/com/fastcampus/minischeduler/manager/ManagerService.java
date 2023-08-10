package com.fastcampus.minischeduler.manager;

import com.fastcampus.minischeduler.core.utils.AES256Utils;
import com.fastcampus.minischeduler.user.Role;
import com.fastcampus.minischeduler.user.User;
import com.fastcampus.minischeduler.user.UserRepository;
import com.fastcampus.minischeduler.user.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

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

    public List<UserResponse.UserDto> getUserListByRole(String role) throws GeneralSecurityException {

        List<UserResponse.UserDto> response = new ArrayList<>();
        List<User> users = null;
        if (role == null || role.equals("ALL")) users = managerRepository.findAllUsers();
        else users = managerRepository.findUsersByRole(Role.valueOf(role));

        for (User user : users) {
            UserResponse.UserDto userDto =
                    UserResponse.UserDto.builder()
                            .id(user.getId())
                            .email(aes256Utils.decryptAES256(user.getEmail()))
                            .profileImage(user.getProfileImage())
                            .role(user.getRole())
                            .fullName(aes256Utils.decryptAES256(user.getFullName()))
                            .sizeOfTicket(user.getSizeOfTicket())
                            .build();
            response.add(userDto);
        }

        return response;
    }

    public void updateRoleByUserId(Long userId, String role) {

        if (role.equals("USER")) role = "ADMIN";
        else role = "USER";

        managerRepository.updateRoleByUserId(userId, Role.valueOf(role));
    }
}
