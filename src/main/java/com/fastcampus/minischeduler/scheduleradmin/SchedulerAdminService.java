package com.fastcampus.minischeduler.scheduleradmin;

import com.fastcampus.minischeduler.core.auth.jwt.JwtTokenProvider;
import com.fastcampus.minischeduler.scheduleruser.Progress;
import com.fastcampus.minischeduler.scheduleruser.SchedulerUser;
import com.fastcampus.minischeduler.scheduleruser.SchedulerUserRepository;
import com.fastcampus.minischeduler.user.User;
import com.fastcampus.minischeduler.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;

import static com.fastcampus.minischeduler.scheduleradmin.SchedulerAdminRequest.SchedulerAdminRequestDto;
import static com.fastcampus.minischeduler.scheduleradmin.SchedulerAdminResponse.SchedulerAdminResponseDto;

@Service
@RequiredArgsConstructor
public class SchedulerAdminService {

    @Value("${file.dir}")
    private String fileDir;
    private final SchedulerAdminRepository schedulerAdminRepository;
    private final SchedulerUserRepository schedulerUserRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 전체 일정 목록을 출력합니다.
     * @return schedulerAdminResponseDtoList
     */
    @Transactional
    public List<SchedulerAdminResponseDto> getSchedulerList(){
        List<SchedulerAdmin> schedulers = schedulerAdminRepository.findAll();
        List<SchedulerAdminResponseDto> schedulerAdminResponseDtoList = new ArrayList<>();

        for(SchedulerAdmin scheduler : schedulers) {
            SchedulerAdminResponseDto schedulerAdminResponseDto =
                    SchedulerAdminResponseDto.builder()
                            .user(scheduler.getUser())
                            .scheduleStart(scheduler.getScheduleStart())
                            .scheduleEnd(scheduler.getScheduleEnd())
                            .title(scheduler.getTitle())
                            .description(scheduler.getDescription())
                            .image(scheduler.getImage())
                            .createdAt(scheduler.getCreatedAt())
                            .updatedAt(scheduler.getUpdatedAt())
                            .build();
            schedulerAdminResponseDtoList.add(schedulerAdminResponseDto);
        }
        return schedulerAdminResponseDtoList;
    }

    /**
     * year와 month로 해당하는 일정의 스케줄만 반환합니다
     * @param year, month
     * @return SchedulerAdminResponseDto
     */
    public List<SchedulerAdminResponseDto> getSchedulerListByYearAndMonth(
            Integer year,
            Integer month
    ) {

        YearMonth yearMonth = YearMonth.of(year, month);

        List<SchedulerAdmin> schedulers = schedulerAdminRepository.findAll();
        List<SchedulerAdminResponseDto> schedulerAdminResponseDtoList = new ArrayList<>();

        for(SchedulerAdmin scheduler : schedulers) {
            LocalDateTime scheduleStart = scheduler.getScheduleStart();
            YearMonth scheduleYearMonth = YearMonth.of(scheduleStart.getYear(), scheduleStart.getMonth());
            if(yearMonth.equals(scheduleYearMonth)) {
                SchedulerAdminResponseDto schedulerAdminResponseDto =
                        SchedulerAdminResponseDto.builder()
                                .user(scheduler.getUser())
                                .scheduleStart(scheduler.getScheduleStart())
                                .scheduleEnd(scheduler.getScheduleEnd())
                                .title(scheduler.getTitle())
                                .description(scheduler.getDescription())
                                .image(scheduler.getImage())
                                .createdAt(scheduler.getCreatedAt())
                                .updatedAt(scheduler.getUpdatedAt())
                                .build();
                schedulerAdminResponseDtoList.add(schedulerAdminResponseDto);
            }
        }
        return schedulerAdminResponseDtoList;
    }

