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
@RequiredArgsConstructor
public class SchedulerAdminController {

    private final SchedulerAdminService schedulerAdminService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/schedulerList")
    public ResponseEntity<List<SchedulerAdminDto>> schedulerList(@RequestHeader(JwtTokenProvider.HEADER) String token) {
        try {
            DecodedJWT decodedJWT = jwtTokenProvider.verify(token.replace(JwtTokenProvider.TOKEN_PREFIX, ""));
            List<SchedulerAdminDto> schedulerAdminDtoList = schedulerAdminService.getSchedulerList();

            return ResponseEntity.ok(schedulerAdminDtoList);
        } catch (SignatureVerificationException | TokenExpiredException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }

    @PostMapping("/createScheduler")
    public ResponseEntity<SchedulerAdminDto> createScheduler(
            @RequestBody SchedulerAdminDto schedulerAdminDto,
            @RequestHeader(JwtTokenProvider.HEADER) String token
    ){

        return ResponseEntity.ok(schedulerAdminService.createScheduler(schedulerAdminDto, token));
    }

    @PostMapping("/updateScheduler/{id}")
    public ResponseEntity<SchedulerAdminDto> updateScheduler(
            @PathVariable Long id,
            @RequestBody SchedulerAdminDto schedulerAdminDto,
            @RequestHeader(JwtTokenProvider.HEADER) String token
    ){
        //스케줄 조회
        SchedulerAdminDto schedulerdto = schedulerAdminService.getSchedulerById(id);
        //로그인한 사용자 id조회
        Long loginUserId = jwtTokenProvider.getUserIdFromToken(token);

        // 스케줄 작성자 id와 로그인한 사용자 id비교
        if(!schedulerdto.getUser().getId().equals(loginUserId)){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); //권한없음
        }

        Long updateId = schedulerAdminService.updateScheduler(id, schedulerAdminDto);
        SchedulerAdminDto updateScheduler = schedulerAdminService.getSchedulerById(updateId);

        return ResponseEntity.ok(updateScheduler);
    }

    @PostMapping("/deleteScheduler/{id}")
    public ResponseEntity<String> deleteScheduler(
            @PathVariable Long id,
            @RequestHeader(JwtTokenProvider.HEADER) String token
    ){

        schedulerAdminService.delete(id, token);

        return ResponseEntity.ok("스케줄 삭제 완료");
    }

    @GetMapping("/searchScheduler")
    public ResponseEntity<List<SchedulerAdminDto>> searchScheduler(@RequestParam String keyword){
        List<SchedulerAdminDto> schedulerAdminDtoListFindByFullname = schedulerAdminService.getSchedulerByFullname(keyword);

        return ResponseEntity.ok(schedulerAdminDtoListFindByFullname);
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