package com.kyn.spring_backend.base.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.kyn.spring_backend.base.dto.ErrorResponse;

import reactor.core.publisher.Mono;
import org.springframework.http.HttpStatus;

@RestControllerAdvice
public class GlobalErrorHandler {

    @ExceptionHandler(GeneralException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleUserException(GeneralException ex) {
        ErrorResponse response = ErrorResponse.from(ex);
        return Mono.just(ResponseEntity
                .status(ex.getStatus())
                .body(response));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> handleGenericError(Exception ex) {
        ErrorResponse response = ErrorResponse.create(
                "",
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR");
        return Mono.just(ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response));
    }
}