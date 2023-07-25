package com.fastcampus.minischeduler.user;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class UserRequest {

    @Setter
    @Getter
    public static class LoginDTO {

        @NotEmpty
        @Pattern(
                regexp = "^[\\w._%+-]+@[\\w.-]+\\.[a-zA-Z]{2,6}$",
                message = "이메일 형식으로 작성해주세요"
        )
        private String email;

        @NotEmpty
        @Size(min = 4, max = 20)
        private String password;
    }

    @Setter
    @Getter
    public static class JoinDTO {

        @NotEmpty
        @Pattern(
                regexp = "^[\\w._%+-]+@[\\w.-]+\\.[a-zA-Z]{2,6}$",
                message = "이메일 형식으로 작성해주세요"
        )
        private String email;

        @NotEmpty
        @Size(min = 4, max = 20)
        private String password;

        @NotEmpty
        @Pattern(
                regexp = "^[a-zA-Z가-힣]{1,20}$",
                message = "한글/영문 1~20자 이내로 작성해주세요"
        )
        private String fullName;

        public User toEntity() {
            return User.builder()
                    .email(email)
                    .password(password)
                    .fullName(fullName)
                    .build();
        }
    }
}
