package com.english.learning.service.auth;

import com.english.learning.entity.User;

import java.util.Optional;

public interface GoogleAuthService {

    /**
     * Verify Google ID token and return user info or throw an exception if invalid.
     * Uses the email to match with existing users, or creates a new one.
     */
    Optional<User> authenticateWithGoogle(String idTokenString);
}
