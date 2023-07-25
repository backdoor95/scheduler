package com.fastcampus.minischeduler.user;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Table(name = "user_tb")
@Entity
@NoArgsConstructor
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
    private String roles;

    @Column(nullable = false, length = 20)
    private String fullName;

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
    public User(
            Long id,
            String password,
            String email,
            Integer sizeOfTicket,
            String profileImage,
            String roles,
            String fullName,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.password = password;
        this.email = email;
        this.sizeOfTicket = sizeOfTicket;
        this.profileImage = profileImage;
        this.roles = roles;
        this.fullName = fullName;
        this.createdAt = createdAt;
    }
}
