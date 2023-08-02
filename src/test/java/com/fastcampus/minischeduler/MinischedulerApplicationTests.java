package com.fastcampus.minischeduler;

import com.fastcampus.minischeduler.scheduleradmin.SchedulerAdminController;
import com.fastcampus.minischeduler.scheduleradmin.SchedulerAdminService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
class MiniSchedulerApplicationTests {

    @Test
    void contextLoads() {
    }

}
