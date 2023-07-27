package com.fastcampus.minischeduler.scheduleruser;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SchedulerUserRepository extends JpaRepository<SchedulerUser, Long> {
    List<SchedulerUser> findByUserId(Long userId);
}
