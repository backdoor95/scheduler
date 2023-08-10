package com.fastcampus.minischeduler.manager.login;

import com.fastcampus.minischeduler.manager.Manager;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class LoginCheckInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) throws Exception {

        // 1. 세션에서 회원 조회
        HttpSession session = request.getSession();
        Manager manager = (Manager) session.getAttribute("principal");

        // 2. 회원 정보 체크
        if (manager == null) {
            response.sendRedirect("/manager/login");
            return false;
        }

        return HandlerInterceptor.super.preHandle(request, response, handler);
    }
}
