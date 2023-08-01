package com.fastcampus.minischeduler.scheduleradmin;

import com.fastcampus.minischeduler.scheduleruser.Progress;
import com.fastcampus.minischeduler.user.User;
import com.fastcampus.minischeduler.user.UserResponse;
import com.fastcampus.minischeduler.user.UserResponse.UserDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SchedulerAdminResponse {

    private List<ScheduleDTO> scheduleDto;
    private CountProcessDTO countProcessDto;
    private UserResponse.GetUserInfoDTO userInfoDTO;

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

        @Builder
        public SchedulerAdminResponseDto(
                User user,
                LocalDateTime scheduleStart,
                LocalDateTime scheduleEnd,
                String title,
                String description,
                String image,
                LocalDateTime createdAt,
                LocalDateTime updatedAt
        ){
            this.user = new UserDto(
                    user.getId(),
                    user.getFullName(),
                    user.getSizeOfTicket(),
                    user.getRole(),
                    user.getProfileImage()
            );
            this.scheduleStart = scheduleStart;
            this.scheduleEnd = scheduleEnd;
            this.title = title;
            this.description = description;
            this.image = image;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }
    }
}
