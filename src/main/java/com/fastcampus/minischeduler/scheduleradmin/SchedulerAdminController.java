package com.fastcampus.minischeduler.scheduleradmin;

import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
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
import com.fastcampus.minischeduler.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class SchedulerAdminController {

    private final SchedulerUserRepository schedulerUserRepository;
    private final SchedulerAdminRepository schedulerAdminRepository;
    private final SchedulerAdminService schedulerAdminService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Value("${file.dir}")
    private String fileDir;

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
    ){
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

    /**
     * 공연 등록/취소 페이지 : 로그인한 기획사가 등록한 일정만 나옴
     *  year과 month가 null일땐 모든 스케줄이 나옴
     */
    @GetMapping("/schedule")
    public ResponseEntity<Map<String, Object>> getSchedulerList(
            @RequestHeader(JwtTokenProvider.HEADER) String token,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month
    ){
        return ResponseEntity.ok(schedulerAdminService.getSchedulerListById(token, year, month));
    }

    /**
     * 공연 상세보기 : 공연의 정보를 상세하게 봄
     */
    @GetMapping("/schedule/{id}")
    public SchedulerAdmin scheduleDetail(@PathVariable Long id){
        return schedulerAdminService.getSchedulerAdminById(id);
    }

    /**
     * 공연 등록 : 기획사가 공연을 등록함
     */
    @PostMapping("/schedule/create")
    public ResponseEntity<SchedulerAdminResponseDto> createScheduler(
            @RequestHeader(JwtTokenProvider.HEADER) String token,
            @RequestParam(required = false) MultipartFile file,
            @RequestParam("scheduleStart") String scheduleStart,
            @RequestParam("scheduleEnd") String scheduleEnd,
            @RequestParam("title") String title,
            @RequestParam("description") String description
    ) throws IOException {
        SchedulerAdminRequestDto schedulerAdminRequestDto = new SchedulerAdminRequestDto();
        schedulerAdminRequestDto.setScheduleStart(LocalDateTime.parse(scheduleStart, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
        schedulerAdminRequestDto.setScheduleEnd(LocalDateTime.parse(scheduleEnd, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
        schedulerAdminRequestDto.setTitle(title);
        schedulerAdminRequestDto.setDescription(description);
        return ResponseEntity.ok(schedulerAdminService.createScheduler(schedulerAdminRequestDto, token, file));
    }

    /**
     * 이미지 연결
     */
    @GetMapping(value = "/schedule/image/{fileName}")
    @ResponseBody
    public byte[] getImage(@PathVariable String fileName){
        try (InputStream inputStream = new FileInputStream(fileDir + fileName)) {
            return inputStream.readAllBytes();

        } catch (FileNotFoundException e) {
            throw new Exception401("파일을 찾을수 없습니다");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
            @RequestHeader(JwtTokenProvider.HEADER) String token,
            @RequestParam(required = false) MultipartFile file,
            @RequestParam("scheduleStart") String scheduleStart,
            @RequestParam("scheduleEnd") String scheduleEnd,
            @RequestParam("title") String title,
            @RequestParam("description") String description
    ) throws IOException {
        //스케줄 조회
        SchedulerAdminResponseDto schedulerDto = schedulerAdminService.getSchedulerById(id);
        //로그인한 사용자 id조회
        Long loginUserId = jwtTokenProvider.getUserIdFromToken(token);

        // 스케줄 작성자 id와 로그인한 사용자 id비교
        if(!schedulerDto.getUser().getId().equals(loginUserId)){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); //권한없음
        }

        SchedulerAdminRequestDto schedulerAdminRequestDto = new SchedulerAdminRequestDto();
        schedulerAdminRequestDto.setScheduleStart(LocalDateTime.parse(scheduleStart, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
        schedulerAdminRequestDto.setScheduleEnd(LocalDateTime.parse(scheduleEnd, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
        schedulerAdminRequestDto.setTitle(title);
        schedulerAdminRequestDto.setDescription(description);

        Long updateId = schedulerAdminService.updateScheduler(id, schedulerAdminRequestDto, file);
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
    ){
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
            @AuthenticationPrincipal MyUserDetails myUserDetails
    ) {

        if(id.longValue() != myUserDetails.getUser().getId()) throw new Exception403("권한이 없습니다");

        ResponseDTO<?> responseDTO = new ResponseDTO<>(schedulerAdminService.getAdminScheduleDetail(id));

        return ResponseEntity.ok(responseDTO);
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
            @AuthenticationPrincipal MyUserDetails myUserDetails
    ) {
        // 유효성 검사
        if(id.longValue() != myUserDetails.getUser().getId()) throw new Exception403("권한이 없습니다");

        SchedulerUser schedulerUser = schedulerUserRepository.findById(schedulerUserId).get();
        User fan = schedulerUser.getUser();
        if(fan == null) throw new Exception400(fan.getEmail(), "해당 사용자는 존재하지 않습니다");

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

        return ResponseEntity.ok(new ResponseDTO<>(message));
    }
}