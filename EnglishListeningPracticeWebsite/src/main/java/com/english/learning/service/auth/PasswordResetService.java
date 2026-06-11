package com.english.learning.service.auth;

import com.english.learning.entity.User;

public interface PasswordResetService {
    String createTokenForUser(User user);

    void sendResetEmail(String email, String token);

    void sendProfilePasswordChangeEmail(String email, String token);

    void sendAdminPasswordChangeEmail(String email, String token);

    void resetPassword(String token, String newPassword);
}

