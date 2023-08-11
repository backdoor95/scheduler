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

    @Query(
            "SELECT " +
                    "sa.id AS adminScheduleId, sa.title AS title, sa.description AS description, " +
                    "su.id AS userScheduleId, su.scheduleStart AS scheduleStart, su.progress AS progress, " +
                    "u.fullName AS fullName " +
            "FROM SchedulerUser AS su " +
            "LEFT OUTER JOIN User AS u " +
            "ON su.user.id = u.id " +
            "LEFT OUTER JOIN SchedulerAdmin AS sa " +
            "ON sa.id = :id"
    )
    List<SchedulerAdminResponse.ScheduleDTO> findSchedulesWithUsersById(Long id);

    @Query(
            "SELECT " +
            "SUM(CASE WHEN su.progress = 'WAITING' THEN 1 ELSE 0 END) AS waiting, " +
            "SUM(CASE WHEN su.progress = 'ACCEPT' THEN 1 ELSE 0 END) AS accepted, " +
            "SUM(CASE WHEN su.progress = 'REFUSE' THEN 1 ELSE 0 END) AS refused " +
            "FROM SchedulerUser AS su WHERE su.schedulerAdmin.id = :id"
    )
    SchedulerAdminResponse.CountProcessDTO countScheduleGroupByProgressById(Long id);

    @Modifying
    @Query("UPDATE SchedulerUser su SET su.progress = :progress WHERE su.id = :schedulerAdminId")
    void updateUserScheduleById(Long schedulerAdminId, Progress progress);

    @Query("SELECT su FROM SchedulerUser su WHERE su.schedulerAdmin.user.id = :id")
    List<SchedulerUser> findAllTicketsByAdminId(Long id);
}
