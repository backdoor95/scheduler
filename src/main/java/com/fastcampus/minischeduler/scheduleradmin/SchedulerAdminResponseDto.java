package com.fastcampus.minischeduler.scheduleradmin;

import com.fastcampus.minischeduler.user.User;
import com.fastcampus.minischeduler.user.UserDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SchedulerAdminResponseDto {

    @JsonIgnoreProperties({"hibernateLazyInitializer"})
    private UserDto user;

    private LocalDateTime scheduleStart;
    private LocalDateTime scheduleEnd;
    private String title;
    private String description;
    private String image;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder
    public SchedulerAdminResponseDto(
            User user,
            LocalDateTime scheduleStart,
            LocalDateTime scheduleEnd,
            String title,
            String description,
            String image,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ){
        this.user = new UserDto(
                user.getId(),
                user.getFullName(),
                user.getSizeOfTicket(),
                user.getRole(),
                user.getProfileImage()
        );
        this.scheduleStart = scheduleStart;
        this.scheduleEnd = scheduleEnd;
        this.title = title;
        this.description = description;
        this.image = image;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
