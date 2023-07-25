package com.fastcampus.minischeduler.utils;

import com.fastcampus.minischeduler.core.exception.Exception401;
import com.fastcampus.minischeduler.core.exception.Exception403;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class FilterResponseUtils {

    public static void unAuthorized(
            HttpServletResponse httpServletResponse,
            Exception401 e
    ) throws IOException {

        httpServletResponse.setStatus(e.status().value());
        httpServletResponse.setContentType("application/json; charset=utf-8");
        ObjectMapper om = new ObjectMapper();
        String responseBody = om.writeValueAsString(e.body());
        httpServletResponse.getWriter().println(responseBody);
    }

    public static void forbidden(
            HttpServletResponse httpServletResponse,
            Exception403 e
    ) throws IOException {

        httpServletResponse.setStatus(e.status().value());
        httpServletResponse.setContentType("application/json; charset=utf-8");
        ObjectMapper om = new ObjectMapper();
        String responseBody = om.writeValueAsString(e.body());
        httpServletResponse.getWriter().println(responseBody);
    }
}
