package com.fastcampus.minischeduler.scheduleradmin;

import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fastcampus.minischeduler.core.auth.jwt.JwtTokenProvider;
import com.fastcampus.minischeduler.core.auth.session.MyUserDetails;
import com.fastcampus.minischeduler.core.dto.ResponseDTO;
import com.fastcampus.minischeduler.core.exception.*;
import com.fastcampus.minischeduler.core.utils.AES256Utils;
import com.fastcampus.minischeduler.scheduleradmin.SchedulerAdminRequest.SchedulerAdminRequestDto;
import com.fastcampus.minischeduler.scheduleradmin.SchedulerAdminResponse.SchedulerAdminResponseDto;
import com.fastcampus.minischeduler.scheduleruser.Progress;
import com.fastcampus.minischeduler.scheduleruser.SchedulerUser;
import com.fastcampus.minischeduler.scheduleruser.SchedulerUserRepository;
import com.fastcampus.minischeduler.user.User;
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

    private final SchedulerUserRepository schedulerUserRepository;
    private final SchedulerAdminService schedulerAdminService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AES256Utils aes256Utils;

    /**
     * 기획사 일정 조회(메인) : 모든 기획사의 일정이 나옴
     * scheduleStart 날짜 기준으로 param으로 받은 년도와 달에 부합하는 모든 스케줄이 나옴
     * year과 month가 null일땐 모든 스케줄이 나옴
     */
    @GetMapping("/scheduleAll")
    public ResponseEntity<List<SchedulerAdminResponseDto>> schedulerList(
            @RequestHeader(JwtTokenProvider.HEADER) String token,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month
    ) throws Exception {
        try {
            DecodedJWT decodedJWT =
                    jwtTokenProvider.verify(token.replace(
                            JwtTokenProvider.TOKEN_PREFIX,
                            "")
                    );
            List<SchedulerAdminResponseDto> schedulerAdminResponseDtoList;

            if(year != null && month != null){
                schedulerAdminResponseDtoList = schedulerAdminService.getSchedulerListByYearAndMonth(year, month);
            }
            else {
                schedulerAdminResponseDtoList = schedulerAdminService.getSchedulerList();
            }
            return ResponseEntity.ok(schedulerAdminResponseDtoList);
        } catch (SignatureVerificationException | TokenExpiredException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }

    // TODO : year랑 month 받아서 넘겨주는거 하나랑 상관없이 넘겨주는거 하나 총 두개를 넘겨줘야되는지 확인하기
    /**
     * 공연 등록/취소 페이지 : 로그인한 기획사가 등록한 일정만 나옴
     */
    @GetMapping("/schedule")
    public ResponseEntity<List<SchedulerAdminResponseDto>> getSchedulerList(
            @RequestHeader(JwtTokenProvider.HEADER) String token
    ) throws Exception {
        return ResponseEntity.ok(schedulerAdminService.getSchedulerListById(token));
    }

    /**
     * 공연 등록 : 기획사가 공연을 등록함
     */
    @PostMapping("/schedule/create")
    public ResponseEntity<SchedulerAdminResponseDto> createScheduler(
            @RequestBody SchedulerAdminRequestDto schedulerAdminRequestDto ,
            @RequestHeader(JwtTokenProvider.HEADER) String token
    ) throws Exception {
        return ResponseEntity.ok(schedulerAdminService.createScheduler(schedulerAdminRequestDto, token));
    }

    /**
     * 공연 일정 삭제 : 공연을 삭제함
     */
    @PostMapping("/schedule/delete/{id}")
    public ResponseEntity<String> deleteScheduler(
            @PathVariable Long id,
            @RequestHeader(JwtTokenProvider.HEADER) String token
    ) throws Exception {

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
    ) throws Exception {
        //스케줄 조회
        SchedulerAdminResponseDto schedulerDto = schedulerAdminService.getSchedulerById(id);
        //로그인한 사용자 id조회
        Long loginUserId = jwtTokenProvider.getUserIdFromToken(token);

        // 스케줄 작성자 id와 로그인한 사용자 id비교
        if(!schedulerDto.getUser().getId().equals(loginUserId)){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); //권한없음
        }

        Long updateId = schedulerAdminService.updateScheduler(id, schedulerAdminRequestDto);
        SchedulerAdminResponseDto updateScheduler = schedulerAdminService.getSchedulerById(updateId);

        return ResponseEntity.ok(updateScheduler);
    }

    /**
     *  공연 기획사별 검색 : 공연 기획사별로 검색가능
     *  scheduleStart 날짜 기준으로 param으로 받은 년도와 달에 부합하는 모든 스케줄이 나옴
     *  year과 month가 null일땐 모든 스케줄이 나옴
     */
    @GetMapping("/schedule/search")
    public ResponseEntity<List<SchedulerAdminResponseDto>> searchScheduler(
            @RequestParam String keyword,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month
    ) throws Exception {
        List<SchedulerAdminResponseDto> schedulerAdminResponseDtoListFindByFulName
                = schedulerAdminService.getSchedulerByFullName(keyword, year, month);

        return ResponseEntity.ok(schedulerAdminResponseDtoListFindByFulName);
    }

    /**
     * 결재관리 페이지
     * @return
     */
    @GetMapping("/schedule/confirm/{id}")
    public ResponseEntity<?> getAdminSchedulerAndUserScheduler(
            @PathVariable Long id,
            @AuthenticationPrincipal MyUserDetails myUserDetails,
            @RequestHeader(JwtTokenProvider.HEADER) String token
    ) throws Exception {

        Long loginUserId = jwtTokenProvider.getUserIdFromToken(token);
        if(!myUserDetails.getUser().getId().equals(loginUserId)) throw new Exception401("인증되지 않았습니다");
        if(!myUserDetails.getUser().getId().equals(id)) throw new Exception403("권한이 없습니다");

        return ResponseEntity.ok(new ResponseDTO<>(schedulerAdminService.getAdminScheduleDetail(id)));
    }

    /**
     * 선택한 티켓을 승인하거나 거절합니다.
     * @param id : 사용자(기획사) id
     * @param schedulerUserId
     * @param progress : 승인/거절
     * @param myUserDetails
     * @return
     */
    @PostMapping("/schedule/confirm/{id}/{schedulerUserId}")
    public ResponseEntity<?> confirmSchedule(
            @PathVariable Long id,
            @PathVariable Long schedulerUserId,
            @RequestParam String progress,
            @AuthenticationPrincipal MyUserDetails myUserDetails,
            @RequestHeader(JwtTokenProvider.HEADER) String token
    ) {
        // 유효성 검사
        Long loginUserId = jwtTokenProvider.getUserIdFromToken(token);
        if(!myUserDetails.getUser().getId().equals(loginUserId)) throw new Exception401("인증되지 않았습니다");
        if(!myUserDetails.getUser().getId().equals(id)) throw new Exception403("권한이 없습니다");

        SchedulerUser schedulerUser = schedulerUserRepository.findById(schedulerUserId).get();
        User fan = schedulerUser.getUser();
        if(fan == null) throw new Exception400("해당 사용자는 존재하지 않습니다");

        String message = null;
        Progress confirmProgress = null;
        if(progress.equals("accept")) {
            confirmProgress = Progress.ACCEPT;
            message = "티켓을 승인합니다";
        } else if(progress.equals("refuse")) {
            fan.setSizeOfTicket(fan.getSizeOfTicket() + 1);
            confirmProgress = Progress.REFUSE;
            message = "티켓을 거절합니다.";
        } else throw new Exception404("잘못된 요청입니다");

        if(schedulerUser == null) throw new Exception400(schedulerUser.toString(), "해당 티켓은 존재하지 않습니다");
        if(schedulerUser.getProgress().equals(Progress.ACCEPT)) throw new Exception412("이미 승인된 티켓입니다");
        if(schedulerUser.getProgress().equals(Progress.REFUSE)) throw new Exception412("이미 거절된 티켓입니다");

        schedulerAdminService.updateUserSchedule(schedulerUserId, confirmProgress);

        return ResponseEntity.ok(message);
    }

    /**
     * 기획사 id를 받아 관련 티케팅 데이터를 엑셀 파일로 다운로드합니다.
     * @param id
     * @param myUserDetails
     * @throws Exception
     */
    @GetMapping("/schedule/{id}/excelDownload")
    public ResponseEntity<String> excelDownload(
            @PathVariable Long id,
            @AuthenticationPrincipal MyUserDetails myUserDetails,
            @RequestHeader(JwtTokenProvider.HEADER) String token
    ) throws Exception {

        // 유효성 검사
        Long loginUserId = jwtTokenProvider.getUserIdFromToken(token);
        if(!myUserDetails.getUser().getId().equals(loginUserId)) throw new Exception401("인증되지 않았습니다");
        if(!myUserDetails.getUser().getId().equals(id)) throw new Exception403("권한이 없습니다");

        schedulerAdminService.excelDownload(id);

        return ResponseEntity.ok("다운로드 완료");
    }
}