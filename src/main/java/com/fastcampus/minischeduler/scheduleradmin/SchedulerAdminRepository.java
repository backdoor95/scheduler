package com.fastcampus.minischeduler.scheduleradmin;

import com.fastcampus.minischeduler.scheduleruser.Progress;
import com.fastcampus.minischeduler.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SchedulerAdminRepository extends JpaRepository<SchedulerAdmin, Long> {

    List<SchedulerAdmin> findByUser(User user);

    List<SchedulerAdmin> findByUserFullNameContaining(String keyword);

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

    @Query(value =
            "SELECT SUM(T.WAITING) AS waiting, SUM(T.ACCEPTED) AS accepted, SUM(T.REFUSED) AS refused " +
            "FROM (" +
            "   SELECT " +
            "   CASE WHEN progress = 'WAITING' THEN COUNT(progress) END AS WAITING, " +
            "   CASE WHEN progress = 'ACCEPT' THEN COUNT(progress) END AS ACCEPTED, " +
            "   CASE WHEN progress = 'REFUSE' THEN COUNT(progress) END AS REFUSED " +
            "   FROM scheduler_user_tb WHERE scheduler_admin_id = :id GROUP BY progress) AS T",
            nativeQuery = true)
    SchedulerAdminResponse.CountProcessDTO countScheduleGroupByProgressById(Long id);

    void updateUserSchedule(Long schedulerAdminId, Progress progress);

}
