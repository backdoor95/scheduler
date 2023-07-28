package com.fastcampus.minischeduler.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmail(@Param("email") String email);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE User u SET" +
            " u.password = :password," +
            " u.profileImage = :profileImage WHERE u.id = :id")
    int updateUserInfo(
            @Param("password") String password,
            @Param("profileImage") String profileImage,
            @Param("id") Long id);


}
