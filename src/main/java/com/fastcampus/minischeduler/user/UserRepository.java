package com.fastcampus.minischeduler.user;

import com.fastcampus.minischeduler.scheduleruser.SchedulerUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmail(@Param("email") String email);

//    @Modifying(clearAutomatically = true)
//    @Query("UPDATE User u SET" +
//            " u.password = :password," +
//            " u.profileImage = :profileImage," +
//            " u.updatedAt = :updatedAt WHERE u.id = :id")
//    int updateUserInfo(
//            @Param("password") String password,
//            @Param("profileImage") String profileImage,
//            @Param("updatedAt") LocalDateTime updatedAt,
//            @Param("id") Long id
//    );

    @Query(value = "UPDATE user_tb SET size_of_ticket = 12 WHERE role = 'USER'", nativeQuery = true)
    List<User> update12TicketsOfAllFans();


    // 이상하게 에러가 발생어디가 문제인지 모르겠슴다...---> 이부분은 나중에 공부
//    @Query(value = "SELECT su " +
//            "FROM SchedulerUser su " +
//            "WHERE su.id = :id")
//    List<SchedulerUser> findSchedulerInfoById(@Param("id") Long id);


    @Query(value = "SELECT " +
            "sa.title AS title, " +
            "su.schedule_start AS scheduleStart, " +
            "su.progress AS progress " +
            "FROM scheduler_user_tb AS su " +
            "INNER JOIN scheduler_admin_tb AS sa " +
            "ON sa.id = su.scheduler_admin_id "+
            "WHERE su.user_id = :id"
    ,nativeQuery=true)
    List<UserResponse.GetRoleUserTicketDTO> findRoleUserTicketListById(@Param("id") Long id);



}
