package com.fastcampus.minischeduler.core.utils;

import com.fastcampus.minischeduler.core.exception.Exception401;
import com.fastcampus.minischeduler.core.exception.Exception403;
import com.fastcampus.minischeduler.core.dto.ResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class FilterResponseUtils {

    public static void unAuthorized(
            HttpServletResponse httpServletResponse,
            Exception401 e
    ) throws IOException {

        httpServletResponse.setStatus(e.status().value());
        httpServletResponse.setContentType("application/json; charset=utf-8");
        ResponseDTO<?> responseDto = new ResponseDTO<>(HttpStatus.UNAUTHORIZED, "unAuthorized", e.getMessage());
        ObjectMapper om = new ObjectMapper();
//        String responseBody = om.writeValueAsString(e.body());
        String responseBody = om.writeValueAsString(responseDto);
        httpServletResponse.getWriter().println(responseBody);
    }

    public static void forbidden(
            HttpServletResponse httpServletResponse,
            Exception403 e
    ) throws IOException {

        httpServletResponse.setStatus(e.status().value());
        httpServletResponse.setContentType("application/json; charset=utf-8");
        ResponseDTO<?> responseDto = new ResponseDTO<>(HttpStatus.UNAUTHORIZED, "unAuthorized", e.getMessage());
        ObjectMapper om = new ObjectMapper();
//        String responseBody = om.writeValueAsString(e.body());
        String responseBody = om.writeValueAsString(responseDto);
        httpServletResponse.getWriter().println(responseBody);

    }
}
