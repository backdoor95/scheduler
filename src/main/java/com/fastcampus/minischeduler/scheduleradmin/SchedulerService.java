package com.fastcampus.minischeduler.scheduleradmin;

import com.fastcampus.minischeduler.core.auth.jwt.JwtTokenProvider;
import com.fastcampus.minischeduler.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        return null;
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

        return null;
    }

    /**
     * 일정을 수정합니다.
     * @param id
     * @param schedulerDto
     * @return
     */
    @Transactional
    public Long updateScheduler(
            Long id,
            SchedulerDto schedulerDto
    ){

        return null;
    }

    /**
     * 일정을 삭제합니다.
     * @param id
     * @param token
     * @return
     */
    @Transactional
    public Long delete(Long id, String token){

        return null;
    }

    /**
     * 사용자 별 일정을 출력합니다.
     * @param keyword
     * @return
     */
    @Transactional
    public List<SchedulerDto> getSchedulerByFullname(String keyword){

        return null;
    }

    /**
     * 사용자 id로 일정을 검색합니다.
     * @param id
     * @return
     */
    @Transactional
    public SchedulerDto getSchedulerById(Long id){

        return null;
    }
}
