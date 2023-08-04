package com.fastcampus.minischeduler;

import com.fastcampus.minischeduler.core.auth.jwt.JwtTokenProvider;
import com.fastcampus.minischeduler.scheduleradmin.SchedulerAdminResponse;
import com.fastcampus.minischeduler.scheduleradmin.SchedulerAdminService;
import com.fastcampus.minischeduler.user.Role;
import com.fastcampus.minischeduler.user.UserResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class SchedulerAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SchedulerAdminService schedulerAdminService;
    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @Test
    public void testSchedulerAll() throws Exception{
        int year = 2023;
        int month = 8;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        UserResponse.UserDto dummyUser = new UserResponse.UserDto();
        dummyUser.setId(1L);
        dummyUser.setFullName("윤두준");
        dummyUser.setRole(Role.ADMIN);
        dummyUser.setProfileImage("프로필");
        SchedulerAdminResponse.SchedulerAdminResponseDto dummySchedule1 = new SchedulerAdminResponse.SchedulerAdminResponseDto();
        dummySchedule1.setUser(dummyUser);
        dummySchedule1.setScheduleStart(LocalDateTime.parse("2023-07-28T10:00:00"));
        dummySchedule1.setScheduleEnd(LocalDateTime.parse("2023-07-29T10:00:00"));
        dummySchedule1.setTitle("하이라이트 공연");
        dummySchedule1.setDescription("라이트 환영");

        SchedulerAdminResponse.SchedulerAdminResponseDto dummySchedule2 = new SchedulerAdminResponse.SchedulerAdminResponseDto();
        dummySchedule2.setUser(dummyUser);
        dummySchedule2.setScheduleStart(LocalDateTime.parse("2023-07-28T10:00:00"));
        dummySchedule2.setScheduleEnd(LocalDateTime.parse("2023-07-29T10:00:00"));
        dummySchedule2.setTitle("하이라이트 공연");
        dummySchedule2.setDescription("라이트 환영");

        List<SchedulerAdminResponse.SchedulerAdminResponseDto> dummySchedulers = new ArrayList<>();
        dummySchedulers.add(dummySchedule1);
        dummySchedulers.add(dummySchedule2);

        when(schedulerAdminService.getSchedulerListByYearAndMonth(year, month))
                .thenReturn(dummySchedulers);

        mockMvc.perform(get("/admin/scheduleAll")
                        .header(JwtTokenProvider.HEADER, "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ5eXNAbmF2ZXIuY29tIiwicm9sZSI6IkFETUlOIiwiaWQiOjIsImV4cCI6MTY5MDk3MjEwNH0.4pqU2xGWKymv9qi_Dzj6LdGUloBrC9_G_EkuLMfNldAW7RAxS7UpqpIBUizjYu8vxn49bunESahGqGFAP137ZQ")
                        .param("year", String.valueOf(year))
                        .param("month", String.valueOf(month)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(dummySchedulers.size())))
                .andExpect(jsonPath("$[0].user.fullName").value(dummyUser.getFullName()))
                .andExpect(jsonPath("$[0].user.role").value(dummyUser.getRole().toString()))
                .andExpect(jsonPath("$[0].scheduleStart").value(dummySchedule1.getScheduleStart().format(formatter)))
                .andExpect(jsonPath("$[0].scheduleEnd").value(dummySchedule1.getScheduleEnd().format(formatter)))
                .andExpect(jsonPath("$[0].title").value(dummySchedule1.getTitle()))
                .andExpect(jsonPath("$[0].description").value(dummySchedule1.getDescription()))
                .andExpect(jsonPath("$[1].user.fullName").value(dummyUser.getFullName()))
                .andExpect(jsonPath("$[1].user.role").value(dummyUser.getRole().toString()))
                .andExpect(jsonPath("$[1].scheduleStart").value(dummySchedule2.getScheduleStart().format(formatter)))
                .andExpect(jsonPath("$[1].scheduleEnd").value(dummySchedule2.getScheduleEnd().format(formatter)))
                .andExpect(jsonPath("$[1].title").value(dummySchedule2.getTitle()))
                .andExpect(jsonPath("$[1].description").value(dummySchedule2.getDescription()));
    }

    @Test
    public void testSchedule() throws Exception{
        int year = 2023;
        int month = 8;
        String token = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ5eXNAbmF2ZXIuY29tIiwicm9sZSI6IkFETUlOIiwiaWQiOjIsImV4cCI6MTY5MDk3MjEwNH0.4pqU2xGWKymv9qi_Dzj6LdGUloBrC9_G_EkuLMfNldAW7RAxS7UpqpIBUizjYu8vxn49bunESahGqGFAP137ZQ";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        UserResponse.UserDto dummyUser = new UserResponse.UserDto();
        dummyUser.setId(1L);
        dummyUser.setFullName("윤두준");
        dummyUser.setRole(Role.ADMIN);
        dummyUser.setProfileImage("프로필");
        SchedulerAdminResponse.SchedulerAdminResponseDto dummySchedule1 = new SchedulerAdminResponse.SchedulerAdminResponseDto();
        dummySchedule1.setUser(dummyUser);
        dummySchedule1.setScheduleStart(LocalDateTime.parse("2023-07-28T10:00:00"));
        dummySchedule1.setScheduleEnd(LocalDateTime.parse("2023-07-29T10:00:00"));
        dummySchedule1.setTitle("하이라이트 공연");
        dummySchedule1.setDescription("라이트 환영");

        SchedulerAdminResponse.SchedulerAdminResponseDto dummySchedule2 = new SchedulerAdminResponse.SchedulerAdminResponseDto();
        dummySchedule2.setUser(dummyUser);
        dummySchedule2.setScheduleStart(LocalDateTime.parse("2023-07-28T10:00:00"));
        dummySchedule2.setScheduleEnd(LocalDateTime.parse("2023-07-29T10:00:00"));
        dummySchedule2.setTitle("하이라이트 공연");
        dummySchedule2.setDescription("라이트 환영");

        List<SchedulerAdminResponse.SchedulerAdminResponseDto> dummySchedulers = new ArrayList<>();
        dummySchedulers.add(dummySchedule1);
        dummySchedulers.add(dummySchedule2);

        LinkedHashMap<String, Object> response = new LinkedHashMap<>();
        response.put("schedulerAdminListByYearAndMonth", dummySchedulers);

//        when(schedulerAdminService.getSchedulerListById(token, year, month))
//                .thenReturn(response);

        mockMvc.perform(get("/schedule")
                        .header(JwtTokenProvider.HEADER, token)
                        .param("year", String.valueOf(year))
                        .param("month", String.valueOf(month)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.schedulerAdminListByYearAndMonth", hasSize(dummySchedulers.size())))
                .andExpect(jsonPath("$.schedulerAdminListByYearAndMonth[0].user.fullName").value(dummyUser.getFullName()))
                .andExpect(jsonPath("$.schedulerAdminListByYearAndMonth[0].user.role").value(dummyUser.getRole().toString()))
                .andExpect(jsonPath("$.schedulerAdminListByYearAndMonth[0].scheduleStart").value(dummySchedule1.getScheduleStart().format(formatter)))
                .andExpect(jsonPath("$.schedulerAdminListByYearAndMonth[0].scheduleEnd").value(dummySchedule1.getScheduleEnd().format(formatter)))
                .andExpect(jsonPath("$.schedulerAdminListByYearAndMonth[0].title").value(dummySchedule1.getTitle()))
                .andExpect(jsonPath("$.schedulerAdminListByYearAndMonth[0].description").value(dummySchedule1.getDescription()))
                .andExpect(jsonPath("$.schedulerAdminListByYearAndMonth[1].user.fullName").value(dummyUser.getFullName()))
                .andExpect(jsonPath("$.schedulerAdminListByYearAndMonth[1].user.role").value(dummyUser.getRole().toString()))
                .andExpect(jsonPath("$.schedulerAdminListByYearAndMonth[1].scheduleStart").value(dummySchedule2.getScheduleStart().format(formatter)))
                .andExpect(jsonPath("$.schedulerAdminListByYearAndMonth[1].scheduleEnd").value(dummySchedule2.getScheduleEnd().format(formatter)))
                .andExpect(jsonPath("$.schedulerAdminListByYearAndMonth[1].title").value(dummySchedule2.getTitle()))
                .andExpect(jsonPath("$.schedulerAdminListByYearAndMonth[1].description").value(dummySchedule2.getDescription()));
    }
}