package com.fastcampus.minischeduler.user;

import com.fastcampus.minischeduler.scheduleruser.Progress;
import com.fastcampus.minischeduler.scheduleruser.SchedulerUser;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

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
        private List<SchedulerUser> schedulerUserList;
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
    public static class UserDto {
        private Long id;
        private String email;
        private String fullName;
        private int sizeOfTicket;
        private Role role;
        private String profileImage;

        public UserDto(
                Long id,
                String fullName,
                int sizeOfTicket,
                Role role,
                String profileImage
        ){
            this.id = id;
            this.fullName = fullName;
            this.sizeOfTicket = sizeOfTicket;
            this.role = role;
            this.profileImage = profileImage;
        }

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

    @Data
    @NoArgsConstructor
    @Builder
    public static class getRoleUserTickeDTO{// role == user
        private String title;
        private LocalDateTime scheduleStart;// schedulerUser의 scheduleStart 를 넣어야함.
        private Progress progress;

        public getRoleUserTickeDTO(String title, LocalDateTime scheduleStart, Progress progress) {
            this.title = title;
            this.scheduleStart = scheduleStart;
            this.progress = progress;
        }
    }

    @Data
    @NoArgsConstructor
    @Builder
    public static class getRoleAdminScheduleDTO{// role == user
        private Progress progress;
        private String title;
        private LocalDateTime scheduleStart;
        private LocalDateTime scheduleEnd;

        public getRoleAdminScheduleDTO(Progress progress, String title, LocalDateTime scheduleStart, LocalDateTime scheduleEnd) {
            this.progress = progress;
            this.title = title;
            this.scheduleStart = scheduleStart;
            this.scheduleEnd = scheduleEnd;
        }
    }



}
