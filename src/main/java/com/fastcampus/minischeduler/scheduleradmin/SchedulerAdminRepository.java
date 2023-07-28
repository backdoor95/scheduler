package com.fastcampus.minischeduler.scheduleradmin;

import com.fastcampus.minischeduler.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SchedulerAdminRepository extends JpaRepository<SchedulerAdmin, Long> {

    List<SchedulerAdmin> findByUser(User user);

    List<SchedulerAdmin> findByUserFullNameContaining(String keyword);

//    @Query("SELECT a, u " +
//            "FROM scheduler_admin_tb a " +
//            "LEFT OUTER JOIN scheduler_user_tb u " +
//            "ON a.id = u.scheduler_admin_id")
//    List<SchedulerAdminResponse.scheduleDTO> findAdminScheduleDetailById(Long id);
}