    /**
     * 일정을 등록합니다.
     * @param schedulerAdminRequestDto, token
     * @return SchedulerAdminResponseDto
     */
    @Transactional
    public SchedulerAdminResponseDto createScheduler(
            SchedulerAdminRequestDto schedulerAdminRequestDto,
            String token,
            MultipartFile file
    ) throws IOException {
        Long loginUserId = jwtTokenProvider.getUserIdFromToken(token);
        User user = userRepository.findById(loginUserId)
                .orElseThrow(()->new IllegalArgumentException("사용자 정보를 찾을 수 없습니다"));

        String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        String filePath = fileDir + filename;
        file.transferTo(new File(filePath));

        SchedulerAdmin scheduler = SchedulerAdmin.builder()
                .user(user)
                .scheduleStart(schedulerAdminRequestDto.getScheduleStart())
                .scheduleEnd(schedulerAdminRequestDto.getScheduleEnd())
                .title(schedulerAdminRequestDto.getTitle())
                .description(schedulerAdminRequestDto.getDescription())
                .image(filename)
                .build();
        SchedulerAdmin saveScheduler = schedulerAdminRepository.save(scheduler);

        return SchedulerAdminResponseDto.builder()
                .user(saveScheduler.getUser())
                .scheduleStart(saveScheduler.getScheduleStart())
                .scheduleEnd(saveScheduler.getScheduleEnd())
                .title(saveScheduler.getTitle())
                .description(saveScheduler.getDescription())
                .image(saveScheduler.getImage())
                .createdAt(saveScheduler.getCreatedAt())
                .updatedAt(saveScheduler.getUpdatedAt())
                .build();
    }

    /**
     * 일정을 수정합니다.
     * @param id, schedulerAdminRequestDto, file
     * @return id
     */
    @Transactional
    public Long updateScheduler(
            Long id,
            SchedulerAdminRequestDto schedulerAdminRequestDto,
            MultipartFile file
    ) throws IOException {
        SchedulerAdmin scheduler = schedulerAdminRepository.findById(id).orElseThrow(
                ()-> new IllegalStateException("스케쥴러를 찾을 수 없습니다")
        );
        if(file != null && !file.isEmpty()){
            String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            String filePath = fileDir + filename;
            file.transferTo(new File(filePath));
            scheduler.update(
                    schedulerAdminRequestDto.getScheduleStart(),
                    schedulerAdminRequestDto.getScheduleEnd(),
                    schedulerAdminRequestDto.getTitle(),
                    schedulerAdminRequestDto.getDescription(),
                    filename
            );
        }
        else {
            scheduler.update(
                    schedulerAdminRequestDto.getScheduleStart(),
                    schedulerAdminRequestDto.getScheduleEnd(),
                    schedulerAdminRequestDto.getTitle(),
                    schedulerAdminRequestDto.getDescription(),
                    scheduler.getImage()
            );
        }
        return id;
    }

    /**
     * 일정을 삭제합니다.
     * @param id, token
     * @return id
     */
    @Transactional
    public Long delete(Long id, String token){

        SchedulerAdminResponseDto schedulerAdminResponseDto = getSchedulerById(id);
       Long loginUserId = jwtTokenProvider.getUserIdFromToken(token);

       if(!schedulerAdminResponseDto.getUser().getId().equals(loginUserId)){
           throw new IllegalStateException("스케줄을 삭제할 권한이 없습니다.");
       }
       SchedulerAdmin schedulerAdmin = schedulerAdminRepository.findById(id)
               .orElseThrow(() -> new IllegalArgumentException("스케줄을 찾을 수 없습니다"));

       List<SchedulerUser> schedulerUsers = schedulerUserRepository.findBySchedulerAdmin(schedulerAdmin);
       if(!schedulerUsers.isEmpty()){
           for(SchedulerUser schedulerUser : schedulerUsers){
               User user = schedulerUser.getUser();
               int ticket = user.getSizeOfTicket();
               user.setSizeOfTicket(ticket+1);
               userRepository.save(user);
           }
       }

       //글 삭제시 local에 저장된 image파일도 같이 삭제
       String image = schedulerAdmin.getImage();
       if(image != null && !image.isEmpty()){
           String filePath = fileDir + image;
           File imgeFile = new File(filePath);
           if(imgeFile.exists()){
               imgeFile.delete();
           }
       }

       schedulerAdminRepository.deleteById(id);
       return id;
    }

