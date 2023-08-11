package com.fastcampus.minischeduler.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmail(@Param("email") String email);

    @Query("UPDATE User AS u SET u.sizeOfTicket = 12 WHERE u.role = 'USER'")
    void update12TicketsOfAllFans();

    @Query(value =
            "SELECT " +
                    "sa.title AS title, " +
                    "su.schedule_start AS scheduleStart, " +
                    "su.progress AS progress " +
            "FROM scheduler_user_tb AS su " +
            "INNER JOIN scheduler_admin_tb AS sa " +
            "ON sa.id = su.scheduler_admin_id "+
            "WHERE su.user_id = :id",
            nativeQuery=true)
    List<UserResponse.GetRoleUserTicketDTO> findRoleUserTicketListById(@Param("id") Long id);

    @Query(value =
            "SELECT " +
                    "sa.title AS title, sa.description AS description, " +
                    "sa.schedule_start AS scheduleStart, " +
                    "sa.schedule_start AS scheduleEnd " +
            "FROM scheduler_admin_tb AS sa " +
            "WHERE sa.user_id = :id",
            nativeQuery=true)
    List<UserResponse.AdminScheduleDTO> findRoleAdminScheduleListById(@Param("id") Long id);


    @Query(value = "SELECT COUNT(*) AS registeredEventCount " +
            "FROM scheduler_admin_tb " +
            "WHERE user_id = :id",
            nativeQuery = true)
    Integer countAdminScheduleRegisteredEvent(@Param("id") Long id);

    @Query(
            "SELECT " +
            "SUM(CASE WHEN su.progress = 'WAITING' THEN 1 END) AS waiting, " +
            "SUM(CASE WHEN su.progress = 'ACCEPT' THEN 1 END) AS accepted, " +
            "SUM(CASE WHEN su.progress = 'REFUSE' THEN 1 END) AS refused " +
            "FROM SchedulerUser AS su WHERE su.schedulerAdmin.id = :id"
    )
    UserResponse.GetRoleAdminCountProgressDTO countAllScheduleUserProgressByAdminId(@Param("id") Long id);
}
