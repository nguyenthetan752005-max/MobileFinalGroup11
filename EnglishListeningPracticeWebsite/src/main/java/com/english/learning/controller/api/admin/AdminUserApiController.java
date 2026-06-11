package com.english.learning.controller.api.admin;

import com.english.learning.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminUserApiController {

    private final UserService userService;

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> softDeleteUser(@PathVariable Long id) {
        return AdminApiResponses.action(() -> userService.softDeleteUser(id), "Da xoa mem nguoi dung.");
    }

    @PostMapping("/users/{id}/restore")
    public ResponseEntity<Map<String, Object>> restoreUser(@PathVariable Long id) {
        return AdminApiResponses.action(() -> userService.restoreUser(id), "Da khoi phuc nguoi dung.");
    }

    @DeleteMapping("/trash/users/{id}")
    public ResponseEntity<Map<String, Object>> hardDeleteUser(@PathVariable Long id) {
        return AdminApiResponses.action(() -> userService.hardDeleteUser(id), "Da xoa vinh vien nguoi dung.");
    }
}
