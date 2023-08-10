package com.fastcampus.minischeduler.manager.exception;

import org.springframework.http.HttpStatus;

public class CustomException extends RuntimeException{

    private HttpStatus status;

    public CustomException(String message) {
        super(message);
    }

    public CustomException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
