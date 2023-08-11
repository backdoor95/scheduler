package com.fastcampus.minischeduler.scheduleradmin;

import com.fastcampus.minischeduler.scheduleruser.Progress;
import com.fastcampus.minischeduler.scheduleruser.SchedulerUser;
import com.fastcampus.minischeduler.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SchedulerAdminRepository extends JpaRepository<SchedulerAdmin, Long> {

    List<SchedulerAdmin> findByUser(User user);

    @Query(value =
            "SELECT sa.id AS adminScheduleId, sa.title, sa.description, " +
            "su.id AS userScheduleId, su.schedule_start AS scheduleStart, su.progress, u.full_name AS fullName " +
            "FROM scheduler_user_tb AS su " +
            "LEFT OUTER JOIN user_tb AS u " +
            "ON su.user_id = u.id " +
            "LEFT OUTER JOIN scheduler_admin_tb AS sa " +
            "ON sa.id = :id",
            nativeQuery = true)
    List<SchedulerAdminResponse.ScheduleDTO> findSchedulesWithUsersById(Long id);

    @Query(
            "   SELECT " +
            "   SUM(CASE WHEN su.progress = 'WAITING' THEN 1 ELSE 0 END) AS waiting, " +
            "   SUM(CASE WHEN su.progress = 'ACCEPT' THEN 1 ELSE 0 END) AS accepted, " +
            "   SUM(CASE WHEN su.progress = 'REFUSE' THEN 1 ELSE 0 END) AS refused " +
            "   FROM SchedulerUser AS su WHERE su.schedulerAdmin.id = :id")
    SchedulerAdminResponse.CountProcessDTO countScheduleGroupByProgressById(Long id);

    @Modifying
    @Query("UPDATE SchedulerUser su SET su.progress = :progress WHERE su.id = :schedulerAdminId")
    void updateUserScheduleById(Long schedulerAdminId, Progress progress);

    @Query("SELECT su FROM SchedulerUser su WHERE su.schedulerAdmin.user.id = :id")
    List<SchedulerUser> findAllTicketsByAdminId(Long id);
}
