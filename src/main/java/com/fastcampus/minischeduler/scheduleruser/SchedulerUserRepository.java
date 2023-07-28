package com.fastcampus.minischeduler.scheduleruser;

import com.fastcampus.minischeduler.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SchedulerUserRepository extends JpaRepository<SchedulerUser, Long> {

    List<SchedulerUser> findByUserId(Long userId);
    boolean existsByUserAndCreatedAtBetween(User user, LocalDateTime startOfMonth, LocalDateTime endOfMonth);

    List<SchedulerUser> findByUser(User user);
}
