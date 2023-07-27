package com.fastcampus.minischeduler.scheduleradmin;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SchedulerAdminRepository extends JpaRepository<SchedulerAdmin, Long> {
    List<SchedulerAdmin> findByUserFullName(String keyword);
}
