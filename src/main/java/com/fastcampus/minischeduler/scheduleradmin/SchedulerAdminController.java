package com.fastcampus.minischeduler.scheduleradmin;

import com.fastcampus.minischeduler.core.auth.jwt.JwtTokenProvider;
import com.fastcampus.minischeduler.core.auth.session.MyUserDetails;
import com.fastcampus.minischeduler.core.dto.ResponseDTO;
import com.fastcampus.minischeduler.core.exception.*;
import com.fastcampus.minischeduler.scheduleradmin.SchedulerAdminRequest.SchedulerAdminRequestDto;
import com.fastcampus.minischeduler.scheduleradmin.SchedulerAdminResponse.SchedulerAdminResponseDto;
import com.fastcampus.minischeduler.scheduleruser.Progress;
import com.fastcampus.minischeduler.scheduleruser.SchedulerUser;
import com.fastcampus.minischeduler.scheduleruser.SchedulerUserRepository;
import com.fastcampus.minischeduler.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class SchedulerAdminController {

    private final SchedulerUserRepository schedulerUserRepository;
    private final SchedulerAdminService schedulerAdminService;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 기획사 일정 조회(메인) : 모든 기획사의 일정이 나옴
     * scheduleStart 날짜 기준으로 param으로 받은 년도와 달에 부합하는 모든 스케줄이 나옴
     * year과 month가 null일땐 모든 스케줄이 나옴
     */
    @GetMapping("/scheduleAll")
    public ResponseEntity<List<SchedulerAdminResponseDto>> schedulerList (
            @RequestHeader(JwtTokenProvider.HEADER) String token,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month
    ) {

        List<SchedulerAdminResponseDto> schedulerAdminResponseDtoList;

        //year와 month 유효성검증
        if (year != null && (year < 2000 || year > 3000))
            throw new Exception400("year", "유효하지 않은 년도입니다.");
        if (month != null && (month < 1 || month > 12))
            throw new Exception400("month", "유효하지 않은 달입니다.");

        try {
            if (year != null && month != null)
                schedulerAdminResponseDtoList = schedulerAdminService.getSchedulerListByYearAndMonth(year, month);
            else
                schedulerAdminResponseDtoList = schedulerAdminService.getSchedulerList();

            return ResponseEntity.ok(schedulerAdminResponseDtoList);
        } catch (Exception e) {
            throw new Exception500("디코딩에 실패하였습니다");
        }
    }

    /**
     * 공연 등록/취소 페이지 : 로그인한 기획사가 등록한 일정만 나옴
     *  year과 month가 null일땐 모든 스케줄이 나옴
     */
    @GetMapping("/schedule")
    public ResponseEntity<Map<String, Object>> getSchedulerList(
            @RequestHeader(JwtTokenProvider.HEADER) String token,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month
    ) {
        //year와 month 유효성검증
        if (year != null && (year < 2000 || year > 3000))
            throw new Exception400("year", "유효하지 않은 년도입니다.");
        if (month != null && (month < 1 || month > 12))
            throw new Exception400("month", "유효하지 않은 달입니다.");
        Long loginUserId = jwtTokenProvider.getUserIdFromToken(token);

        try {
            return ResponseEntity.ok(schedulerAdminService.getSchedulerListById(loginUserId, year, month));
        } catch (Exception e) {
            throw new Exception500("디코딩에 실패하였습니다");
        }
    }

    /**
     * 공연 등록 : 기획사가 공연을 등록함
     */
    @PostMapping("/schedule/create")
    public ResponseEntity<SchedulerAdminResponseDto> createScheduler(
            @RequestHeader(JwtTokenProvider.HEADER) String token,
            @RequestPart(value = "file", required = false) MultipartFile image,
            @RequestPart(value = "dto") SchedulerAdminRequestDto schedulerAdminRequestDto
    ) {
        if (schedulerAdminRequestDto.getScheduleStart() == null || schedulerAdminRequestDto.getScheduleEnd() == null)
            throw new Exception400("scheduleStart/scheduleEnd", "날짜정보가 비어있습니다");
        if (image != null && image.getSize() > 10000000)
            throw new Exception413(String.valueOf(image.getSize()), "파일이 너무 큽니다");
        if(schedulerAdminRequestDto.getTitle() == null) throw new Exception400("title", "제목이 비어있습니다");

        Long loginUserId = jwtTokenProvider.getUserIdFromToken(token);

        try {
            return ResponseEntity.ok(
                    schedulerAdminService.createScheduler(
                            schedulerAdminRequestDto,
                            loginUserId,
                            image
                    )
            );
        } catch (Exception e) {
            throw new Exception500("디코딩에 실패하였습니다");
        }
    }

    /**
     * 공연 일정 삭제 : 공연을 삭제함
     */
    @PostMapping("/schedule/delete/{adminScheduleId}")
    public ResponseEntity<String> deleteScheduler(
            @PathVariable Long adminScheduleId,
            @RequestHeader(JwtTokenProvider.HEADER) String token
    ) {

        if(adminScheduleId == null || adminScheduleId <= 0)
            throw new Exception400(adminScheduleId.toString(), "유효하지 않은 id값입니다");

        try {
            SchedulerAdminResponseDto schedulerAdminResponseDto =
                    schedulerAdminService.getSchedulerById(adminScheduleId);

            Long loginUserId = jwtTokenProvider.getUserIdFromToken(token);
            if (!schedulerAdminResponseDto.getUser().getId().equals(loginUserId))
                throw new Exception403("권한이 없습니다");

            schedulerAdminService.delete(adminScheduleId);

            return ResponseEntity.ok("스케줄 삭제 완료");
        } catch (Exception e) {
            throw new Exception500("디코딩에 실패하였습니다");
        }
    }

    /**
     * 공연 일정 수정 : 공연 일정을 업데이트함
     */
    @PostMapping("/schedule/update/{id}")
    public ResponseEntity<SchedulerAdminResponseDto> updateScheduler(
            @PathVariable Long id,
            @RequestHeader(JwtTokenProvider.HEADER) String token,
            @RequestPart(value = "file", required = false) MultipartFile image,
            @RequestPart(value = "dto") SchedulerAdminRequestDto schedulerAdminRequestDto
    ) {
        try {
            //스케줄 조회
            SchedulerAdminResponseDto schedulerDto = schedulerAdminService.getSchedulerById(id);
            //로그인한 사용자 id조회
            Long loginUserId = jwtTokenProvider.getUserIdFromToken(token);

            if (image != null && image.getSize() > 10000000)
                throw new Exception413(String.valueOf(image.getSize()), "파일이 너무 큽니다");

            // 스케줄 작성자 id와 로그인한 사용자 id비교
            if(!schedulerDto.getUser().getId().equals(loginUserId)) throw new Exception403("권한이 없습니다"); //권한없음
            if (schedulerAdminRequestDto.getScheduleStart() == null || schedulerAdminRequestDto.getScheduleEnd() == null)
                throw new Exception400("scheduleStart/scheduleEnd", "날짜정보가 비어있습니다");
            if(schedulerAdminRequestDto.getTitle() == null) throw new Exception400("title", "제목이 비어있습니다");

            return ResponseEntity
                    .ok(schedulerAdminService.getSchedulerById(
                            schedulerAdminService.updateScheduler(id, schedulerAdminRequestDto, image)));
        } catch (Exception e) {
            throw new Exception500("디코딩에 실패하였습니다");
        }
    }

    /**
     *  공연 기획사별 검색 : 공연 기획사별로 검색가능
     *  scheduleStart 날짜 기준으로 param으로 받은 년도와 달에 부합하는 모든 스케줄이 나옴
     *  year과 month가 null일땐 모든 스케줄이 나옴
     */
    @GetMapping("/schedule/search")
    public ResponseEntity<List<SchedulerAdminResponseDto>> searchScheduler(
            @RequestHeader(JwtTokenProvider.HEADER) String token,
            @RequestParam String keyword,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month
    ) {
        // TODO - 기획사 이름으로 사용자 검색 유효성 검사

        //year와 month 유효성검증
        if (year != null && (year < 2000 || year > 3000)) throw new Exception400("year", "유효하지 않은 년도입니다");
        if (month != null && (month < 1 || month > 12)) throw new Exception400("month", "유효하지 않은 달입니다");

        try {
            return ResponseEntity.ok(schedulerAdminService.getSchedulerByFullName(keyword, year, month));
        } catch (Exception e) {
            throw new Exception500("디코딩에 실패하였습니다");
        }
    }

    /**
     * 티켓 결재 페이지
     * @param token : 토큰
     * @return : userDto, scheduleDto, countProcessDto
     */
    @GetMapping("/schedule/confirm")
    public ResponseEntity<?> getAdminSchedulerAndUserScheduler(
            @RequestHeader(JwtTokenProvider.HEADER) String token
    ) {
        try {
            return ResponseEntity.ok(new ResponseDTO<>(schedulerAdminService.getAdminScheduleDetail(token)));
        } catch (Exception e) {
            throw new Exception500("디코딩에 실패하였습니다");
        }
    }

    /**
     * 선택한 티켓을 승인하거나 거절합니다.
     * @param userSchedulerId : 선택한 사용자 일정 id
     * @param progress        : 선택된 티켓 승인 옵션
     * @param token           : 헤더의 토큰을 가져옴
     * @return                : 메세지 응답
     */
    @PostMapping("/schedule/confirm/{userSchedulerId}")
    public ResponseEntity<?> confirmSchedule(
            @PathVariable Long userSchedulerId,
            @RequestParam(required = false) String progress,
            @RequestHeader(JwtTokenProvider.HEADER) String token
    ) {
        // 유효성 검사
        Optional<SchedulerUser> object = schedulerUserRepository.findById(userSchedulerId);
        if (object.isEmpty()) throw new Exception400(userSchedulerId.toString(), "해당 티켓이 존재하지 않습니다");

        if (progress == null || progress.isBlank()) throw new Exception400("'승인' 또는 '거절'을 선택해주세요");

        SchedulerUser schedulerUser = object.get();
        if(schedulerUser.getProgress().equals(Progress.ACCEPT)) throw new Exception412("이미 승인된 티켓입니다");
        if(schedulerUser.getProgress().equals(Progress.REFUSE)) throw new Exception412("이미 거절된 티켓입니다");
        User fan = schedulerUser.getUser();

        String message = null;
        Progress confirmProgress = null;
        if (progress.equals("ACCEPT")) {
            confirmProgress = Progress.ACCEPT;
            message = "티켓을 승인합니다";
        } else if(progress.equals("REFUSE")) {
            fan.setSizeOfTicket(fan.getSizeOfTicket() + 1);
            confirmProgress = Progress.REFUSE;
            message = "티켓을 거절합니다.";
        } else throw new Exception404("잘못된 요청입니다");

        try {
            return ResponseEntity.ok(
                    new ResponseDTO<>(
                            schedulerAdminService.updateUserSchedule(userSchedulerId, confirmProgress, token),
                            message
                    )
            );
        } catch (Exception e) {
            throw new Exception500("디코딩에 실패하였습니다");
        }
    }

    /**
     * 기획사 id를 받아 관련 티케팅 데이터를 엑셀 파일로 다운로드합니다.
     * @param id            : 현재 로그인한 기획사 id
     * @param myUserDetails : 현재 로그인한 사용자 정보
     */
    @GetMapping("/schedule/{id}/excelDownload")
    public ResponseEntity<String> excelDownload(
            @PathVariable Long id,
            @AuthenticationPrincipal MyUserDetails myUserDetails,
            @RequestHeader(JwtTokenProvider.HEADER) String token
    ) {

        // 유효성 검사
        Long loginUserId = jwtTokenProvider.getUserIdFromToken(token);
        if (!myUserDetails.getUser().getId().equals(loginUserId)) throw new Exception401("인증되지 않았습니다");
        if (!myUserDetails.getUser().getId().equals(id)) throw new Exception403("권한이 없습니다");

        try {
            schedulerAdminService.excelDownload(id);

            return ResponseEntity.ok("다운로드 완료");
        } catch (Exception e) {
            throw new Exception500("디코딩에 실패하였습니다");
        }
    }
}