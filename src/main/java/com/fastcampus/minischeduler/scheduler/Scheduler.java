package com.fastcampus.minischeduler.entity;

import com.fastcampus.minischeduler.user.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Entity(name = "scheduler_tb")
@Table
public class Scheduler {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @Column(nullable = false)
    private Category category;

    @Column(nullable = false)
    private LocalDateTime date;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Builder
    public Scheduler(
            Long id,
            User user,
            Category category,
            LocalDateTime date,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.user = user;
        this.category = category;
        this.date = date;
        this.createdAt = createdAt;

    }
}
