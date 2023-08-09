package com.fastcampus.minischeduler.scheduleruser;

import com.fastcampus.minischeduler.core.auth.jwt.JwtTokenProvider;
import com.fastcampus.minischeduler.core.exception.*;
import com.fastcampus.minischeduler.scheduleradmin.SchedulerAdmin;
import com.fastcampus.minischeduler.scheduleradmin.SchedulerAdminResponse.SchedulerAdminResponseDto;
import com.fastcampus.minischeduler.scheduleradmin.SchedulerAdminService;
import com.fastcampus.minischeduler.scheduleruser.SchedulerUserRequest.SchedulerUserRequestDto;
import com.fastcampus.minischeduler.scheduleruser.SchedulerUserResponse.SchedulerUserResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class SchedulerUserController {

    private final SchedulerUserService schedulerUserService;
    private final SchedulerAdminService schedulerAdminService;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 사용자전체 일정 조회페이지(메인) : 모든 기획사의 일정과 본인이 신청한 일정이 나옴
     * @param token : 사용자 인증 토큰
     * @param year : 년도
     * @param month : 달
     * @return  모든 기획사의 등록된 행사 + 본인이 신청한 티켓의 내용을 담은 Map 객체
     * @throws Exception400 요청이 잘못된 경우 올바르지 않은 년도 또는 달
     * @throws Exception500 디코딩에 실패한 경우
     */
    @GetMapping("/schedule")
    public ResponseEntity<Map<String, Object>> getschedulerList(
            @RequestHeader(JwtTokenProvider.HEADER) String token,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month
    ) {
        List<SchedulerAdminResponseDto> schedulerAdminResponseDtoList;
        List<SchedulerUserResponseDto> schedulerUserDtoList;

        //year와 month 유효성검증
        if (year != null && (year < 2000 || year > 3000)) throw new Exception400("year", "유효하지 않은 년도입니다.");
        if (month != null && (month < 1 || month > 12)) throw new Exception400("month", "유효하지 않은 달입니다.");

        Long loginUserId = jwtTokenProvider.getUserIdFromToken(token);
        try {
            if (year != null && month != null) {
                schedulerAdminResponseDtoList = schedulerAdminService.getSchedulerListByYearAndMonth(year, month);
                schedulerUserDtoList = schedulerUserService.getSchedulerUserListByYearAndMonth(loginUserId, year, month);
            } else {
                schedulerAdminResponseDtoList = schedulerAdminService.getSchedulerList();
                schedulerUserDtoList = schedulerUserService.getSchedulerUserList(loginUserId);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("schedulerAdmin", schedulerAdminResponseDtoList);
            response.put("schedulerUser", schedulerUserDtoList);

            return ResponseEntity.ok(response);
        } catch (GeneralSecurityException gse) {
            throw new Exception500("디코딩에 실패하였습니다");
        }
    }

    /**
     * 공연 기획사별 검색 : 기획사이름으로 검색가능
     * @param token : 사용자 인증 토큰
     * @param keyword : 검색 키워드(기획사 이름)
     * @param year : 년도
     * @param month : 달
     * @return 검색한 기획사가 등록한 공연정보 모두와 본인이 신청한 티켓의 내용을 담은 Map 객체
     * @throws Exception400 요청이 잘못된 경우 올바르지 않은 년도 또는 달
     * @throws Exception500 디코딩에 실패한 경우
     */
    @GetMapping("/schedule/search")
    public ResponseEntity<Map<String, Object>> searchSchedulerList(
            @RequestParam String keyword,
            @RequestHeader(JwtTokenProvider.HEADER) String token,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month
    ) {

        //year와 month 유효성검증
        if (year != null && (year < 2000 || year > 3000)) throw new Exception400("year", "유효하지 않은 년도입니다.");
        if(month != null && (month < 1 || month > 12)) throw new Exception400("month", "유효하지 않은 달입니다.");

        try {
            List<SchedulerAdminResponseDto> schedulerAdminResponseDtoListFindByFullName
                    = schedulerAdminService.getSchedulerByFullName(keyword, year, month);
            List<SchedulerUserResponseDto> schedulerUserDtoList;
            Long loginUserId = jwtTokenProvider.getUserIdFromToken(token);

            if (year != null && month != null)
                schedulerUserDtoList = schedulerUserService.getSchedulerUserListByYearAndMonth(loginUserId, year, month);
            else schedulerUserDtoList = schedulerUserService.getSchedulerUserList(loginUserId);

            Map<String, Object> response = new HashMap<>();
            response.put("schedulerAdmin", schedulerAdminResponseDtoListFindByFullName);
            response.put("schedulerUser", schedulerUserDtoList);

            return ResponseEntity.ok(response);
        } catch (GeneralSecurityException gse) {
            throw new Exception500("디코딩에 실패하였습니다");
        }
    }

    /**
     * 공연 상세보기 : 공연의 정보를 상세하게 봄
     * @param token : 사용자 인증 토큰
     * @param adminScheduleId : 선택한 공연의 id값
     * @return 선택한 공연 정보가 담긴 SchedulerAdmin 반환
     * @throws Exception400 id값이 null이거나 0보다 작을경우 / id값에 해당하는 공연정보를 찾을 수 없을경우
     */
    @GetMapping("/schedule/{adminScheduleId}")
    public ResponseEntity<SchedulerAdmin> scheduleDetail(
            @RequestHeader(JwtTokenProvider.HEADER) String token,
            @PathVariable Long adminScheduleId
    ) {

        if (adminScheduleId == null || adminScheduleId <= 0)
            throw new Exception400("adminScheduleId", "잘못된 요청입니다");

        SchedulerAdmin schedulerAdmin = schedulerAdminService.getSchedulerAdminById(adminScheduleId);
        if (schedulerAdmin == null) throw new Exception400("schedulerAdmin", "해당하는 공연의 정보를 찾을 수 없습니다");

        return ResponseEntity.ok(schedulerAdmin);
    }

    /**
     * 티켓팅 등록 페이지 : 기획사가 등록한 공연 id로 공연을 찾아 user의 schedule을 등록함
     * @param token : 사용자 인증 토큰
     * @param schedulerUserDto : 팬이 등록하는 티켓팅 정보
     * @param schedulerAdminId : 기획사가 등록한 공연의 id값
     * @return 팬이 등록한 티켓팅 정보를 반환
     * @throws Exception403 한달에 한번이상 공연을 신청하려는 경우 / 티켓의 수가 부족한 경우
     * @throws Exception500 디코딩에 실패한 경우 / 이미지 파일전송에 실패한 경우
     */
    @PostMapping("/schedule/create")
    public ResponseEntity<SchedulerUserResponseDto> createUserScheduler(
            @RequestBody SchedulerUserRequestDto schedulerUserDto,
            @RequestHeader(JwtTokenProvider.HEADER) String token,
            @RequestParam Long schedulerAdminId
    ) {
        Long loginUserId = jwtTokenProvider.getUserIdFromToken(token);
        int userTicketCount = schedulerUserService.getUserTicketCount(loginUserId);
        if (userTicketCount > 1) {
            if(!schedulerUserService.existingSchedulerInCurrentMonth(loginUserId, schedulerUserDto.getScheduleStart())){
                try {
                    return ResponseEntity
                            .ok(schedulerUserService.createSchedulerUser(schedulerAdminId, schedulerUserDto, loginUserId));
                } catch (GeneralSecurityException gse) {
                    throw new Exception500("디코딩에 실패하였습니다");
                }
            } else throw new Exception403("한달에 한번만 공연을 신청할 수 있습니다.");
        } else throw new Exception403("티켓이 부족합니다.");
    }

    /**
     * 티켓팅 취소 : 사용자가 티케팅 내역을 취소함 취소시 티켓을 다시 1개 되돌려줌
     * @param token : 사용자 인증 토큰
     * @param id : 삭제하려는 티켓의 id
     * @return "티켓팅 취소 완료"
     * @throws Exception400 유효하지 않은 id값일 경우
     * @throws Exception500 디코딩에 실패한 경우
     */
    @PostMapping("/schedule/cancel/{id}")
    public ResponseEntity<String> cancelScheduler(
            @PathVariable Long id,
            @RequestHeader(JwtTokenProvider.HEADER) String token
    ) {
        if (id == null || id <= 0) throw new Exception400("id", "유효하지 않은 id값입니다.");

        try {
            Long loginUserId = jwtTokenProvider.getUserIdFromToken(token);
            schedulerUserService.cancel(id, loginUserId);

            return ResponseEntity.ok("티켓팅 취소 완료");
        } catch (GeneralSecurityException gse) {
            throw new Exception500("디코딩에 실패하였습니다");
        }
    }
}
