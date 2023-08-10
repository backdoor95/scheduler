package com.fastcampus.minischeduler.manager;

import com.fastcampus.minischeduler.core.exception.Exception400;
import com.fastcampus.minischeduler.core.exception.Exception500;
import com.fastcampus.minischeduler.manager.exception.AuthException;
import com.fastcampus.minischeduler.manager.exception.CustomException;
import com.fastcampus.minischeduler.user.User;
import com.fastcampus.minischeduler.user.UserResponse;
import com.fastcampus.minischeduler.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.security.GeneralSecurityException;
import java.util.List;

import static com.fastcampus.minischeduler.core.exception.ErrorCode.*;

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
            throw new CustomException(INVALID_ACCESS.getMessage());

        return "/login";
    }

    /**
     * 로그인 합니다.
     * @param request 로그인 정보 입력
     * @return redirect:/admin/users
     */
    @PostMapping("/login")
    public String login(ManagerRequest.LoginRequestDTO request) {

        if (request.getUsername() == null || request.getUsername().isEmpty())
            throw new CustomException(EMPTY_ID.getMessage());
        if (request.getPassword() == null || request.getPassword().isEmpty())
            throw new CustomException(EMPTY_PASSWORD.getMessage());
        if (managerService.isNotExistId(request.getUsername()))
            throw new CustomException(CHECK_ID.getMessage());

        Manager principal = managerService.login(request);
        if (principal == null) throw new CustomException(CHECK_PASSWORD.getMessage());

        // 로그인 인증 처리 - 세션, 인증 유지 시간 지정
        session.setAttribute("principal", principal);
        session.setMaxInactiveInterval(60 * 30);

        return "redirect:/manager";
    }

    /**
     * 세션을 만료시켜 로그아웃 합니다.
     * @return redirect:/manager/login
     */
    @GetMapping("/logout")
    public String logout() {

        if(session.getAttribute("principal") == null)
            throw new AuthException(INVALID_AUTHENTICATION.getMessage());

        session.invalidate();
        return "redirect:/manager/login";
    }

    /**
     * 모든 사용자를 조회합니다.
     * @param role 권한
     * @param model 모델 바인딩
     * @return users.html
     */
    @GetMapping("")
    public String users(
            @RequestParam(name = "role", required = false) String role,
            @PageableDefault(size = 10) Pageable pageable,
            Model model
    ) {

        if(session.getAttribute("principal") == null)
            throw new AuthException(INVALID_AUTHENTICATION.getMessage());
        if(role != null && (!role.equals("USER") && !role.equals("ADMIN") && !role.equals("ALL")))
            throw new CustomException(INVALID_REQUEST.getMessage());

        try {
            Page<UserResponse.UserDto> userDtoPage = managerService.getUserListByRole(role, pageable);

            int startPage = Math.max(1, userDtoPage.getPageable().getPageNumber() - 4);
            int endPage = Math.min(userDtoPage.getPageable().getPageNumber() + 4, userDtoPage.getTotalPages());

            model.addAttribute("startPage", startPage);
            model.addAttribute("endPage", endPage);
            model.addAttribute("userList", userDtoPage);
            return "/users";
        } catch (Exception500 gse) {
            throw new Exception500(gse.getMessage());
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
        if(session.getAttribute("principal") == null)
            throw new AuthException(INVALID_AUTHENTICATION.getMessage());

        if (!role.equals("USER") && !role.equals("ADMIN"))
            throw new Exception400(role, INVALID_REQUEST.getMessage());

        managerService.updateRoleByUserId(userId, role);

        return "redirect:/manager";
    }
}
