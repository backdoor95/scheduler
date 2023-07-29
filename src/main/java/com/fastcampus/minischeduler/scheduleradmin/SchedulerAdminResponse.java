package com.fastcampus.minischeduler.scheduleradmin;

import com.fastcampus.minischeduler.scheduleruser.Progress;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SchedulerAdminResponse {

    private List<ScheduleDTO> scheduleDto;
    private CountProcessDTO countProcessDto;

    //    @Builder
//    public static class ScheduleDTO {
//
//        // admin 공연 일정 데이터
//        private Long adminScheduleId;
//        private String title;
//        private String description;
//
//        // user 티케팅 승인대기 일정 데이터
//        private Long userScheduleId;
//        private String fullName;
//        private Progress progress;
//        private LocalDateTime scheduleStart;
//    }

    public interface ScheduleDTO {

        // admin 공연 일정 데이터
        Long getAdminScheduleId(); //
        String getTitle();
        String getDescription();

        // user 티케팅 승인대기 일정 데이터
        Long getUserScheduleId(); //
        String getFullName(); //
        Progress getProgress();
        LocalDateTime getScheduleStart(); //
    }

//    @Builder
//    public static class CountProcessDTO {
//
//        // 승인 현황 별 count
//        private Integer waiting;
//        private Integer accepted;
//        private Integer refused;
//    }

    public interface CountProcessDTO {

        // 승인 현황 별 count
        Integer getWaiting();
        Integer getAccepted();
        Integer getRefused();
    }

}
