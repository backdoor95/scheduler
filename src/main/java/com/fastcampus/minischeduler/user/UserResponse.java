package com.fastcampus.minischeduler.user;

import lombok.*;

import java.time.LocalDateTime;

public class UserResponse {

    @Getter
    @Setter
    public static class JoinDTO {
        private Long id;
        private String email;
        private String fullName;
        private Role role;
        private String profileImage;

        public JoinDTO(User user) {
            this.id = user.getId();
            this.email = user.getEmail();
            this.fullName = user.getFullName();
            this.role = user.getRole();
            this.profileImage = user.getProfileImage();
        }
    }

    @Getter
    public static class DetailOutDTO{
        private Long id;
        private String email;
        private String fullName;
        private Role role;

        public DetailOutDTO(User user){
            this.id = user.getId();
            this.email = user.getEmail();
            this.fullName = user.getFullName();
            this.role = user.getRole();
        }
    }

    @Setter
    @Getter
    @Builder
    @AllArgsConstructor
    public static class GetUserInfoDTO {

        private String email;
        private String fullName;
        private Integer usedTicket;
        private Integer sizeOfTicket;
        private String profileImage;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        public GetUserInfoDTO(User user) {
            this.email = user.getEmail();
            this.fullName = user.getFullName();
            this.sizeOfTicket = user.getSizeOfTicket();
            this.usedTicket = user.getUsedTicket();
            this.profileImage = user.getProfileImage();
            this.createdAt = user.getCreatedAt();
            this.updatedAt = user.getUpdatedAt();
        }

    }

    @Data
    @NoArgsConstructor
    @Builder
    @AllArgsConstructor
    public static class UserDto {

        private Long id;
        private String email;
        private String fullName;
        private int sizeOfTicket;
        private Role role;
        private String profileImage;

        public UserDto(User user) {
            this.id = user.getId();
            this.email = user.getEmail();
            this.fullName = user.getFullName();
            this.sizeOfTicket = user.getSizeOfTicket();
            this.role = user.getRole();
            this.profileImage = user.getProfileImage();
        }

        public UserDto(UserDto user) {
            this.id = user.getId();
            this.email = user.getEmail();
            this.fullName = user.getFullName();
            this.sizeOfTicket = user.getSizeOfTicket();
            this.role = user.getRole();
            this.profileImage = user.getProfileImage();
        }
    }
}
