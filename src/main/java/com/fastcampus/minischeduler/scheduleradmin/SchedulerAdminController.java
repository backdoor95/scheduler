package com.fastcampus.minischeduler.scheduleradmin;

import com.fastcampus.minischeduler.core.auth.jwt.JwtTokenProvider;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.GeneralSecurityException;
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
     * 기획사 일정 조회페이지(메인) : 모든 기획사의 일정이 나옴
     * @param token : 사용자 인증 토큰
     * @param year : 년도
     * @param month : 달
     * @return  모든 기획사의 등록된 행사를 담은 schedulerAdminResponseDtoList
     * @throws Exception400 요청이 잘못된 경우 올바르지 않은 년도 또는 달
     * @throws Exception500 디코딩에 실패한 경우
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
            throw new Exception400("year", ErrorCode.INVALID_YEAR.getMessage());
        if (month != null && (month < 1 || month > 12))
            throw new Exception400("month", ErrorCode.INVALID_MONTH.getMessage());

        try {
            if (year != null && month != null)
                schedulerAdminResponseDtoList = schedulerAdminService.getSchedulerListByYearAndMonth(year, month);
            else
                schedulerAdminResponseDtoList = schedulerAdminService.getSchedulerList();

            return ResponseEntity.ok(schedulerAdminResponseDtoList);
        } catch (GeneralSecurityException gse) {
            throw new Exception500(ErrorCode.FAIL_DECODING.getMessage());
        }
    }

    /**
     * 공연 등록/취소 페이지 : 로그인한 기획사가 등록한 일정만 나옴
     * @param token : 사용자 인증 토큰
     * @param year : 년도
     * @param month : 달
     * @return 기획사가 등록한 일정을 년도와 달에 맞는 행사 + 모든 행사를 포함한 Map 객체
     * @throws Exception400 요청이 잘못된 경우 올바르지 않은 년도 또는 달
     * @throws Exception500 디코딩에 실패한 경우
     */
    @GetMapping("/schedule")
    public ResponseEntity<Map<String, Object>> getSchedulerList(
            @RequestHeader(JwtTokenProvider.HEADER) String token,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month
    ) {
        //year와 month 유효성검증
        if (year != null && (year < 2000 || year > 3000))
            throw new Exception400("year", ErrorCode.INVALID_YEAR.getMessage());
        if (month != null && (month < 1 || month > 12))
            throw new Exception400("month", ErrorCode.INVALID_MONTH.getMessage());
        try {
            return ResponseEntity.ok(schedulerAdminService.getSchedulerListById(token, year, month));
        } catch (GeneralSecurityException gse) {
            throw new Exception500(ErrorCode.FAIL_DECODING.getMessage());
        }
    }

    /**
     * 공연 등록 페이지 : 기획사가 공연을 등록함
     * @param token : 사용자 인증 토큰
     * @param image : 이미지
     * @param schedulerAdminRequestDto : 공연 등록 내용
     * @return 기획사가 등록한 행사를 반환
     * @throws Exception400 날짜 정보가 비어있는 경우 / 제목이 비어있는 경우
     * @throws Exception413 image 파일의 크기가 너무 큰 경우
     * @throws Exception500 디코딩에 실패한 경우 / 이미지 파일전송에 실패한 경우
     */
    @PostMapping("/schedule/create")
    public ResponseEntity<SchedulerAdminResponseDto> createScheduler(
            @RequestHeader(JwtTokenProvider.HEADER) String token,
            @RequestPart(value = "file", required = false) MultipartFile image,
            @RequestPart(value = "dto") SchedulerAdminRequestDto schedulerAdminRequestDto
    ) {
        if (schedulerAdminRequestDto.getScheduleStart() == null || schedulerAdminRequestDto.getScheduleEnd() == null)
            throw new Exception400("scheduleStart/scheduleEnd", ErrorCode.EMPTY_DATE.getMessage());
        if (image != null && image.isEmpty() && image.getSize() > 10000000)
            throw new Exception413(String.valueOf(image.getSize()), ErrorCode.FILE_CAPACITY_EXCEEDED.getMessage());
        if(schedulerAdminRequestDto.getTitle() == null) throw new Exception400("title", ErrorCode.EMPTY_TITLE.getMessage());

        try {
            return ResponseEntity.ok(schedulerAdminService.createScheduler(schedulerAdminRequestDto, token, image));
        } catch (GeneralSecurityException gse) {
            throw new Exception500(ErrorCode.FAIL_DECODING.getMessage());
        } catch (IOException ioe) {
            throw new Exception500(ErrorCode.FAIL_IMAGE_UPLOAD.getMessage());
        }
    }

    /**
     * 공연 일정 삭제 : 기획사가 등록한 공연을 삭제함
     * @param token : 사용자 인증 토큰
     * @param adminScheduleId : 삭제하려는 공연의 id
     * @return "스케줄 삭제 완료"
     * @throws Exception400 유효하지 않은 id값일 경우
     * @throws Exception403 본인이 등록한 행사가 아닌데 삭제하려고 시도할 경우
     * @throws Exception500 디코딩에 실패한 경우
     */
    @PostMapping("/schedule/delete/{adminScheduleId}")
    public ResponseEntity<String> deleteScheduler(
            @PathVariable Long adminScheduleId,
            @RequestHeader(JwtTokenProvider.HEADER) String token
    ) {

        if(adminScheduleId == null || adminScheduleId <= 0)
            throw new Exception400("adminScheduleId", ErrorCode.INVALID_ID.getMessage());

        try {
            SchedulerAdminResponseDto schedulerAdminResponseDto =
                    schedulerAdminService.getSchedulerById(adminScheduleId);

            Long loginUserId = jwtTokenProvider.getUserIdFromToken(token);
            if (!schedulerAdminResponseDto.getUser().getId().equals(loginUserId))
                throw new Exception403(ErrorCode.INVALID_USER.getMessage());

            schedulerAdminService.delete(adminScheduleId);

            return ResponseEntity.ok("스케줄 삭제 완료");
        } catch (GeneralSecurityException gse) {
            throw new Exception500(ErrorCode.FAIL_DECODING.getMessage());
        }
    }

    /**
     * 공연 일정 수정 : 기획사가 공연 일정을 업데이트함
     * @param token : 사용자 인증 토큰
     * @param id : 업데이트 하려는 공연의 id값
     * @param image : 이미지
     * @param schedulerAdminRequestDto : 공연 업데이트 내용
     * @return 기획사가 업데이트한 행사내용을 반환
     * @throws Exception400 날짜 정보가 비어있는 경우 / 제목이 비어있는 경우
     * @throws Exception403 본인이 등록한 행사가 아닌데 업데이트하려고 시도할 경우
     * @throws Exception413 image 파일의 크기가 너무 큰 경우
     * @throws Exception500 디코딩에 실패한 경우 / 이미지 파일전송에 실패한 경우
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
                throw new Exception413(String.valueOf(image.getSize()), ErrorCode.FILE_CAPACITY_EXCEEDED.getMessage());

            // 스케줄 작성자 id와 로그인한 사용자 id비교
            if(!schedulerDto.getUser().getId().equals(loginUserId)) throw new Exception403(ErrorCode.INVALID_USER.getMessage()); //권한없음
            if (schedulerAdminRequestDto.getScheduleStart() == null || schedulerAdminRequestDto.getScheduleEnd() == null)
                throw new Exception400("scheduleStart/scheduleEnd", ErrorCode.EMPTY_DATE.getMessage());
            if(schedulerAdminRequestDto.getTitle() == null) throw new Exception400("title", ErrorCode.EMPTY_TITLE.getMessage());

            return ResponseEntity
                    .ok(schedulerAdminService.getSchedulerById(
                            schedulerAdminService.updateScheduler(id, schedulerAdminRequestDto, image)));
        } catch (GeneralSecurityException gse) {
            throw new Exception500(ErrorCode.FAIL_DECODING.getMessage());
        } catch (IOException ioe) {
            throw new Exception500(ErrorCode.FAIL_IMAGE_UPLOAD.getMessage());
        }
    }

    /**
     * 공연 기획사별 검색 : 기획사이름으로 검색가능
     * @param token : 사용자 인증 토큰
     * @param keyword : 검색 키워드(기획사 이름)
     * @param year : 년도
     * @param month : 달
     * @return 검색한 기획사가 등록한 공연정보 모두를 반환
     * @throws Exception400 요청이 잘못된 경우 올바르지 않은 년도 또는 달
     * @throws Exception500 디코딩에 실패한 경우
     */
    @GetMapping("/schedule/search")
    public ResponseEntity<List<SchedulerAdminResponseDto>> searchScheduler(
            @RequestHeader(JwtTokenProvider.HEADER) String token,
            @RequestParam String keyword,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month
    ) {
        //year와 month 유효성검증
        if (year != null && (year < 2000 || year > 3000)) throw new Exception400("year", ErrorCode.INVALID_YEAR.getMessage());
        if (month != null && (month < 1 || month > 12)) throw new Exception400("month", ErrorCode.INVALID_MONTH.getMessage());

        try {
            return ResponseEntity.ok(schedulerAdminService.getSchedulerByFullName(keyword, year, month));
        } catch (GeneralSecurityException gse) {
            throw new Exception500(ErrorCode.FAIL_DECODING.getMessage());
        }
    }

    /**
     * 티켓 결재 페이지 : 팬이 신청한 티켓을 승인하거나 반려함
     * @param token : 사용자 인증 토큰
     * @return : 기획사 정보와 관련 티켓승인현황, 기획사 일정을 반환
     * @throws Exception500 디코딩에 실패한 경우
     */
    @GetMapping("/schedule/confirm")
    public ResponseEntity<?> getAdminSchedulerAndUserScheduler(
            @RequestHeader(JwtTokenProvider.HEADER) String token
    ) {
        try {
            return ResponseEntity.ok(new ResponseDTO<>(schedulerAdminService.getAdminScheduleDetail(token)));
        } catch (GeneralSecurityException gse) {
            throw new Exception500(ErrorCode.FAIL_DECODING.getMessage());
        }
    }

    /**
     * 티켓 결재 : 선택한 티켓을 승인하거나 거절함.
     * @param userSchedulerId : 선택한 사용자 일정 id
     * @param progress : 선택된 티켓 승인 옵션
     * @param token : 사용자 인증 토큰
     * @return : 메세지 응답
     * @throws Exception400 티켓이 존재하지 않을 경우 / 상태를 선택하지 않을 경우
     * @throws Exception412 이미 승인됐거나 거절된 티켓일경우
     * @throws Exception404 승인 거절 이외의 요청일경우
     * @throws Exception500 디코딩에 실패한 경우
     */
    @PostMapping("/schedule/confirm/{userSchedulerId}")
    public ResponseEntity<?> confirmSchedule(
            @PathVariable Long userSchedulerId,
            @RequestParam(required = false) String progress,
            @RequestHeader(JwtTokenProvider.HEADER) String token
    ) {
        // 유효성 검사
        Optional<SchedulerUser> object = schedulerUserRepository.findById(userSchedulerId);
        if (object.isEmpty()) throw new Exception400(userSchedulerId.toString(), ErrorCode.TICKET_NOT_FOUND.getMessage());

        if (progress == null || progress.isBlank()) throw new Exception400(ErrorCode.EMPTY_PROGRESS.getMessage());

        SchedulerUser schedulerUser = object.get();
        if(schedulerUser.getProgress().equals(Progress.ACCEPT)) throw new Exception412(ErrorCode.ALREADY_ACCEPTED_TICKET.getMessage());
        if(schedulerUser.getProgress().equals(Progress.REFUSE)) throw new Exception412(ErrorCode.ALREADY_REFUSED_TICKET.getMessage());
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
        } else throw new Exception404(ErrorCode.INVALID_REQUEST.getMessage());

        try {
            return ResponseEntity.ok(
                    new ResponseDTO<>(
                            schedulerAdminService.updateUserSchedule(userSchedulerId, confirmProgress, token),
                            message
                    )
            );
        } catch (GeneralSecurityException gse) {
            throw new Exception500(ErrorCode.FAIL_DECODING.getMessage());
        }
    }

    /**
     * 엑셀 파일 다운로드 : 기획사 토큰을 받아 관련 티케팅 데이터를 엑셀 파일로 다운로드함.
     * @param token : 사용자 인증 토큰
     * @return : "다운로드 완료"
     * @throws Exception500 디코딩에 실패한 경우 / 이미지 파일 전송에 실패한 경우 / 잘못된 접근일 경우
     */
    @GetMapping("/schedule/excelDownload")
    public ResponseEntity<String> excelDownload(
            @RequestHeader(JwtTokenProvider.HEADER) String token
    ) {
        try {
            schedulerAdminService.excelDownload(token);

            return ResponseEntity.ok("다운로드 완료");
        } catch (GeneralSecurityException gse) {
            throw new Exception500(ErrorCode.FAIL_DECODING.getMessage());
        } catch (IOException ioe) {
            throw new Exception500(ErrorCode.FAIL_IMAGE_UPLOAD.getMessage());
        } catch (IllegalAccessException iae) {
            throw new Exception500(ErrorCode.INVALID_ACCESS.getMessage());
        }
    }
}