    /**
     * 사용자 별 일정을 출력합니다.
     * year와 month가 null이 아니라면 해당하는 년도와 달로 출력합니다
     * @param keyword, year, month
     * @return List<SchedulerAdminResponseDto>
     */
    @Transactional
    public List<SchedulerAdminResponseDto> getSchedulerByFullName(
            String keyword,
            Integer year,
            Integer month
    ){
        YearMonth yearMonth = null;
        if(year != null && month != null) yearMonth = YearMonth.of(year, month);

        List<SchedulerAdmin> schedulers = schedulerAdminRepository.findByUserFullNameContaining(keyword);
        List<SchedulerAdminResponseDto> schedulerAdminResponseDtoList = new ArrayList<>();

        for(SchedulerAdmin scheduler : schedulers){
            if(yearMonth != null){
                LocalDateTime scheduleStart = scheduler.getScheduleStart();
                YearMonth scheduleYearMonth = YearMonth.of(scheduleStart.getYear(), scheduleStart.getMonth());
                if(yearMonth.equals(scheduleYearMonth)) {
                    SchedulerAdminResponseDto schedulerAdminResponseDto =
                            SchedulerAdminResponseDto.builder()
                                    .user(scheduler.getUser())
                                    .scheduleStart(scheduler.getScheduleStart())
                                    .scheduleEnd(scheduler.getScheduleEnd())
                                    .title(scheduler.getTitle())
                                    .description(scheduler.getDescription())
                                    .image(scheduler.getImage())
                                    .createdAt(scheduler.getCreatedAt())
                                    .updatedAt(scheduler.getUpdatedAt())
                                    .build();
                    schedulerAdminResponseDtoList.add(schedulerAdminResponseDto);
                }
            } else {
                SchedulerAdminResponseDto schedulerAdminResponseDto =
                        SchedulerAdminResponseDto.builder()
                                .user(scheduler.getUser())
                                .scheduleStart(scheduler.getScheduleStart())
                                .scheduleEnd(scheduler.getScheduleEnd())
                                .title(scheduler.getTitle())
                                .description(scheduler.getDescription())
                                .image(scheduler.getImage())
                                .createdAt(scheduler.getCreatedAt())
                                .updatedAt(scheduler.getUpdatedAt())
                                .build();
                schedulerAdminResponseDtoList.add(schedulerAdminResponseDto);
            }
        }
        return schedulerAdminResponseDtoList;
    }

    /**
     * 사용자 id로 일정을 검색합니다.
     * @param id
     * @return SchedulerAdminResponseDto
     */
    @Transactional
    public SchedulerAdminResponseDto getSchedulerById(Long id){

        SchedulerAdmin scheduler = schedulerAdminRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다.")
        );

