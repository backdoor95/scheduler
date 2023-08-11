package com.fastcampus.minischeduler.scheduleradmin;

import com.fastcampus.minischeduler.scheduleruser.Progress;
import com.fastcampus.minischeduler.scheduleruser.SchedulerUser;
import com.fastcampus.minischeduler.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SchedulerAdminRepository extends JpaRepository<SchedulerAdmin, Long> {

    List<SchedulerAdmin> findByUser(User user);

    @Query(
            "SELECT " +
                    "sa.id AS adminScheduleId, " +
                    "sa.title AS title, sa.description AS description, " +
                    "su.id AS userScheduleId, su.scheduleStart AS scheduleStart, su.progress AS progress, " +
                    "u.fullName AS fullName " +
            "FROM SchedulerAdmin AS sa " +
            "INNER JOIN SchedulerUser AS su ON sa.id = su.schedulerAdmin.id " +
            "INNER JOIN User AS u ON su.user.id = u.id " +
            "WHERE sa.user.id = :id"
    )
    List<SchedulerAdminResponse.ScheduleDTO> findSchedulesWithUsersById(@Param("id") Long id);

    @Query(value =
            "SELECT SUM(T.WAITING) AS WAITING, SUM(T.ACCEPTED) AS ACCEPTED, SUM(T.REFUSED) AS REFUSED " +
            "FROM (" +
                "SELECT " +
                "CASE WHEN su.progress = 'WAITING' THEN COUNT(su.progress) END AS WAITING, " +
                "CASE WHEN su.progress = 'ACCEPT' THEN COUNT(su.progress) END AS ACCEPTED, " +
                "CASE WHEN su.progress = 'REFUSE' THEN COUNT(su.progress) END AS REFUSED " +
                "FROM scheduler_user_tb AS su " +
                "INNER JOIN scheduler_admin_tb AS sa ON sa.id = su.scheduler_admin_id " +
                "WHERE sa.user_id = :id GROUP BY su.progress" +
            ") AS T",
            nativeQuery = true
    )
    SchedulerAdminResponse.CountProcessDTO countScheduleGroupByProgressById(@Param("id") Long id);

    @Modifying
    @Query("UPDATE SchedulerUser su SET su.progress = :progress WHERE su.id = :schedulerAdminId")
    void updateUserScheduleById(Long schedulerAdminId, Progress progress);

    @Query("SELECT su FROM SchedulerUser su WHERE su.schedulerAdmin.user.id = :id")
    List<SchedulerUser> findAllTicketsByAdminId(Long id);
}
