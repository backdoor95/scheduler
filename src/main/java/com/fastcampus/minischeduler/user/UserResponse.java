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
        private Role role;

        public JoinDTO(User user) {
            this.id = user.getId();
            this.email = user.getEmail();
            this.fullName = user.getFullName();
            this.role = user.getRole();
        }
    }

    @Getter @Setter
    public static class DetailOutDTO{
        private Long id;
        private String email;
        private String fullName;
        private Role role;

        public DetailOutDTO(User user) {
            this.id = user.getId();
            this.email = user.getEmail();
            this.fullName = user.getFullName();
            this.role = user.getRole();
        }
    }

    @Setter
    @Getter
    public static class GetUserInfoDTO {

        private String email;
        private String fullName;
        private Integer usedTicket;
        private Integer leftTicket;
        private String profileImage;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public GetUserInfoDTO(User user) {
            this.email = user.getEmail();
            this.fullName = user.getFullName();
            this.leftTicket = user.getSizeOfTicket();
            this.usedTicket = 12-user.getSizeOfTicket();
            //this.profileImage = user.getProfileImage();
            this.createdAt = user.getCreatedAt();
            this.updatedAt = user.getUpdatedAt();
        }
    }


}
