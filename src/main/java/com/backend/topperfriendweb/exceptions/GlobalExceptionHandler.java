package com.backend.topperfriendweb.exceptions;

import com.backend.topperfriendweb.dto.CommonResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<CommonResponse<Object>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Error: {}", ex.getMessage());

        if (ex.getMessage().contains("already exists")) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(CommonResponse.error(ex.getMessage()));
        }
        if (ex.getMessage().contains("not found")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(CommonResponse.error(ex.getMessage()));
        }
        if (ex.getMessage().contains("Invalid or expired OTP")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(CommonResponse.error(ex.getMessage()));
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(CommonResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<CommonResponse<Object>> handleRuntimeException(RuntimeException ex) {
        log.error("Runtime error: ", ex);

        // Check for specific runtime exceptions
        if (ex.getMessage().contains("Failed to create study plan") ||
                ex.getMessage().contains("Failed to update study plan") ||
                ex.getMessage().contains("Failed to delete study plan") ||
                ex.getMessage().contains("Failed to retrieve study plan")) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CommonResponse.error(ex.getMessage()));
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CommonResponse.error("Internal server error"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonResponse<Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.warn("Validation error: {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(CommonResponse.error("Validation failed: " + errors.toString()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponse<Object>> handleGenericException(Exception ex) {
        log.error("Internal error: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CommonResponse.error("Internal server error"));
    }
}