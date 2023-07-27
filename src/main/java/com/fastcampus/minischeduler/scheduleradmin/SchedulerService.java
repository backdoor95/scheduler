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
public class SchedulerService {

    private final SchedulerRepository schedulerRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 전체 일정 목록을 출력합니다.
     * @return
     */
    @Transactional
    public List<SchedulerDto> getSchedulerList(){
        List<SchedulerUser> schedulers = schedulerRepository.findAll();
        List<SchedulerDto> schedulerDtoList = new ArrayList<>();

        for(SchedulerUser scheduler : schedulers) {
            SchedulerDto schedulerDto = SchedulerDto.builder()
                    .user(scheduler.getUser())
                    .category(scheduler.getCategory())
                    .scheduleStart(scheduler.getScheduleStart())
                    .scheduleEnd(scheduler.getScheduleEnd())
                    .title(scheduler.getTitle())
                    .description(scheduler.getDescription())
                    .createdAt(scheduler.getCreatedAt())
                    .updatedAt(scheduler.getUpdatedAt())
                    .build();
            schedulerDtoList.add(schedulerDto);
        }
        return schedulerDtoList;
    }

    /**
     * 일정을 등록합니다.
     * @param schedulerDto
     * @param token
     * @return
     */
    @Transactional
    public SchedulerDto createScheduler(
            SchedulerDto schedulerDto,
            String token
    ){

        Long loginUserId = jwtTokenProvider.getUserIdFromToken(token);
        User user = userRepository.findById(loginUserId)
                .orElseThrow(()->new IllegalArgumentException("사용자 정보를 찾을 수 없습니다"));
        SchedulerUser scheduler = SchedulerUser.builder()
                .user(user)
                .category(Category.ANNUAL_LEAVE)
                .scheduleStart(schedulerDto.getScheduleStart())
                .scheduleEnd(schedulerDto.getScheduleEnd())
                .title(schedulerDto.getTitle())
                .description(schedulerDto.getDescription())
                .build();
        SchedulerUser saveScheduler = schedulerRepository.save(scheduler);
        return SchedulerDto.builder()
                .user(saveScheduler.getUser())
                .category(saveScheduler.getCategory())
                .scheduleStart(saveScheduler.getScheduleStart())
                .scheduleEnd(saveScheduler.getScheduleEnd())
                .title(saveScheduler.getTitle())
                .description(saveScheduler.getDescription())
                .createdAt(saveScheduler.getCreatedAt())
                .updatedAt(saveScheduler.getUpdatedAt())
                .build();
    }

    /**
     * 일정을 수정합니다.
     * @param id
     * @param schedulerDto
     * @return
     */
    @Transactional
    public Long updateScheduler(Long id, SchedulerDto schedulerDto){
        SchedulerUser scheduler = schedulerRepository.findById(id).orElseThrow(
                ()-> new IllegalStateException("스케쥴러를 찾을 수 없습니다")
        );
        scheduler.update(
                schedulerDto.getScheduleStart(),
                schedulerDto.getScheduleEnd(),
                schedulerDto.getTitle(),
                schedulerDto.getDescription()
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
       SchedulerDto schedulerDto = getSchedulerById(id);
       Long loginUserId = jwtTokenProvider.getUserIdFromToken(token);

       if(!schedulerDto.getUser().getId().equals(loginUserId)){
           throw new IllegalStateException("스케줄을 삭제할 권한이 없습니다.");
       }

       schedulerRepository.deleteById(id);
       return id;
    }

    /**
     * 사용자 별 일정을 출력합니다.
     * @param keyword
     * @return
     */
    @Transactional
    public List<SchedulerDto> getSchedulerByFullname(String keyword){

        List<SchedulerUser> schedulers = schedulerRepository.findByUserFullName(keyword);
        List<SchedulerDto> schedulerDtoList = new ArrayList<>();

        for(SchedulerUser scheduler : schedulers){
            SchedulerDto schedulerDto = SchedulerDto.builder()
                    .user(scheduler.getUser())
                    .category(scheduler.getCategory())
                    .scheduleStart(scheduler.getScheduleStart())
                    .scheduleEnd(scheduler.getScheduleEnd())
                    .title(scheduler.getTitle())
                    .description(scheduler.getDescription())
                    .createdAt(scheduler.getCreatedAt())
                    .updatedAt(scheduler.getUpdatedAt())
                    .build();
            schedulerDtoList.add(schedulerDto);
        }

        return schedulerDtoList;
    }

    /**
     * 사용자 id로 일정을 검색합니다.
     * @param id
     * @return
     */
    @Transactional
    public SchedulerDto getSchedulerById(Long id){

        SchedulerUser scheduler = schedulerRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다.")
        );

        return SchedulerDto.builder()
                .user(scheduler.getUser())
                .category(scheduler.getCategory())
                .scheduleStart(scheduler.getScheduleStart())
                .scheduleEnd(scheduler.getScheduleEnd())
                .title(scheduler.getTitle())
                .description(scheduler.getDescription())
                .createdAt(scheduler.getCreatedAt())
                .updatedAt(scheduler.getUpdatedAt())
                .build();
    }
}
