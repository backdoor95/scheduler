package com.fastcampus.minischeduler.scheduler;

import com.fastcampus.minischeduler.user.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SchedulerDto {
    private Long id;
    @JsonIgnoreProperties({"hibernateLazyInitializer"})
    private User user;
    private Category category;
    private LocalDateTime scheduleStart;
    private LocalDateTime scheduleEnd;
    private String title;
    private String description;
    private boolean confirm;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder
    public SchedulerDto(Long id, User user, Category category, LocalDateTime scheduleStart, LocalDateTime scheduleEnd, String title, String description, Boolean confirm, LocalDateTime createdAt, LocalDateTime updatedAt){
        this.id = id;
        this.user=user;
        this.category = category;
        this.scheduleStart = scheduleStart;
        this.scheduleEnd = scheduleEnd;
        this.title = title;
        this.description = description;
        this.confirm = confirm;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
