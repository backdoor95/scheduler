package com.fastcampus.minischeduler.scheduleradmin;

import com.fastcampus.minischeduler.scheduleruser.Progress;
import com.fastcampus.minischeduler.user.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

public class SchedulerAdminResponse {

    @Getter
    public static class scheduleDTO {

        // admin 공연 일정
        @JsonIgnoreProperties({"hibernateLazyInitializer"})
        private User user;

        @JsonIgnoreProperties({"hibernateLazyInitializer"})
        private SchedulerAdmin schedulerAdmin;

//        private String title;
//        private String description;
//        private String image;
//
//        private LocalDateTime scheduleStart;
//        private LocalDateTime scheduleEnd;
//
//        private Progress progress;
//
//        private LocalDateTime createdAt;
//        private LocalDateTime updatedAt;
//
//        // user 티케팅 승인대기 일정
//
//        private LocalDateTime scheduleStart;
//        private LocalDateTime createdAt;

        @Builder
        public scheduleDTO(
                User user,
                SchedulerAdmin schedulerAdmin
//                LocalDateTime scheduleStart,
//                Progress progress,
//                LocalDateTime createdAt
        ){
            this.user = user;
            this.schedulerAdmin = schedulerAdmin;
//            this.scheduleStart = scheduleStart;
//            this.progress = progress;
//            this.createdAt = createdAt;
        }
    }
}
