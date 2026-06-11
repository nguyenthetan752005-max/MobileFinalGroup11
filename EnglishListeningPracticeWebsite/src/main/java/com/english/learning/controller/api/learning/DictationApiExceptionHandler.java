package com.english.learning.controller.api.learning;

import com.english.learning.exception.SentenceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice(assignableTypes = DictationApiController.class)
public class DictationApiExceptionHandler {

    @ExceptionHandler(SentenceNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleSentenceNotFound(SentenceNotFoundException exception) {
        Map<String, String> error = new HashMap<>();
        error.put("message", exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationError(MethodArgumentNotValidException exception) {
        Map<String, String> error = new HashMap<>();
        FieldError fieldError = exception.getBindingResult().getFieldError();
        String message = (fieldError != null) ? fieldError.getDefaultMessage() : "Invalid request body";
        error.put("message", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
