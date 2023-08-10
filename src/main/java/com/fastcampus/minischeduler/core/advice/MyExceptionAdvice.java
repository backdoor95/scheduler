package com.fastcampus.minischeduler.core.advice;

import com.fastcampus.minischeduler.core.exception.*;
import com.fastcampus.minischeduler.core.utils.ApiUtils;
import com.fastcampus.minischeduler.manager.exception.AuthException;
import com.fastcampus.minischeduler.manager.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RequiredArgsConstructor
@RestControllerAdvice
public class MyExceptionAdvice {

    @ExceptionHandler(Exception400.class)
    public ResponseEntity<?> badRequest(Exception400 e) {
        return new ResponseEntity<>(e.body(), e.status());
    }

    @ExceptionHandler(Exception401.class)
    public ResponseEntity<?> unAuthorized(Exception401 e) {
        return new ResponseEntity<>(e.body(), e.status());
    }

    @ExceptionHandler(Exception403.class)
    public ResponseEntity<?> forbidden(Exception403 e) {
        return new ResponseEntity<>(e.body(), e.status());
    }

    @ExceptionHandler(Exception404.class)
    public ResponseEntity<?> notFound(Exception404 e) {
        return new ResponseEntity<>(e.body(), e.status());
    }

    @ExceptionHandler(Exception412.class)
    public ResponseEntity<?> preconditionFailed(Exception412 e) {
        return new ResponseEntity<>(e.body(), e.status());
    }

    @ExceptionHandler(Exception500.class)
    public ResponseEntity<?> serverError(Exception500 e) {
        log.error(e.getMessage(), e);
        return new ResponseEntity<>(e.body(), e.status());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> unknownServerError(Exception e) {

        ApiUtils.ApiResult<?> apiResult =
                ApiUtils.error(
                        e.getMessage(),
                        HttpStatus.INTERNAL_SERVER_ERROR
                );
        log.error(e.getMessage(), e);
        return new ResponseEntity<>(apiResult, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        String key = e.getName();
        Exception400 badRequestException = new Exception400(key, "유효하지 않은 입력값입니다.");
        return ResponseEntity.status(badRequestException.status()).body(badRequestException.body());
    }

    @ExceptionHandler(MissingPathVariableException.class)
    public ResponseEntity<?> handleMissingPathVariableException(MissingPathVariableException e) {
        String variableName = e.getVariableName();
        String message = "경로 변수가 누락되었습니다 : " + variableName;
        Exception400 badRequestException = new Exception400(variableName, message);
        return ResponseEntity.status(badRequestException.status()).body(badRequestException.body());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> methodValidException(MethodArgumentNotValidException e) {
        ExceptionValid ev = new ExceptionValid(
                e.getBindingResult().getFieldError().getCode(),
                e.getBindingResult().getFieldError().getDefaultMessage()
        );
        return new ResponseEntity<>(ev.body(), ev.status());
    }

    @ExceptionHandler(CustomException.class)
    public String basicException(Exception e) {

        StringBuilder sb = new StringBuilder();
        sb.append("<script>");
        sb.append("alert('" + e.getMessage() + "');");
        sb.append("history.back();"); // 알림창 띄우고 페이지는 그대로
        sb.append("</script>");
        return sb.toString();
    }

    @ExceptionHandler(AuthException.class)
    public String authException(Exception e) {

        StringBuilder sb = new StringBuilder();
        sb.append("<script>");
        sb.append("alert('" + e.getMessage() + "');");
        sb.append("location.href='/manager/login';"); // 알림창 띄우고 로그인창으로 이동
        sb.append("</script>");
        return sb.toString();
    }
}
