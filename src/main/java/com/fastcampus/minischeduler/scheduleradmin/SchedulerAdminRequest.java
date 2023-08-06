package com.fastcampus.minischeduler.scheduleradmin;

import com.fastcampus.minischeduler.user.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
public class SchedulerAdminRequest {

    @Data
    @NoArgsConstructor
    public static class SchedulerAdminRequestDto {

        @JsonIgnoreProperties({"hibernateLazyInitializer"})
        private User user;

        private LocalDateTime scheduleStart;
        private LocalDateTime scheduleEnd;
        private String title;
        private String description;
        private String image;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
