package com.fastcampus.minischeduler.core.exception;

import com.fastcampus.minischeduler.core.dto.ResponseDTO;
import lombok.Getter;
import org.springframework.http.HttpStatus;


// 권한 없음
@Getter
public class Exception412 extends RuntimeException {
    public Exception412(String message) {
        super(message);
    }

    public ResponseDTO<?> body(){
        return new ResponseDTO<>(HttpStatus.PRECONDITION_FAILED, "preconditionFailed", getMessage());
    }

    public HttpStatus status(){
        return HttpStatus.PRECONDITION_FAILED;
    }
}