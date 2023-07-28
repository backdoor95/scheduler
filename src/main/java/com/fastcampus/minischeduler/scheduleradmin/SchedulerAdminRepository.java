package com.fastcampus.minischeduler.scheduleradmin;

import com.fastcampus.minischeduler.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SchedulerAdminRepository extends JpaRepository<SchedulerAdmin, Long> {
    List<SchedulerAdmin> findByUser(User user);

    List<SchedulerAdmin> findByUserFullNameContaining(String keyword);
}
