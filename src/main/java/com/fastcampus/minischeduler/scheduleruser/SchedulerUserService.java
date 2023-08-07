package com.fastcampus.minischeduler.scheduleruser;

import com.fastcampus.minischeduler.core.auth.jwt.JwtTokenProvider;
import com.fastcampus.minischeduler.core.utils.AES256Utils;
import com.fastcampus.minischeduler.scheduleradmin.SchedulerAdmin;
import com.fastcampus.minischeduler.scheduleradmin.SchedulerAdminRepository;
import com.fastcampus.minischeduler.scheduleruser.SchedulerUserResponse.SchedulerUserResponseDto;
import com.fastcampus.minischeduler.user.User;
import com.fastcampus.minischeduler.user.UserRepository;
import com.fastcampus.minischeduler.user.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SchedulerUserService {

    private final SchedulerUserRepository schedulerUserRepository;
    private final SchedulerAdminRepository schedulerAdminRepository;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final AES256Utils aes256Utils;

    /**
     * token으로 사용자를 찾아 사용자가 작성한 모든 schedule을 반환합니다.
     * @param loginUserId
     * @return List<SchedulerUserResponseDto>
     */
    @Transactional
    public List<SchedulerUserResponseDto> getSchedulerUserList(Long loginUserId) throws GeneralSecurityException {

        User user = userRepository.findById(loginUserId)
                .orElseThrow(()->new IllegalArgumentException("사용자 정보를 찾을 수 없습니다"));
        Long userId = user.getId();
        List<SchedulerUser> schedulerUsers = schedulerUserRepository.findByUserId(userId);
        List<SchedulerUserResponseDto> schedulerUserDtoList = new ArrayList<>();

        for (SchedulerUser schedulerUser : schedulerUsers) {
            UserResponse.UserDto responseUser = new UserResponse.UserDto(schedulerUser.getUser());
            responseUser.setFullName(aes256Utils.decryptAES256(responseUser.getFullName()));
            responseUser.setEmail(aes256Utils.decryptAES256(responseUser.getEmail()));

            SchedulerUserResponseDto schedulerUserDto =
                SchedulerUserResponseDto.builder()
                        .user(responseUser)
                        .id(schedulerUser.getId())
                        .schedulerAdmin(schedulerUser.getSchedulerAdmin())
                        .scheduleStart(schedulerUser.getScheduleStart())
                        .progress(schedulerUser.getProgress())
                        .createdAt(schedulerUser.getCreatedAt())
                        .build();
            schedulerUserDtoList.add(schedulerUserDto);
        }
        return schedulerUserDtoList;
    }

    /**
     * token으로 사용자를 찾아 사용자가 작성한 모든 schedule중 year와 month에 부합하는 스케줄을 반환합니다.
     * @param loginUserId
     * @param year
     * @param month
     * @return List<SchedulerUserResponseDto>
     */
    public List<SchedulerUserResponseDto> getSchedulerUserListByYearAndMonth(
            Long loginUserId,
            Integer year,
            Integer month
    ) throws GeneralSecurityException {

        YearMonth yearMonth = YearMonth.of(year, month);
        User user = userRepository.findById(loginUserId)
                .orElseThrow(()->new IllegalArgumentException("사용자 정보를 찾을 수 없습니다"));
        Long userId = user.getId();
        List<SchedulerUser> schedulerUsers = schedulerUserRepository.findByUserId(userId);
        List<SchedulerUserResponseDto> schedulerUserDtoList = new ArrayList<>();

        for (SchedulerUser schedulerUser : schedulerUsers) {

            LocalDateTime scheduleStart = schedulerUser.getScheduleStart();
            YearMonth scheduleYearMonth = YearMonth.of(scheduleStart.getYear(), scheduleStart.getMonth());

            UserResponse.UserDto responseUser = new UserResponse.UserDto(schedulerUser.getUser());
            responseUser.setFullName(aes256Utils.decryptAES256(responseUser.getFullName()));
            responseUser.setEmail(aes256Utils.decryptAES256(responseUser.getEmail()));

            if (yearMonth.equals(scheduleYearMonth)) {

                SchedulerUserResponseDto schedulerUserDto =
                    SchedulerUserResponseDto.builder()
                            .user(responseUser)
                            .id(schedulerUser.getId())
                            .schedulerAdmin(schedulerUser.getSchedulerAdmin())
                            .scheduleStart(schedulerUser.getScheduleStart())
                            .progress(schedulerUser.getProgress())
                            .createdAt(schedulerUser.getCreatedAt())
                            .build();
                schedulerUserDtoList.add(schedulerUserDto);
            }
        }
        return schedulerUserDtoList;
    }

    /**
     * schedule 등록 : token으로 사용자를 찾아 사용자의 티켓수를 감소시키고 내용을 저장합니다
     * @param schedulerAdminId
     * @param loginUserId
     * @return SchedulerUserResponseDto
     */
    @Transactional
    public SchedulerUserResponseDto createSchedulerUser(
            Long schedulerAdminId,
            SchedulerUserRequest.SchedulerUserRequestDto schedulerUserRequestDto,
            Long loginUserId
    ) throws GeneralSecurityException {

        User user = userRepository.findById(loginUserId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다"));
        //유저의 티켓수 1차감
        int ticket = user.getSizeOfTicket() - 1;
        user.setSizeOfTicket(ticket);

        SchedulerAdmin schedulerAdmin = schedulerAdminRepository.findById(schedulerAdminId)
                .orElseThrow(() -> new IllegalArgumentException("스케줄을 찾을 수 없습니다"));

        SchedulerUser schedulerUser =
            SchedulerUser.builder()
                    .user(user)
                    .scheduleStart(schedulerUserRequestDto.getScheduleStart())
                    .schedulerAdmin(schedulerAdmin)
                    .createdAt(schedulerUserRequestDto.getCreatedAt())
                    .build();
        SchedulerUser saveSchedulerUser = schedulerUserRepository.save(schedulerUser);

        UserResponse.UserDto responseUser = new UserResponse.UserDto(saveSchedulerUser.getUser());
        responseUser.setEmail(aes256Utils.decryptAES256(responseUser.getEmail()));
        responseUser.setFullName(aes256Utils.decryptAES256(responseUser.getFullName()));

        return SchedulerUserResponseDto.builder()
                .user(responseUser)
                .id(saveSchedulerUser.getId())
                .schedulerAdmin(saveSchedulerUser.getSchedulerAdmin())
                .scheduleStart(saveSchedulerUser.getScheduleStart())
                .progress(saveSchedulerUser.getProgress())
                .createdAt(saveSchedulerUser.getCreatedAt())
                .build();
    }

    /**
     * 사용자 티켓 수 확인
     * @param userId
     * @return sizeOfTicket
     */
    public int getUserTicketCount(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));

        return user.getSizeOfTicket();
    }

    /**
     * 한달에 한번만 신청할수 있게 사용자의 전체 신청내역 날짜와 비교함
     * @param userId
     * @param scheduleStart
     * @return boolean
     */
    public boolean existingSchedulerInCurrentMonth(
            Long userId,
            LocalDateTime scheduleStart
    ) {

        List<SchedulerUser> schedulerUsers = schedulerUserRepository.findByUserId(userId);
        int year = scheduleStart.getYear();
        int month = scheduleStart.getMonthValue();

        for (SchedulerUser schedulerUser : schedulerUsers) {

            LocalDateTime exitScheduleStart = schedulerUser.getScheduleStart();
            int exitYear = exitScheduleStart.getYear();
            int exitMonth = exitScheduleStart.getMonthValue();

            if (month == exitMonth && year == exitYear) return true;
        }
        return false;
    }

    /**
     * token으로 사용자를 찾고 schedule의 id로 작성한 스케줄의 userId와 비교해 권한확인을 함
     * 삭제되면 티켓수를 1개 다시 되돌려주고 삭제함
     * @param id
     * @param loginUserId
     */
    public void cancel(Long id, Long loginUserId) throws GeneralSecurityException {
        User user = userRepository.findById(loginUserId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다"));
        SchedulerUserResponseDto schedulerUserDto = getSchedulerById(id);
        if (!schedulerUserDto.getUser().getId().equals(loginUserId))
            throw new IllegalStateException("스케줄을 삭제할 권한이 없습니다.");

        //삭제하면 티켓수를 다시 되돌려줌
        int ticket = user.getSizeOfTicket() + 1;
        user.setSizeOfTicket(ticket);

        schedulerUserRepository.deleteById(id);

    }

    /**
     * id로 스케줄을 찾아 반환
     * @param id
     * @return SchedulerUserResponseDto
     */
    public SchedulerUserResponseDto getSchedulerById(Long id) throws GeneralSecurityException {

        SchedulerUser schedulerUser = schedulerUserRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("해당 티켓팅은 존재하지 않습니다.")
        );

        UserResponse.UserDto responseUser = new UserResponse.UserDto(schedulerUser.getUser());
        responseUser.setFullName(aes256Utils.decryptAES256(responseUser.getFullName()));
        responseUser.setEmail(aes256Utils.decryptAES256(responseUser.getEmail()));

        return SchedulerUserResponseDto.builder()
                .user(responseUser)
                .id(schedulerUser.getId())
                .schedulerAdmin(schedulerUser.getSchedulerAdmin())
                .scheduleStart(schedulerUser.getScheduleStart())
                .progress(schedulerUser.getProgress())
                .createdAt(schedulerUser.getCreatedAt())
                .build();
    }
}
