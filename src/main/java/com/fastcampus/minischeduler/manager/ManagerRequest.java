package com.fastcampus.minischeduler.manager;

import lombok.Getter;
import lombok.Setter;

public class ManagerRequest {

    @Getter
    @Setter
    public static class LoginRequestDTO {
        private String username;
        private String password;
    }
}
