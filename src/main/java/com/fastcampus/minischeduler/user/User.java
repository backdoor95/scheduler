package com.fastcampus.minischeduler.user;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Calendar;

@Getter
@Table(name = "user_tb")
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 30)
    private String email;

    @Column(nullable = false, length = 120)
    private String password;

    @Column
    private Integer sizeOfTicket;

    @Column
    private String profileImage;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private Role role;

    @Column(nullable = false, length = 20)
    private String fullName;

    @Column
    @Builder.Default
    private Integer usedTicket = 0;

    @Column(nullable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    @Column
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime latestLogin;

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

    public void setSizeOfTicket(Integer sizeOfTicket) {
        this.sizeOfTicket = sizeOfTicket;
    }

    public void updateUserInfo(String password, String profileImage){
        this.password = password;
        this.profileImage = profileImage;
        onUpdate();
    }
}