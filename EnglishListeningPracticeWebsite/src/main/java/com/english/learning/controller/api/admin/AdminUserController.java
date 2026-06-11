package com.english.learning.controller.api.admin;

import com.english.learning.entity.User;
import com.english.learning.exception.ResourceNotFoundException;
import com.english.learning.repository.UserRepository;
import com.english.learning.service.auth.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Admin API quản lý người dùng.
 * Khóa/mở khóa tài khoản, quản lý trạng thái.
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserRepository userRepository;
    private final TokenBlacklistService tokenBlacklistService;

    /**
     * Khóa tài khoản tạm thời (is_active = false).
     */
    @PostMapping("/{userId}/suspend")
    public ResponseEntity<Map<String, String>> suspendUser(
            @PathVariable Long userId,
            @RequestBody SuspendRequest request) {
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User không tồn tại"));
        
        // Set is_active = false để khóa tài khoản
        user.setIsActive(false);
        user.setSuspensionReason(request.getReason());
        userRepository.save(user);
        
        // Revoke tất cả tokens của user
        tokenBlacklistService.revokeAllUserTokens(userId);
        
        String message = String.format("Đã khóa tài khoản %s", user.getEmail());
        
        return ResponseEntity.ok(Map.of("message", message));
    }
    
    /**
     * Cấm tài khoản vĩnh viễn (is_deleted = true).
     */
    @PostMapping("/{userId}/ban")
    public ResponseEntity<Map<String, String>> banUser(
            @PathVariable Long userId,
            @RequestBody SuspendRequest request) {
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User không tồn tại"));
        
        // Set is_deleted = true để cấm vĩnh viễn
        user.setIsDeleted(true);
        user.setIsActive(false);
        user.setSuspensionReason(request.getReason());
        userRepository.save(user);
        
        // Revoke tất cả tokens của user
        tokenBlacklistService.revokeAllUserTokens(userId);
        
        String message = String.format("Đã cấm tài khoản %s", user.getEmail());
        
        return ResponseEntity.ok(Map.of("message", message));
    }

    /**
     * Mở khóa tài khoản (is_active = true, is_deleted = false).
     */
    @PostMapping("/{userId}/activate")
    public ResponseEntity<Map<String, String>> activateUser(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User không tồn tại"));
        
        user.setIsActive(true);
        user.setIsDeleted(false);
        user.setSuspensionReason(null);
        userRepository.save(user);
        
        // Clear revocation để user có thể đăng nhập lại
        tokenBlacklistService.clearUserRevocation(userId);
        
        return ResponseEntity.ok(Map.of(
            "message", "Đã mở khóa tài khoản " + user.getEmail()
        ));
    }

    /**
     * Lấy thông tin tài khoản user.
     */
    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> getUserInfo(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User không tồn tại"));
        
        String accountStatus;
        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            accountStatus = "BANNED";
        } else if (Boolean.FALSE.equals(user.getIsActive())) {
            accountStatus = "SUSPENDED";
        } else {
            accountStatus = "ACTIVE";
        }
        
        return ResponseEntity.ok(Map.of(
            "id", user.getId(),
            "email", user.getEmail(),
            "username", user.getUsername(),
            "accountStatus", accountStatus,
            "suspensionReason", user.getSuspensionReason() != null ? user.getSuspensionReason() : "",
            "isActive", user.getIsActive(),
            "isDeleted", user.getIsDeleted()
        ));
    }

    /**
     * Request body cho suspend/ban.
     */
    @Data
    public static class SuspendRequest {
        private String reason;
    }
}
