package com.english.learning.dto.mobile;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MobileAuthResponse {
    private boolean success;
    private String code;      // Error code (ACCOUNT_LOCKED, TOKEN_EXPIRED, etc.)
    private String message;
    private Long userId;
    private String username;
    private String email;
    private String role;
    private String avatarUrl;
    private String token; // JWT token cho authenticated requests
}