        return SchedulerAdminResponseDto.builder()
                .user(scheduler.getUser())
                .scheduleStart(scheduler.getScheduleStart())
                .scheduleEnd(scheduler.getScheduleEnd())
                .title(scheduler.getTitle())
                .description(scheduler.getDescription())
                .image(scheduler.getImage())
                .createdAt(scheduler.getCreatedAt())
                .updatedAt(scheduler.getUpdatedAt())
                .build();
    }

    /**
     * 사용자 id로 일정을 검색합니다.
     * @param id
     * @return SchedulerAdmin
     */
    public SchedulerAdmin getSchedulerAdminById(Long id) {
        return schedulerAdminRepository.findById(id).orElse(null);
    }

    /**
     * token으로 사용자를 찾아 사용자가 작성한 모든 schedule을 반환합니다.
     * year와 month가 null이 아니면 각 년도와 달에 부합한 스케줄도 같이 전달합니다.
     * @param token, year, month
     * @return Map<String, Object>
     */
    public Map<String, Object> getSchedulerListById(String token, Integer year, Integer month) {
        Map<String, Object> response = new HashMap<>();
        Long loginUserId = jwtTokenProvider.getUserIdFromToken(token);
        User user = userRepository.findById(loginUserId)
                .orElseThrow(()->new IllegalArgumentException("사용자 정보를 찾을 수 없습니다"));
        List<SchedulerAdmin> schedulerAdmins = schedulerAdminRepository.findByUser(user);
        List<SchedulerAdminResponseDto> schedulerAdminResponseDtoList = new ArrayList<>();
        List<SchedulerAdminResponseDto> schedulerAdminResponseDtoListByYearAndMonth = new ArrayList<>();
        if(year != null && month != null){
            YearMonth yearMonth = YearMonth.of(year, month);
            for(SchedulerAdmin schedulerAdmin : schedulerAdmins){
                LocalDateTime scheduleStart = schedulerAdmin.getScheduleStart();
                YearMonth scheduleYearMonth = YearMonth.of(scheduleStart.getYear(), scheduleStart.getMonth());
                if(yearMonth.equals(scheduleYearMonth)){
                    SchedulerAdminResponseDto schedulerAdminResponseDto =
                            SchedulerAdminResponseDto.builder()
                                    .user(schedulerAdmin.getUser())
                                    .scheduleStart(schedulerAdmin.getScheduleStart())
                                    .scheduleEnd(schedulerAdmin.getScheduleEnd())
                                    .title(schedulerAdmin.getTitle())
                                    .description(schedulerAdmin.getDescription())
                                    .image(schedulerAdmin.getImage())
                                    .createdAt(schedulerAdmin.getCreatedAt())
                                    .updatedAt(schedulerAdmin.getUpdatedAt())
                                    .build();
                    schedulerAdminResponseDtoListByYearAndMonth.add(schedulerAdminResponseDto);
                }
            }
            response.put("schedulerAdminListByYearAndMonth", schedulerAdminResponseDtoListByYearAndMonth);
        }

        for(SchedulerAdmin schedulerAdmin : schedulerAdmins){
            SchedulerAdminResponseDto schedulerAdminResponseDto =
                    SchedulerAdminResponseDto.builder()
                            .user(schedulerAdmin.getUser())
                            .scheduleStart(schedulerAdmin.getScheduleStart())
                            .scheduleEnd(schedulerAdmin.getScheduleEnd())
                            .title(schedulerAdmin.getTitle())
                            .description(schedulerAdmin.getDescription())
                            .image(schedulerAdmin.getImage())
                            .createdAt(schedulerAdmin.getCreatedAt())
                            .updatedAt(schedulerAdmin.getUpdatedAt())
                            .build();
            schedulerAdminResponseDtoList.add(schedulerAdminResponseDto);
        }
        response.put("schedulerAdminList", schedulerAdminResponseDtoList);
        return response;
    }

    /**
     * 결재관리 페이지의 해당 기획사 공연에 티케팅한 사용자의 내역과
     * 승인현황 별 티케팅 수를 조회합니다.
     * @param id
     * @return
     */
    @Transactional(readOnly = true)
    public SchedulerAdminResponse getAdminScheduleDetail(Long id) {

        SchedulerAdminResponse schedulerAdminResponse = new SchedulerAdminResponse(
                schedulerAdminRepository.findSchedulesWithUsersById(id),
                schedulerAdminRepository.countScheduleGroupByProgressById(id));

        return schedulerAdminResponse;
    }

    @Transactional
    public void updateUserSchedule(Long schedulerAdminId, Progress progress) {

        schedulerAdminRepository.updateUserScheduleById(schedulerAdminId, progress);
    }
}
