package com.fastcampus.minischeduler.scheduleradmin;

import com.fastcampus.minischeduler.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Entity(name = "scheduler_admin_tb")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class SchedulerAdmin { // 기획사 일정

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Column(nullable = false)
    private LocalDateTime scheduleStart;

    private LocalDateTime scheduleEnd;

    @Column(length = 20)
    private String title;

    @Column(columnDefinition = "LONGTEXT")
    private String description;

    @Column(length = 200)
    private String image;

    @Column(nullable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Builder
    public SchedulerAdmin(
            Long id,
            User user,
            LocalDateTime scheduleStart,
            LocalDateTime scheduleEnd,
            String image,
            String title,
            String description
    ) {
        this.id = id;
        this.user = user;
        this.scheduleStart = scheduleStart;
        this.scheduleEnd = scheduleEnd;
        this.image = image;
        this.title = title;
        this.description = description;
        onCreate();
    }

    public void update(
            LocalDateTime scheduleStart,
            LocalDateTime scheduleEnd,
            String title,
            String description,
            String image
    ){
        this.scheduleStart = scheduleStart;
        this.scheduleEnd = scheduleEnd;
        this.title = title;
        this.description = description;
        this.image = image;
        onUpdate();
    }

}
