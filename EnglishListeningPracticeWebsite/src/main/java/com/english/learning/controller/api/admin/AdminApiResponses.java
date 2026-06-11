package com.english.learning.controller.api.admin;

import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

final class AdminApiResponses {

    private AdminApiResponses() {
    }

    static ResponseEntity<Map<String, Object>> action(ThrowingRunnable action, String message) {
        try {
            action.run();
            return ResponseEntity.ok(successBody(message));
        } catch (Exception e) {
            return badRequest(e.getMessage());
        }
    }

    static ResponseEntity<Map<String, Object>> entity(ThrowingSupplier<Object> supplier, String message) {
        try {
            Map<String, Object> response = successBody(message);
            response.put("data", supplier.get());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return badRequest(e.getMessage());
        }
    }

    static Map<String, Object> successBody() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        return response;
    }

    static Map<String, Object> successBody(String message) {
        Map<String, Object> response = successBody();
        response.put("message", message);
        return response;
    }

    static ResponseEntity<Map<String, Object>> badRequest(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return ResponseEntity.badRequest().body(response);
    }

    @FunctionalInterface
    interface ThrowingRunnable {
        void run() throws Exception;
    }

    @FunctionalInterface
    interface ThrowingSupplier<T> {
        T get() throws Exception;
    }
}
