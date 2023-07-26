package com.fastcampus.minischeduler.user;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Table(name = "user_tb")
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 20)
    private String email;

    @Column(nullable = false, length = 120)
    private String password;

    private Integer sizeOfTicket;
    private String profileImage;

    @Builder.Default
    private String role = "USER";

    @Column(nullable = false, length = 20)
    private String fullName;

    @Column(nullable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    private LocalDateTime latestLogin; // 업데이트 메서드 필요

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    protected void onUpdateLatestLogin() {
        this.latestLogin = LocalDateTime.now();
    }

    @Builder
    public User(
            Long id,
            String password,
            String email,
            Integer sizeOfTicket,
            String profileImage,
            String role,
            String fullName,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.password = password;
        this.email = email;
        this.sizeOfTicket = sizeOfTicket;
        this.profileImage = profileImage;
        this.role = role;
        this.fullName = fullName;
        this.createdAt = createdAt;
    }
}
