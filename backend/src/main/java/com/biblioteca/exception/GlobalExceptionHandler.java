package com.biblioteca.exception;

import com.biblioteca.api.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        String message = e.getMessage();
        if (message == null || message.isBlank()) {
            message = "Error interno del servidor";
        }
        return ResponseEntity.badRequest().body(ApiResponse.error(message));
    }
}