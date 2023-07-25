package com.fastcampus.minischeduler.user;

import lombok.Getter;
import lombok.Setter;

public class UserResponse {

    @Setter
    @Getter
    public static class JoinDTO {
        private Long id;
        private String email;
        private String fullName;

        public JoinDTO(User user) {
            this.id = user.getId();
            this.email = user.getEmail();
            this.fullName = user.getFullName();
        }
    }
}
