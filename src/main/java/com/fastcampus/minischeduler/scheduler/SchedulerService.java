package com.fastcampus.minischeduler.scheduler;

import com.fastcampus.minischeduler.security.JwtTokenProvider;
import com.fastcampus.minischeduler.user.User;
import com.fastcampus.minischeduler.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.security.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Transactional
    public List<SchedulerDto> getSchedulerList(){
        List<Scheduler> schedulers = schedulerRepository.findAll();
        List<SchedulerDto> schedulerDtoList = new ArrayList<>();
        for(Scheduler scheduler : schedulers){
            SchedulerDto schedulerDto = SchedulerDto.builder()
                    .id(scheduler.getId())
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

    @Transactional
    public SchedulerDto createScheduler(SchedulerDto schedulerDto, String token){
        Long loginUserId = jwtTokenProvider.getUserIdFromToken(token);
        User user = userRepository.findById(loginUserId)
                .orElseThrow(()->new IllegalArgumentException("사용자 정보를 찾을 수 없습니다"));
        Scheduler scheduler = Scheduler.builder()
                .user(user)
                .category(Category.ANNUAL_LEAVE)
                .scheduleStart(schedulerDto.getScheduleStart())
                .scheduleEnd(schedulerDto.getScheduleEnd())
                .title(schedulerDto.getTitle())
                .description(schedulerDto.getDescription())
                .confirm(false)
                .build();
        Scheduler saveScheduler = schedulerRepository.save(scheduler);
        return SchedulerDto.builder()
                .id(saveScheduler.getId())
                .user(saveScheduler.getUser())
                .category(saveScheduler.getCategory())
                .scheduleStart(saveScheduler.getScheduleStart())
                .scheduleEnd(saveScheduler.getScheduleEnd())
                .title(saveScheduler.getTitle())
                .description(saveScheduler.getDescription())
                .confirm(saveScheduler.isConfirm())
                .createdAt(saveScheduler.getCreatedAt())
                .updatedAt(saveScheduler.getUpdatedAt())
                .build();
    }
}
