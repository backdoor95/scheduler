package com.fastcampus.minischeduler.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmail(@Param("email") String email);

    @Query(value = "UPDATE user_tb SET size_of_ticket = 12 WHERE role = 'USER'", nativeQuery = true)
    List<User> update12TicketsOfAllFans();

    @Query(value = "SELECT " +
            "sa.title AS title, " +
            "su.schedule_start AS scheduleStart, " +
            "su.progress AS progress " +
            "FROM scheduler_user_tb AS su " +
            "INNER JOIN scheduler_admin_tb AS sa " +
            "ON sa.id = su.scheduler_admin_id "+
            "WHERE su.user_id = :id",
            nativeQuery=true)
    List<UserResponse.GetRoleUserTicketDTO> findRoleUserTicketListById(@Param("id") Long id);

    @Query(value = "SELECT " +
            "sa.title AS title, " +
            "sa.description AS description, " +
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


    @Query(value = "SELECT " +
            "SUM(CASE WHEN progress = 'WAITING' THEN 1 END) AS waiting, " +
            "SUM(CASE WHEN progress = 'ACCEPT' THEN 1 END) AS accepted, " +
            "SUM(CASE WHEN progress = 'REFUSE' THEN 1 END) AS refused " +
            "FROM scheduler_user_tb " +
            "WHERE scheduler_admin_id = :id",
            nativeQuery = true)
    UserResponse.GetRoleAdminCountProgressDTO countAllScheduleUserProgressByAdminId(@Param("id") Long id);
}
