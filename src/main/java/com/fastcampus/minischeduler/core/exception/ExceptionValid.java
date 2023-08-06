package com.fastcampus.minischeduler.core.exception;

import com.fastcampus.minischeduler.core.dto.ResponseDTO;
import com.fastcampus.minischeduler.core.dto.ValidDTO;
import lombok.Getter;
import org.springframework.http.HttpStatus;

// 유효성 검사 실패, 잘못된 파라메터 요청
@Getter
public class ExceptionValid extends RuntimeException {

    private String key;
    private String value;

    public ExceptionValid(String key, String value) {
        super(key + " : " + value);
        this.key = key;
        this.value = value;
    }

    public ExceptionValid(String message) {
        super(message);
    }

    public ResponseDTO<?> body(){
        ValidDTO validDTO = new ValidDTO(key, value);
        return new ResponseDTO<>(HttpStatus.BAD_REQUEST, "badRequest", validDTO);
    }

    public HttpStatus status(){
        return HttpStatus.BAD_REQUEST;
    }
}