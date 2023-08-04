package com.fastcampus.minischeduler.core.exception;

import com.fastcampus.minischeduler.core.dto.ResponseDTO;
import com.fastcampus.minischeduler.core.dto.ValidDTO;
import lombok.Getter;
import org.springframework.http.HttpStatus;


// 권한 없음
@Getter
public class Exception413 extends RuntimeException {

    private String key;
    private String value;

    public Exception413(String key, String value) {
        super(key + " : " + value);
        this.key = key;
        this.value = value;
    }

    public Exception413(String message) {
        super(message);
    }

    public ResponseDTO<?> body(){
        ValidDTO validDTO = new ValidDTO(key, value);
        return new ResponseDTO<>(HttpStatus.PAYLOAD_TOO_LARGE, "paylaodTooLarge", validDTO);
    }

    public HttpStatus status(){
        return HttpStatus.PAYLOAD_TOO_LARGE;
    }

}