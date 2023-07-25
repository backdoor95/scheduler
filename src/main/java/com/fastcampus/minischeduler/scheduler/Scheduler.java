package com.fastcampus.minischeduler.scheduler;

import com.fastcampus.minischeduler.user.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity(name = "scheduler_tb")
@Table
public class Scheduler {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private Category category;

    @Column(nullable = false)
    private LocalDateTime scheduleStart;

    @Column(nullable = false)
    private LocalDateTime scheduleEnd;

    @Column(length = 20)
    private String title;

    @Column(columnDefinition = "LONGTEXT")
    private String description;

    private boolean confirm;

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
    public Scheduler(
            Long id,
            User user,
            Category category,
            LocalDateTime scheduleStart,
            LocalDateTime scheduleEnd,
            String title,
            String description,
            boolean confirm,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.user = user;
        this.category = category;
        this.scheduleStart = scheduleStart;
        this.scheduleEnd = scheduleEnd;
        this.title = title;
        this.description = description;
        this.confirm = confirm;
        this.createdAt = createdAt;

    }
}
