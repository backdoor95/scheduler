package com.fastcampus.minischeduler.manager;

import com.fastcampus.minischeduler.core.exception.Exception400;
import com.fastcampus.minischeduler.core.exception.Exception500;
import com.fastcampus.minischeduler.manager.exception.AuthException;
import com.fastcampus.minischeduler.manager.exception.CustomException;
import com.fastcampus.minischeduler.user.Role;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.security.GeneralSecurityException;

@Controller
@RequiredArgsConstructor
@RequestMapping("/manager")
public class ManagerController {

    private final HttpSession session;

    private final ManagerService managerService;

    /**
     * 로그인 페이지
     * @return login.html
     */
    @GetMapping("/login")
    public String login() {

        if(session.getAttribute("principal") != null)
            throw new CustomException("이미 로그인되었습니다.");

        return "/login";
    }

    /**
     * 로그인 합니다.
     * @param request 로그인 정보 입력
     * @return redirect:/admin/userList
     */
    @PostMapping("/login")
    public String login(ManagerRequest.LoginRequestDTO request) {

        // 유효성 검사
        if (request.getUsername() == null || request.getUsername().isEmpty())
            throw new CustomException("계정을 입력하세요");
        if (request.getPassword() == null || request.getPassword().isEmpty())
            throw new CustomException("비밀번호를 입력하세요");
        if (managerService.isNotExistId(request.getUsername()))
            throw new CustomException("아이디가 존재하지 않습니다");

        Manager principal = managerService.login(request);
        if (principal == null) throw new CustomException("비밀번호가 틀렸습니다");

        // 로그인 인증 처리 - 세션, 인증 유지 시간 지정
        session.setAttribute("principal", principal);
        session.setMaxInactiveInterval(60 * 30);

        return "redirect:/manager/users";
    }

    /**
     * 세션을 만료시켜 로그아웃 합니다.
     * @return redirect:/manager/login
     */
    @GetMapping("/logout")
    public String logout() {

        session.invalidate();
        return "redirect:/manager/login";
    }

    /**
     * 모든 사용자를 조회합니다.
     * @param role 권한
     * @param model 모델 바인딩
     * @return users.html
     */
    @GetMapping("/users")
    public String users(
            @RequestParam(name = "role", required = false) String role,
            Model model
    ) {
        // 유효성 검사
        if(session.getAttribute("principal") == null)
            throw new AuthException("로그인이 필요합니다");
        if(role != null && (!role.equals("USER") && !role.equals("ADMIN") && !role.equals("ALL")))
            throw new CustomException("카테고리 기준이 잘못되었습니다");

        try {
            model.addAttribute("userList", managerService.getUserListByRole(role));
            return "/users";
        } catch (GeneralSecurityException gse) {
            throw new Exception500("디코딩에 실패하였습니다");
        }
    }

    /**
     * 사용자의 권한을 변경합니다.
     * @param userId 사용자 pk
     * @param role 현재 권한
     * @return redirect:/manager/users
     */
    @GetMapping("/role/{userId}")
    public String role(
            @PathVariable Long userId,
            @RequestParam(name = "role") String role
    ) {
        if (!role.equals("USER") || !role.equals("ADMIN"))
            throw new Exception400(role, "잘못된 요청입니다");

        managerService.updateRoleByUserId(userId, role);

        return "redirect:/manager/users";
    }
}
