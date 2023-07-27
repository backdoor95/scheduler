package com.fastcampus.minischeduler.scheduleruser;

import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fastcampus.minischeduler.core.auth.jwt.JwtTokenProvider;
import com.fastcampus.minischeduler.scheduleradmin.SchedulerAdminDto;
import com.fastcampus.minischeduler.scheduleradmin.SchedulerAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class SchedulerUserController {
    private final SchedulerUserService schedulerUserService;
    private final SchedulerAdminService schedulerAdminService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/schedulerUserList")
    public ResponseEntity<Map<String, Object>> schedulerList(@RequestHeader(JwtTokenProvider.HEADER) String token) {
        List<SchedulerAdminDto> schedulerAdminDtoList = schedulerAdminService.getSchedulerList();
        List<SchedulerUserDto> schedulerUserDtoList = schedulerUserService.getSchedulerUserList(token);

        Map<String, Object> response = new HashMap<>();
        response.put("schedulerAdminDtoList", schedulerAdminDtoList);
        response.put("schedulerUserDtoList", schedulerUserDtoList);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/createUserScheduler")
    public ResponseEntity<SchedulerUserDto> createUserScheduler(
            @RequestBody SchedulerUserDto schedulerUserDto,
            @RequestHeader(JwtTokenProvider.HEADER) String token,
            @RequestParam Long schedulerAdminId
    ){
        DecodedJWT decodedJWT = jwtTokenProvider.verify(token.replace(JwtTokenProvider.TOKEN_PREFIX, ""));
        Long loginUserId = jwtTokenProvider.getUserIdFromToken(token);

        int userTicketCount = schedulerUserService.getUserTicketCount(loginUserId);
        if(userTicketCount > 1){
            return ResponseEntity.ok(schedulerUserService.createSchedulerUser(schedulerAdminId, schedulerUserDto, token));
        }
        else {
            //1개 미만이면 권한없음상태
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
    }
}
