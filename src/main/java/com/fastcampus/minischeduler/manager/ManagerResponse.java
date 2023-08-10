package com.fastcampus.minischeduler.manager;

import com.fastcampus.minischeduler.user.Role;
import lombok.Getter;
import lombok.Setter;

public class ManagerResponse {

    @Getter
    @Setter
    public static class AllUsersResponseDTO {
        private Long id;
        private String fullName;
        private String email;
        private Role role;
    }
}
