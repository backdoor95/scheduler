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
        //유저의 티켓수 1차감
        int ticket = user.getSizeOfTicket() - 1;
        user.setSizeOfTicket(ticket);
        userRepository.save(user);
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

    public Long cancel(Long id, String token) {
        Long loginUserId = jwtTokenProvider.getUserIdFromToken(token);
        User user = userRepository.findById(loginUserId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다"));
        SchedulerUserDto schedulerUserDto = getSchedulerById(id);
        if(!schedulerUserDto.getUser().getId().equals(loginUserId)){
            throw new IllegalStateException("스케줄을 삭제할 권한이 없습니다.");
        }
        //삭제하면 티켓수를 다시 되돌려줌
        int ticket = user.getSizeOfTicket() + 1;
        user.setSizeOfTicket(ticket);
        userRepository.save(user);
        schedulerUserRepository.deleteById(id);
        return id;
    }

    private SchedulerUserDto getSchedulerById(Long id) {
        SchedulerUser schedulerUser = schedulerUserRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("해당 티켓팅은 존재하지 않습니다.")
        );
        return SchedulerUserDto.builder()
                .user(schedulerUser.getUser())
                .schedulerAdmin(schedulerUser.getSchedulerAdmin())
                .scheduleStart(schedulerUser.getScheduleStart())
                .progress(schedulerUser.getProgress())
                .createdAt(schedulerUser.getCreatedAt())
                .build();
    }


}
