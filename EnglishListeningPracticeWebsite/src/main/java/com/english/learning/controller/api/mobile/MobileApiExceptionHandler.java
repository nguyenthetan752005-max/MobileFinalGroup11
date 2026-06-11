package com.english.learning.controller.api.mobile;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Global exception handler for all Mobile API controllers.
 * Returns consistent JSON error responses.
 */
@Slf4j
@RestControllerAdvice(assignableTypes = {
        MobileAuthController.class,
        MobileCommentController.class,
        MobileContentApiController.class,
        MobileDictationController.class,
        MobileLeaderboardController.class,
        MobileNotificationController.class,
        MobileProfileController.class,
        MobileProgressController.class,
        MobileSpeakingController.class,
        MobileTrackingController.class
})
public class MobileApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        FieldError fieldError = ex.getBindingResult().getFieldErrors().stream().findFirst().orElse(null);
        String message = fieldError != null ? fieldError.getDefaultMessage() : "Dữ liệu không hợp lệ.";
        return error(400, "VALIDATION_ERROR", message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleBadBody(HttpMessageNotReadableException ex) {
        return error(400, "BAD_REQUEST", "Dữ liệu gửi lên không hợp lệ.");
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<Map<String, Object>> handleMissingHeader(MissingRequestHeaderException ex) {
        return error(400, "MISSING_HEADER", "Thiếu header: " + ex.getHeaderName());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        log.error("Unhandled exception in mobile API", ex);
        return error(500, "INTERNAL_ERROR", "Đã xảy ra lỗi. Vui lòng thử lại sau.");
    }

    private ResponseEntity<Map<String, Object>> error(int status, String code, String message) {
        return ResponseEntity.status(status).body(Map.of(
                "success", false,
                "code", code,
                "message", message
        ));
    }
}
