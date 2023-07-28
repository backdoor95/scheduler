package com.fastcampus.minischeduler.scheduleradmin;

import com.fastcampus.minischeduler.core.auth.jwt.JwtTokenProvider;
import com.fastcampus.minischeduler.scheduleruser.SchedulerUser;
import com.fastcampus.minischeduler.user.User;
import com.fastcampus.minischeduler.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SchedulerAdminService {

    private final SchedulerAdminRepository schedulerAdminRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 전체 일정 목록을 출력합니다.
     * @return
     */
    @Transactional
    public List<SchedulerAdminDto> getSchedulerList(){
        List<SchedulerAdmin> schedulers = schedulerAdminRepository.findAll();
        List<SchedulerAdminDto> schedulerAdminDtoList = new ArrayList<>();

        for(SchedulerAdmin scheduler : schedulers) {
            SchedulerAdminDto schedulerAdminDto = SchedulerAdminDto.builder()
                    .user(scheduler.getUser())
                    .scheduleStart(scheduler.getScheduleStart())
                    .scheduleEnd(scheduler.getScheduleEnd())
                    .title(scheduler.getTitle())
                    .description(scheduler.getDescription())
                    .image(scheduler.getImage())
                    .createdAt(scheduler.getCreatedAt())
                    .updatedAt(scheduler.getUpdatedAt())
                    .build();
            schedulerAdminDtoList.add(schedulerAdminDto);
        }
        return schedulerAdminDtoList;
    }

    /**
     * 일정을 등록합니다.
     * @param schedulerAdminDto
     * @param token
     * @return
     */
    @Transactional
    public SchedulerAdminDto createScheduler(
            SchedulerAdminDto schedulerAdminDto,
            String token
    ){
        Long loginUserId = jwtTokenProvider.getUserIdFromToken(token);
        User user = userRepository.findById(loginUserId)
                .orElseThrow(()->new IllegalArgumentException("사용자 정보를 찾을 수 없습니다"));
        SchedulerAdmin scheduler = SchedulerAdmin.builder()
                .user(user)
                .scheduleStart(schedulerAdminDto.getScheduleStart())
                .scheduleEnd(schedulerAdminDto.getScheduleEnd())
                .title(schedulerAdminDto.getTitle())
                .description(schedulerAdminDto.getDescription())
                .image(schedulerAdminDto.getImage())
                .build();
        SchedulerAdmin saveScheduler = schedulerAdminRepository.save(scheduler);
        return SchedulerAdminDto.builder()
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
     * @param id
     * @param schedulerAdminDto
     * @return
     */
    @Transactional
    public Long updateScheduler(Long id, SchedulerAdminDto schedulerAdminDto){
        SchedulerAdmin scheduler = schedulerAdminRepository.findById(id).orElseThrow(
                ()-> new IllegalStateException("스케쥴러를 찾을 수 없습니다")
        );
        scheduler.update(
                schedulerAdminDto.getScheduleStart(),
                schedulerAdminDto.getScheduleEnd(),
                schedulerAdminDto.getTitle(),
                schedulerAdminDto.getDescription(),
                schedulerAdminDto.getImage()
        );
        return id;
    }

    /**
     * 일정을 삭제합니다.
     * @param id
     * @param token
     * @return
     */
    @Transactional
    public Long delete(Long id, String token){
       SchedulerAdminDto schedulerAdminDto = getSchedulerById(id);
       Long loginUserId = jwtTokenProvider.getUserIdFromToken(token);

       if(!schedulerAdminDto.getUser().getId().equals(loginUserId)){
           throw new IllegalStateException("스케줄을 삭제할 권한이 없습니다.");
       }

       schedulerAdminRepository.deleteById(id);
       return id;
    }

    /**
     * 사용자 별 일정을 출력합니다.
     * @param keyword
     * @return
     */
    @Transactional
    public List<SchedulerAdminDto> getSchedulerByFullname(String keyword){

        List<SchedulerAdmin> schedulers = schedulerAdminRepository.findByUserFullName(keyword);
        List<SchedulerAdminDto> schedulerAdminDtoList = new ArrayList<>();

        for(SchedulerAdmin scheduler : schedulers){
            SchedulerAdminDto schedulerAdminDto = SchedulerAdminDto.builder()
                    .user(scheduler.getUser())
                    .scheduleStart(scheduler.getScheduleStart())
                    .scheduleEnd(scheduler.getScheduleEnd())
                    .title(scheduler.getTitle())
                    .description(scheduler.getDescription())
                    .image(scheduler.getImage())
                    .createdAt(scheduler.getCreatedAt())
                    .updatedAt(scheduler.getUpdatedAt())
                    .build();
            schedulerAdminDtoList.add(schedulerAdminDto);
        }

        return schedulerAdminDtoList;
    }

    /**
     * 사용자 id로 일정을 검색합니다.
     * @param id
     * @return
     */
    @Transactional
    public SchedulerAdminDto getSchedulerById(Long id){

        SchedulerAdmin scheduler = schedulerAdminRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다.")
        );

        return SchedulerAdminDto.builder()
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

//    public List<SchedulerAdminResponse.scheduleDTO> getAdminScheduleDetail(Long id) {
//
//        return schedulerAdminRepository.findAdminScheduleDetailById(id);
//    }
}
