package com.fastcampus.minischeduler.scheduleradmin;

import com.fastcampus.minischeduler.core.auth.jwt.JwtTokenProvider;
import com.fastcampus.minischeduler.scheduleruser.SchedulerUser;
import com.fastcampus.minischeduler.scheduleruser.SchedulerUserRepository;
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
    private final SchedulerUserRepository schedulerUserRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 전체 일정 목록을 출력합니다.
     * @return
     */
    @Transactional
    public List<SchedulerAdminResponseDto> getSchedulerList(){
        List<SchedulerAdmin> schedulers = schedulerAdminRepository.findAll();
        List<SchedulerAdminResponseDto> schedulerAdminResponseDtoList = new ArrayList<>();

        for(SchedulerAdmin scheduler : schedulers) {
            SchedulerAdminResponseDto schedulerAdminResponseDto = SchedulerAdminResponseDto.builder()
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
     * 일정을 등록합니다.
     * @param schedulerAdminRequestDto
     * @param token
     * @return
     */
    @Transactional
    public SchedulerAdminResponseDto createScheduler(
            SchedulerAdminRequestDto schedulerAdminRequestDto,
            String token
    ){
        Long loginUserId = jwtTokenProvider.getUserIdFromToken(token);
        User user = userRepository.findById(loginUserId)
                .orElseThrow(()->new IllegalArgumentException("사용자 정보를 찾을 수 없습니다"));
        SchedulerAdmin scheduler = SchedulerAdmin.builder()
                .user(user)
                .scheduleStart(schedulerAdminRequestDto.getScheduleStart())
                .scheduleEnd(schedulerAdminRequestDto.getScheduleEnd())
                .title(schedulerAdminRequestDto.getTitle())
                .description(schedulerAdminRequestDto.getDescription())
                .image(schedulerAdminRequestDto.getImage())
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
     * @param id, schedulerAdminRequestDto
     * @return id
     */
    @Transactional
    public Long updateScheduler(Long id, SchedulerAdminRequestDto schedulerAdminRequestDto){
        SchedulerAdmin scheduler = schedulerAdminRepository.findById(id).orElseThrow(
                ()-> new IllegalStateException("스케쥴러를 찾을 수 없습니다")
        );
        scheduler.update(
                schedulerAdminRequestDto.getScheduleStart(),
                schedulerAdminRequestDto.getScheduleEnd(),
                schedulerAdminRequestDto.getTitle(),
                schedulerAdminRequestDto.getDescription(),
                schedulerAdminRequestDto.getImage()
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

       schedulerAdminRepository.deleteById(id);
       return id;
    }

    /**
     * 사용자 별 일정을 출력합니다.
     * @param keyword
     * @return
     */
    @Transactional
    public List<SchedulerAdminResponseDto> getSchedulerByFullname(String keyword){

        List<SchedulerAdmin> schedulers = schedulerAdminRepository.findByUserFullNameContaining(keyword);
        List<SchedulerAdminResponseDto> schedulerAdminResponseDtoList = new ArrayList<>();

        for(SchedulerAdmin scheduler : schedulers){
            SchedulerAdminResponseDto schedulerAdminResponseDto = SchedulerAdminResponseDto.builder()
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
     * 사용자 id로 일정을 검색합니다.
     * @param id
     * @return
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
     * @return
     */
    public SchedulerAdmin getSchedulerAdminById(Long id) {
        return schedulerAdminRepository.findById(id).orElse(null);
    }

    /**
     * token으로 사용자를 찾아 사용자가 작성한 모든 schedule을 반환합니다.
     * @param token
     * @return
     */
    public List<SchedulerAdminResponseDto> getSchedulerListById(String token) {
        Long loginUserId = jwtTokenProvider.getUserIdFromToken(token);
        User user = userRepository.findById(loginUserId)
                .orElseThrow(()->new IllegalArgumentException("사용자 정보를 찾을 수 없습니다"));
        List<SchedulerAdmin> schedulerAdmins = schedulerAdminRepository.findByUser(user);
        List<SchedulerAdminResponseDto> schedulerAdminResponseDtoList = new ArrayList<>();
        for(SchedulerAdmin schedulerAdmin : schedulerAdmins){
            SchedulerAdminResponseDto schedulerAdminResponseDto = SchedulerAdminResponseDto.builder()
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
        return schedulerAdminResponseDtoList;
    }
}
