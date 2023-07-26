package com.fastcampus.minischeduler.scheduler;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SchedulerRepository extends JpaRepository<Scheduler, Long> {
    List<Scheduler> findByUserFullName(String keyword);
}
