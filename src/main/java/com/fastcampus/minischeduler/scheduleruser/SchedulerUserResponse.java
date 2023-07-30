package com.fastcampus.minischeduler.scheduleruser;

import com.fastcampus.minischeduler.scheduleradmin.SchedulerAdmin;
import com.fastcampus.minischeduler.user.User;
import com.fastcampus.minischeduler.user.UserDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

public class SchedulerUserResponse {

    @Data
    public static class SchedulerUserResponseDto {

        @JsonIgnoreProperties({"hibernateLazyInitializer"})
        private UserDto user;

        @JsonIgnoreProperties({"hibernateLazyInitializer"})
        private SchedulerAdmin schedulerAdmin;

        private LocalDateTime scheduleStart;
        private Progress progress;
        private LocalDateTime createdAt;

        @Builder
        public SchedulerUserResponseDto(
                User user,
                SchedulerAdmin schedulerAdmin,
                LocalDateTime scheduleStart,
                Progress progress,
                LocalDateTime createdAt
        ){
            this.user = new UserDto(
                    user.getId(),
                    user.getFullName(),
                    user.getSizeOfTicket(),
                    user.getRole(),
                    user.getProfileImage()
            );
            this.schedulerAdmin = schedulerAdmin;
            this.scheduleStart = scheduleStart;
            this.progress = progress;
            this.createdAt = createdAt;
        }
    }
}
