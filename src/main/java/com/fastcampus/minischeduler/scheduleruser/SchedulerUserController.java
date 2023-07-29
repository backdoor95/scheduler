package com.fastcampus.minischeduler.scheduleruser;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.fastcampus.minischeduler.core.auth.jwt.JwtTokenProvider;
import com.fastcampus.minischeduler.scheduleradmin.SchedulerAdmin;
import com.fastcampus.minischeduler.scheduleradmin.SchedulerAdminResponse.SchedulerAdminResponseDto;
import com.fastcampus.minischeduler.scheduleradmin.SchedulerAdminService;
import com.fastcampus.minischeduler.scheduleruser.SchedulerUserRequest.SchedulerUserRequestDto;
import com.fastcampus.minischeduler.scheduleruser.SchedulerUserResponse.SchedulerUserResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
     * 전체 일정 조회(메인) : 모든 기획사의 일정과 본인이 신청한 일정이 나옴
     * scheduleStart 날짜 기준으로 param으로 받은 년도와 달에 부합하는 모든 스케줄이 나옴
     * 본인이 신청한 날짜기준으로 year와 month에 부합하는 일정만 나옴
     * year과 month가 null일땐 모든 스케줄이 나옴
     */
    @GetMapping("/schedule")
    public ResponseEntity<Map<String, Object>> schedulerList(
            @RequestHeader(JwtTokenProvider.HEADER) String token,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month
    ) {
        List<SchedulerAdminResponseDto> schedulerAdminResponseDtoList;
        List<SchedulerUserResponseDto> schedulerUserDtoList;

        if(year != null && month != null){
            schedulerAdminResponseDtoList = schedulerAdminService.getSchedulerListByYearAndMonth(year, month);
            schedulerUserDtoList = schedulerUserService.getSchedulerUserListByYearAndMonth(token, year, month);
        }
        else {
            schedulerAdminResponseDtoList = schedulerAdminService.getSchedulerList();
            schedulerUserDtoList = schedulerUserService.getSchedulerUserList(token);
        }
        Map<String, Object> response = new HashMap<>();
        response.put("schedulerAdmin", schedulerAdminResponseDtoList);
        response.put("schedulerUser", schedulerUserDtoList);
        return ResponseEntity.ok(response);
    }

    /**
     * 기획사 검색: 기획사의 이름으로 검색. 검색 내용과 본인의 스케줄이 나옴
     * 정확히 일치하지 않더라도 keyword가 fullname에 포함되어있으면 출력
     * scheduleStart 날짜 기준으로 param으로 받은 년도와 달에 부합하는 모든 스케줄이 나옴
     * 본인이 신청한 날짜기준으로 year와 month에 부합하는 일정만 나옴
     * year과 month가 null일땐 모든 스케줄이 나옴
     */
    @GetMapping("/schedule/search")
    public ResponseEntity<Map<String, Object>> searchSchedulerList(
            @RequestParam String keyword,
            @RequestHeader(JwtTokenProvider.HEADER) String token,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month
    ) {
        List<SchedulerAdminResponseDto> schedulerAdminResponseDtoListFindByFullName
                = schedulerAdminService.getSchedulerByFullName(keyword, year, month);
        List<SchedulerUserResponseDto> schedulerUserDtoList;

        if(year != null && month != null){
            schedulerUserDtoList = schedulerUserService.getSchedulerUserListByYearAndMonth(token, year, month);
        }
        else {
            schedulerUserDtoList = schedulerUserService.getSchedulerUserList(token);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("schedulerAdmin", schedulerAdminResponseDtoListFindByFullName);
        response.put("schedulerUser", schedulerUserDtoList);
        return ResponseEntity.ok(response);
    }

    /**
     * 공연 상세보기 : 공연의 정보를 상세하게 봄
     */
    @GetMapping("/schedule/{id}")
    public SchedulerAdmin scheduleDetail(@PathVariable Long id){
        return schedulerAdminService.getSchedulerAdminById(id);
    }

    /**
     * 티켓팅 등록 : Admin이 등록한 공연 id로 공연을 찾아 user의 schedule을 등록함
     * 한달에 한번만 등록 가능 + 티켓의 수가 1개 이상이여야 함
     */
    @PostMapping("/schedule/create")
    public ResponseEntity<SchedulerUserResponseDto> createUserScheduler(
            @RequestBody SchedulerUserRequestDto schedulerUserDto,
            @RequestHeader(JwtTokenProvider.HEADER) String token,
            @RequestParam Long schedulerAdminId
    ){
        DecodedJWT decodedJWT = jwtTokenProvider.verify(token.replace(JwtTokenProvider.TOKEN_PREFIX, ""));
        Long loginUserId = jwtTokenProvider.getUserIdFromToken(token);

        int userTicketCount = schedulerUserService.getUserTicketCount(loginUserId);
        if(
                userTicketCount > 1 &&
                !schedulerUserService.existingSchedulerInCurrentMonth(
                        loginUserId,
                        schedulerUserDto.getScheduleStart()
                )
        ){
            return ResponseEntity.ok(schedulerUserService.createSchedulerUser(schedulerAdminId, schedulerUserDto, token));
        }
        else {
            //1개 미만이면 권한없음상태
            System.out.println("권한없음");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

    }

    /**
     * 티켓팅 취소 : 사용자가 티케팅 내역을 취소시 티켓을 다시 1개 되돌려줌
     */
    @PostMapping("/schedule/cancel/{id}")
    public ResponseEntity<String> cancelScheduler(
            @PathVariable Long id,
            @RequestHeader(JwtTokenProvider.HEADER) String token
    ){
        schedulerUserService.cancel(id, token);
        return ResponseEntity.ok("티켓팅 취소 완료");
    }
}
