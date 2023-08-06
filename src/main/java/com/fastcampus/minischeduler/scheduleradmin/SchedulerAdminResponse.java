package com.fastcampus.minischeduler.scheduleradmin;

import com.fastcampus.minischeduler.scheduleruser.Progress;
import com.fastcampus.minischeduler.user.UserResponse.UserDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SchedulerAdminResponse {

    private UserDto userDto;
    private List<ScheduleDTO> scheduleDto;
    private CountProcessDTO countProcessDto;

    public interface ScheduleDTO {

        // admin 공연 일정 데이터
        Long getAdminScheduleId();
        String getTitle();
        String getDescription();

        // user 티케팅 승인대기 일정 데이터
        Long getUserScheduleId();
        String getFullName();
        String setFullName(String fullName);
        Progress getProgress();
        LocalDateTime getScheduleStart();
    }

    public interface CountProcessDTO {

        // 승인 현황 별 count
        Integer getWaiting();
        Integer getAccepted();
        Integer getRefused();
    }

    @Data
    @NoArgsConstructor
    public static class SchedulerAdminResponseDto {

        @JsonIgnoreProperties({"hibernateLazyInitializer"})
        private UserDto user;

        private LocalDateTime scheduleStart;
        private LocalDateTime scheduleEnd;
        private String title;
        private String description;
        private String image;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
