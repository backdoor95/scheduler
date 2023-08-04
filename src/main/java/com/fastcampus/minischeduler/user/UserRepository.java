package com.fastcampus.minischeduler.user;

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

    @Query(
            value = "SELECT * FROM user_tb AS u WHERE u.email = :email AND u.password = :password",
            nativeQuery = true
    )
    Optional<User> findByEmailAndPassword(@Param("email") String email, @Param("password") String password);
}
