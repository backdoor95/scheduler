package com.fastcampus.minischeduler.scheduleradmin;

import com.fastcampus.minischeduler.scheduleruser.SchedulerUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SchedulerRepository extends JpaRepository<SchedulerUser, Long> {
    List<SchedulerUser> findByUserFullName(String keyword);
}
