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

    @Getter @Setter
    public static class DetailOutDTO{
        private Long id;
        private String email;
        private String fullName;
        private String role;

        public DetailOutDTO(User user) {
            this.id = user.getId();
            this.email = user.getEmail();
            this.fullName = user.getFullName();
            this.role = user.getRole();
        }
    }
}
