package com.kyn.spring_backend.modules.user.exception;

import org.springframework.http.HttpStatus;

import com.kyn.spring_backend.base.exception.GeneralException;

public class UserNotFoundException extends GeneralException {
    public UserNotFoundException() {
        super("user not found", HttpStatus.NOT_FOUND);
    }
}