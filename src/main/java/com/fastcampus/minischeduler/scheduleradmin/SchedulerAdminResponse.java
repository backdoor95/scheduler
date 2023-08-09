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
@Builder
public class SchedulerAdminResponse {

    private UserDto userDto;
    private CountProcessDTO countProcessDto;
    private List<ImplScheduleDTO> implScheduleDto;

    public interface ScheduleDTO {

        // admin 공연 일정 데이터
        Long getAdminScheduleId();
        String getTitle();
        String getDescription();

        // user 티케팅 승인대기 일정 데이터
        Long getUserScheduleId();
        String getFullName();
        void setFullName(String fullName);
        Progress getProgress();
        LocalDateTime getScheduleStart();
    }

    // 위 인터페이스로 된 DTO를 사용해놓고 이름 디코딩을 해서 fullName을 재정의하니
    // a tuplebackedmap cannot be modified 에러가 발생함
    // 이때문에 이를 구현하는 클래스를 DTO로 만들어 시도하였음 2023-08-08
    @Builder
    @AllArgsConstructor
    public static class ImplScheduleDTO implements ScheduleDTO {

        Long adminScheduleId;
        String title;
        String description;

        // user 티케팅 승인대기 일정 데이터
        Long userScheduleId;
        String fullName;
        Progress progress;
        LocalDateTime scheduleStart;

        @Override
        public Long getAdminScheduleId() {
            return adminScheduleId;
        }

        @Override
        public String getTitle() {
            return title;
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public Long getUserScheduleId() {
            return userScheduleId;
        }

        @Override
        public String getFullName() {
            return fullName;
        }

        @Override
        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        @Override
        public Progress getProgress() {
            return progress;
        }

        @Override
        public LocalDateTime getScheduleStart() {
            return scheduleStart;
        }
    }

    public interface CountProcessDTO {

        // 승인 현황 별 count
        Integer getWaiting();
        Integer getAccepted();
        Integer getRefused();
    }

    @Data
    @NoArgsConstructor
    @Builder
    @AllArgsConstructor
    public static class SchedulerAdminResponseDto {

        @JsonIgnoreProperties({"hibernateLazyInitializer"})
        private UserDto user;

        private Long id;
        private LocalDateTime scheduleStart;
        private LocalDateTime scheduleEnd;
        private String title;
        private String description;
        private String image;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
