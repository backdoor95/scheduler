package com.fastcampus.minischeduler.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.lang.reflect.Member;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@SpringBootTest
class UserControllerTest {
//
//    @Autowired
//    private WebApplicationContext context;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private UserController userController;
//
//    @Autowired
//    PasswordEncoder passwordEncoder;
//
//    private MockMvc mvc;

//    @BeforeEach
//    public void setup(){
//    }

//    @Test
//    @DisplayName("회원가입 테스트")
//    void join() {
//        // Given
//
//        // When
//
//        // Then
//
//    }

//    @Test
//    @DisplayName("로그인 테스트")
//    void login() throws Exception {
//        // Given
//
//        // When
//
//        // Then
//    }
}