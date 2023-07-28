package com.fastcampus.minischeduler.scheduleradmin;

import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fastcampus.minischeduler.core.auth.jwt.JwtTokenProvider;
import com.fastcampus.minischeduler.core.auth.session.MyUserDetails;
import com.fastcampus.minischeduler.core.dto.ResponseDTO;
import com.fastcampus.minischeduler.core.exception.Exception403;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class SchedulerAdminController {

    private final SchedulerAdminService schedulerAdminService;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 기획사 일정 조회(메인) : 모든 기획사의 일정이 나옴
     */
    @GetMapping("/scheduleAll")
    public ResponseEntity<List<SchedulerAdminResponseDto>> schedulerList(@RequestHeader(JwtTokenProvider.HEADER) String token) {
        try {
            DecodedJWT decodedJWT = jwtTokenProvider.verify(token.replace(JwtTokenProvider.TOKEN_PREFIX, ""));
            List<SchedulerAdminResponseDto> schedulerAdminResponseDtoList = schedulerAdminService.getSchedulerList();

            return ResponseEntity.ok(schedulerAdminResponseDtoList);
        } catch (SignatureVerificationException | TokenExpiredException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }

    /**
     * 공연 등록/취소 페이지 : 로그인한 기획사가 등록한 일정만 나옴
     */
    @GetMapping("/schedule")
    public ResponseEntity<List<SchedulerAdminResponseDto>> schedulerById(
            @RequestHeader(JwtTokenProvider.HEADER) String token
    ){
        return ResponseEntity.ok(schedulerAdminService.getSchedulerListById(token));
    }

    /**
     * 공연 등록 : 기획사가 공연을 등록함
     */
    @PostMapping("/schedule/create")
    public ResponseEntity<SchedulerAdminResponseDto> createScheduler(
            @RequestBody SchedulerAdminRequestDto schedulerAdminRequestDto , // TODO: request DTO가 필요한가
            @RequestHeader(JwtTokenProvider.HEADER) String token
    ){
        return ResponseEntity.ok(schedulerAdminService.createScheduler(schedulerAdminRequestDto, token));
    }

    /**
     * 공연 일정 삭제 : 공연을 삭제함
     */
    @PostMapping("/schedule/delete/{id}")
    public ResponseEntity<String> deleteScheduler(
            @PathVariable Long id,
            @RequestHeader(JwtTokenProvider.HEADER) String token
    ){

        schedulerAdminService.delete(id, token);

        return ResponseEntity.ok("스케줄 삭제 완료");
    }

    /**
     * 공연 일정 수정 : 공연 일정을 업데이트함
     */
    @PostMapping("/schedule/update/{id}")
    public ResponseEntity<SchedulerAdminResponseDto> updateScheduler(
            @PathVariable Long id,
            @RequestBody SchedulerAdminRequestDto schedulerAdminRequestDto,
            @RequestHeader(JwtTokenProvider.HEADER) String token
    ){
        //스케줄 조회
        SchedulerAdminResponseDto schedulerdto = schedulerAdminService.getSchedulerById(id);
        //로그인한 사용자 id조회
        Long loginUserId = jwtTokenProvider.getUserIdFromToken(token);

        // 스케줄 작성자 id와 로그인한 사용자 id비교
        if(!schedulerdto.getUser().getId().equals(loginUserId)){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); //권한없음
        }

        Long updateId = schedulerAdminService.updateScheduler(id, schedulerAdminRequestDto);
        SchedulerAdminResponseDto updateScheduler = schedulerAdminService.getSchedulerById(updateId);

        return ResponseEntity.ok(updateScheduler);
    }

    /**
     *  공연 기획사별 검색 : 공연 기획사별로 검색가능
     */
    @GetMapping("/schedule/search")
    public ResponseEntity<List<SchedulerAdminResponseDto>> searchScheduler(@RequestParam String keyword){
        List<SchedulerAdminResponseDto> schedulerAdminResponseDtoListFindByFullname = schedulerAdminService.getSchedulerByFullname(keyword);

        return ResponseEntity.ok(schedulerAdminResponseDtoListFindByFullname);
    }

    /**
     * 결재관리 페이지
     * @return
     */
//    @GetMapping("/schedule/confirm/{id}")
//    public ResponseEntity<List<SchedulerAdminResponse.scheduleDTO>> getAdminSchedulerAndUserScheduler(
//            @PathVariable Long id,
//            @AuthenticationPrincipal MyUserDetails myUserDetails
//    ) {
//
//        if(id.longValue() != myUserDetails.getUser().getId()) throw new Exception403("권한이 없습니다");
//
//        List<SchedulerAdminResponse.scheduleDTO> scheduleDTOs = schedulerAdminService.getAdminScheduleDetail(id);
//
//        return ResponseEntity.ok(new ResponseDTO<>(scheduleDTOs).getData());
//    }
}

// user
// id, user_id, scheduler_admin_tb_id, schedule_start, progress, created_at

// admin
// id, user_id, title, description, image, schedule_start, schedule_end, created_at, updated_at

// count(*) progress==WAITING