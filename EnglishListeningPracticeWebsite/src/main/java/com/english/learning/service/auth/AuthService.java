package com.english.learning.service.auth;

import com.english.learning.entity.User;

import java.util.Optional;

/**
 * SRP: Authentication Service Interface (OOSE / SOLID).
 *
 * Handles ONLY authentication-related concerns:
 * - User registration
 * - User/Admin login authentication
 * - Email lookup (for password recovery)
 * - Password hashing and updates
 */
public interface AuthService {
    User register(User user);

    Optional<User> authenticate(String username, String password);

    Optional<User> authenticateAdmin(String username, String password);

    Optional<User> authenticateUser(String username, String password);

    Optional<User> findByEmail(String email);

    void updatePassword(User user, String newPassword);
}

