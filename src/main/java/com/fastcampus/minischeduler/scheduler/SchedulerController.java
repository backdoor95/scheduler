package com.fastcampus.minischeduler.scheduler;


import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fastcampus.minischeduler.core.auth.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<List<SchedulerDto>> schedulerList(@RequestHeader(JwtTokenProvider.HEADER) String token){
        try{
            DecodedJWT decodedJWT = jwtTokenProvider.verify(token.replace(JwtTokenProvider.TOKEN_PREFIX, ""));
            List<SchedulerDto> schedulerDtoList = schedulerService.getSchedulerList();

            return ResponseEntity.ok(schedulerDtoList);
        }catch (SignatureVerificationException | TokenExpiredException e){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

    }

    @PostMapping("/createScheduler")
    public ResponseEntity<SchedulerDto> createScheduler(@RequestBody SchedulerDto schedulerDto, @RequestHeader(JwtTokenProvider.HEADER) String token){
        SchedulerDto saveScheduler = schedulerService.createScheduler(schedulerDto, token);
        return ResponseEntity.ok(saveScheduler);
    }

    @PostMapping("/updateScheduler/{id}")
    public ResponseEntity<SchedulerDto> updateScheduler(@PathVariable Long id, @RequestBody SchedulerDto schedulerDto, @RequestHeader(JwtTokenProvider.HEADER) String token){
        //스케줄 조회
        SchedulerDto Schedulerdto = schedulerService.getSchedulerById(id);
        //로그인한 사용자 id조회
        Long loginUserId = jwtTokenProvider.getUserIdFromToken(token);

        // 스케줄 작성자 id와 로그인한 사용자 id비교
        if(!Schedulerdto.getUser().getId().equals(loginUserId)){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); //권한없음
        }

        Long updateId = schedulerService.update(id, schedulerDto);
        SchedulerDto updateScheduler = schedulerService.getSchedulerById(updateId);

        return ResponseEntity.ok(updateScheduler);
    }

    @PostMapping("/deleteScheduler/{id}")
    public ResponseEntity<String> deleteScheduler(@PathVariable Long id, @RequestHeader(JwtTokenProvider.HEADER) String token){
        schedulerService.delete(id, token);
        return ResponseEntity.ok("스케줄 삭제 완료");
    }

    @GetMapping("/searchScheduler")
    public ResponseEntity<List<SchedulerDto>> searchScheduler(@RequestParam String keyword){
        System.out.println("controller이다"+keyword);
        List<SchedulerDto> schedulerDtoListFindByFullname = schedulerService.getSchedulerByFullname(keyword);
        return ResponseEntity.ok(schedulerDtoListFindByFullname);
    }
}
