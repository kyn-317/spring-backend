package com.kyn.spring_backend.modules.user.exception;

import org.springframework.http.HttpStatus;

import com.kyn.spring_backend.base.exception.GeneralException;

public class InvalidCredentialsException extends GeneralException {
    public InvalidCredentialsException() {
        super("Invalid Credentials", HttpStatus.UNAUTHORIZED);
    }
}