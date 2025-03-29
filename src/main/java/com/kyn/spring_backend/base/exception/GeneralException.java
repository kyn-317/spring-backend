package com.kyn.spring_backend.base.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class GeneralException extends RuntimeException {
    private final HttpStatus status;
    private final String message;

    public GeneralException(String message, HttpStatus status) {
        super(message);
        this.message = message;
        this.status = status;
    }
}