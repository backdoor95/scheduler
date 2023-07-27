package com.fastcampus.minischeduler.scheduleruser;

import com.fastcampus.minischeduler.scheduleradmin.Progress;
import com.fastcampus.minischeduler.scheduleradmin.SchedulerAdmin;
import com.fastcampus.minischeduler.user.User;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.*;
import java.time.LocalDateTime;


@Getter
@Entity(name = "scheduler_user_tb")
@Table
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SchedulerUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private SchedulerAdmin schedulerAdmin;

    @Column(nullable = false)
    private LocalDateTime scheduleStart;

    @Builder.Default
    private Progress progress = Progress.WAITING;

    @Column(nullable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public SchedulerUser(
            Long id,
            User user,
            SchedulerAdmin schedulerAdmin,
            LocalDateTime scheduleStart,
            Progress progress,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.user = user;
        this.schedulerAdmin = schedulerAdmin;
        this.scheduleStart = scheduleStart;
        this.progress = progress;
        this.createdAt = createdAt;

    }
}
