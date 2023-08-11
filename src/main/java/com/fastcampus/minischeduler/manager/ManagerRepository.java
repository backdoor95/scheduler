package com.fastcampus.minischeduler.manager;

import com.fastcampus.minischeduler.user.Role;
import com.fastcampus.minischeduler.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface ManagerRepository extends JpaRepository<Manager, Long> {

    @Query("SELECT m FROM Manager m WHERE m.username = :username")
    Manager findByUsername(String username);

    @Query("SELECT m FROM Manager m WHERE m.username = :username AND m.password = :password")
    Manager findByUsernameAndPassword(String username, String password);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.role = :role WHERE u.id = :userId")
    void updateRoleByUserId(Long userId, @Param("role") Role role);

    @Query("SELECT u FROM User u WHERE u.role = :role")
    Page<User> findUsersByRole(@Param("role") Role role, Pageable pageable);
}