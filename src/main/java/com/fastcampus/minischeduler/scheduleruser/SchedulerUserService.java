package com.fastcampus.minischeduler.scheduleruser;

import com.fastcampus.minischeduler.core.auth.jwt.JwtTokenProvider;
import com.fastcampus.minischeduler.scheduleradmin.SchedulerAdmin;
import com.fastcampus.minischeduler.scheduleradmin.SchedulerAdminRepository;
import com.fastcampus.minischeduler.user.User;
import com.fastcampus.minischeduler.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SchedulerUserService {
    private final SchedulerUserRepository schedulerUserRepository;
    private final SchedulerAdminRepository schedulerAdminRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public List<SchedulerUserDto> getSchedulerUserList(String token){
        Long loginUserId = jwtTokenProvider.getUserIdFromToken(token);
        User user = userRepository.findById(loginUserId)
                .orElseThrow(()->new IllegalArgumentException("사용자 정보를 찾을 수 없습니다"));
        Long userId = user.getId();
        List<SchedulerUser> schedulerUsers = schedulerUserRepository.findByUserId(userId);
        List<SchedulerUserDto> schedulerUserDtoList = new ArrayList<>();

        for(SchedulerUser schedulerUser : schedulerUsers){
            SchedulerUserDto schedulerUserDto = SchedulerUserDto.builder()
                    .user(schedulerUser.getUser())
                    .schedulerAdmin(schedulerUser.getSchedulerAdmin())
                    .scheduleStart(schedulerUser.getScheduleStart())
                    .progress(schedulerUser.getProgress())
                    .createdAt(schedulerUser.getCreatedAt())
                    .build();
            schedulerUserDtoList.add(schedulerUserDto);
        }
        return schedulerUserDtoList;

    }

    @Transactional
    public SchedulerUserDto createSchedulerUser(
            Long schedulerAdminId,
            SchedulerUserDto schedulerUserDto,
            String token
    ){
        Long loginUserId = jwtTokenProvider.getUserIdFromToken(token);
        User user = userRepository.findById(loginUserId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다"));
        decreaseUserTicket(user);

        SchedulerAdmin schedulerAdmin = schedulerAdminRepository.findById(schedulerAdminId)
                .orElseThrow(() -> new IllegalArgumentException("스케줄을 찾을 수 없습니다"));

        SchedulerUser schedulerUser = SchedulerUser.builder()
                .user(user)
                .scheduleStart(schedulerUserDto.getScheduleStart())
                .schedulerAdmin(schedulerAdmin)
                .createdAt(schedulerUserDto.getCreatedAt())
                .build();
        SchedulerUser saveSchedulerUser = schedulerUserRepository.save(schedulerUser);
        return SchedulerUserDto.builder()
                .user(saveSchedulerUser.getUser())
                .schedulerAdmin(saveSchedulerUser.getSchedulerAdmin())
                .scheduleStart(saveSchedulerUser.getScheduleStart())
                .progress(saveSchedulerUser.getProgress())
                .createdAt(saveSchedulerUser.getCreatedAt())
                .build();

    }

    /**
     * 사용자 티켓 수 확인
     */
    public int getUserTicketCount(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));
        return user.getSizeOfTicket();
    }

    /**
     * 사용자 티켓수 감소
     */
    public void decreaseUserTicket(User user){
        int ticket = user.getSizeOfTicket() - 1;
        user.setSizeOfTicket(ticket);
        userRepository.save(user);
    }
}
