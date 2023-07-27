package com.fastcampus.minischeduler.user;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

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

    @Setter
    @Getter
    public static class UserInfoDTO {

        private String email;
        private String fullName;
        private Integer sizeOfTicket;
        private String profileImage;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public UserInfoDTO(User user) {
            this.email = user.getEmail();
            this.fullName = user.getFullName();
            this.sizeOfTicket = user.getSizeOfTicket();
            this.profileImage = user.getProfileImage();
            this.createdAt = user.getCreatedAt();
            this.updatedAt = user.getUpdatedAt();
        }
    }


}
