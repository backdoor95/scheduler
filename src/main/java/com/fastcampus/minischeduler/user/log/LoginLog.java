package com.fastcampus.minischeduler.user.log;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Table(name = "login_log_tb")
@Entity
public class LoginLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private String userAgent;
    private String clientIP;
    private LocalDateTime createdAt;

    @Builder
    public LoginLog(
            Long id,
            Long userId,
            String userAgent,
            String clientIP,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.userId = userId;
        this.userAgent = userAgent;
        this.clientIP = clientIP;
        this.createdAt = createdAt;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
