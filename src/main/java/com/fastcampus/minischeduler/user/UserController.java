package com.fastcampus.minischeduler.user;

import com.fastcampus.minischeduler.log.LoginLog;
import com.fastcampus.minischeduler.security.JwtTokenProvider;
import com.fastcampus.minischeduler.utils.ApiUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/join")
    public ResponseEntity<?> join(
            @RequestBody
            @Valid
            UserRequest.JoinDTO joinDTO,
            Errors errors
    ) {
        if(errors.hasErrors()) return null;

        return ResponseEntity.ok(ApiUtils.success(userService.signup(joinDTO)));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody
            @Valid
            UserRequest.LoginDTO loginDTO,
            Errors errors
    ) {
        if(errors.hasErrors()) return null;

        return ResponseEntity.ok()
                .header(JwtTokenProvider.HEADER, userService.signin(loginDTO))
                .body(ApiUtils.success(null));
    }

}
