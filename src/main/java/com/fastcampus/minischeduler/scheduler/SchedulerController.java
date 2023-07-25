package com.fastcampus.minischeduler.scheduler;

import com.fastcampus.minischeduler.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SchedulerController {
    private final SchedulerService schedulerService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/schedulerList")
    public ResponseEntity<List<SchedulerDto>> schedulerList(){
        List<SchedulerDto> schedulerDtoList = schedulerService.getSchedulerList();

        return ResponseEntity.ok(schedulerDtoList);
    }

    @PostMapping("/createScheduler")
    public ResponseEntity<SchedulerDto> createSceduler(@RequestBody SchedulerDto schedulerDto, @RequestHeader(JwtTokenProvider.HEADER) String token){
        SchedulerDto saveScheduler = schedulerService.createScheduler(schedulerDto, token);
        return ResponseEntity.ok(saveScheduler);
    }
}
