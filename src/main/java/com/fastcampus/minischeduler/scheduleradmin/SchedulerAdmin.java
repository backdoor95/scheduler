package com.fastcampus.minischeduler.scheduleradmin;

import com.fastcampus.minischeduler.user.User;
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
@Table
@AllArgsConstructor
public class SchedulerAdmin { // 기획사 일정
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
            String description,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.user = user;
        this.scheduleStart = scheduleStart;
        this.scheduleEnd = scheduleEnd;
        this.image = image;
        this.title = title;
        this.description = description;
        this.createdAt = createdAt;

    }

    // 이 메서드는 직접 실행하는 로직은 없지만 JPA의 영속성 컨텍스트로 인해 자동으로 실행된다
    // 영속성 컨텍스트에 포함된 Entity 객체의 값이 변경되면 트랜잭션이 종료(commit)되는 시점이 update쿼리 실행 -> 더티 체킹
    public void update(LocalDateTime scheduleStart, LocalDateTime scheduleEnd, String title, String description){
        this.scheduleStart = scheduleStart;
        this.scheduleEnd = scheduleEnd;
        this.title = title;
        this.description = description;
        onUpdate();
    }

}
