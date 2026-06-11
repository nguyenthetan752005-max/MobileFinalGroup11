package com.english.learning.service.impl.auth;

import com.english.learning.entity.User;
import com.english.learning.enums.Role;
import com.english.learning.repository.UserRepository;
import com.english.learning.service.auth.AuthService;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * SRP: Authentication Service Implementation (OOSE / SOLID).
 *
 * Single Responsibility: This class handles ONLY authentication logic.
 * - Registration (email/username uniqueness check + password hashing)
 * - Login (BCrypt verification with legacy plain-text fallback)
 * - Role-based authentication filtering (Admin vs User)
 * - Password updates (hashing before persistence)
 *
 * It does NOT handle profile management, admin moderation, or user deletion.
 * Those concerns belong to UserManagementService (UserServiceImpl).
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;

    @Override
    public User register(User user) {
        // Kiá»ƒm tra email Ä‘Ã£ tá»“n táº¡i
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email Ä‘Ã£ tá»“n táº¡i!");
        }
        // Kiá»ƒm tra username Ä‘Ã£ tá»“n táº¡i
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("Username Ä‘Ã£ tá»“n táº¡i!");
        }

        // Hash password trÆ°á»›c khi lÆ°u
        String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
        user.setPassword(hashedPassword);

        return userRepository.save(user);
    }

    @Override
    public Optional<User> authenticate(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByEmail(username);
        }

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String storedPassword = user.getPassword();

            // Há»— trá»£ cáº£ tÃ i khoáº£n cÅ© chÆ°a mÃ£ hÃ³a máº­t kháº©u
            if (storedPassword.startsWith("$2a$")) {
                if (BCrypt.checkpw(password, storedPassword)) {
                    return Optional.of(user);
                }
            } else {
                if (storedPassword.equals(password)) {
                    return Optional.of(user);
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> authenticateAdmin(String username, String password) {
        Optional<User> userOpt = authenticate(username, password);
        if (userOpt.isPresent() && Role.ADMIN.equals(userOpt.get().getRole())) {
            return userOpt;
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> authenticateUser(String username, String password) {
        Optional<User> userOpt = authenticate(username, password);
        if (userOpt.isPresent() && Role.USER.equals(userOpt.get().getRole())) {
            return userOpt;
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public void updatePassword(User user, String newPassword) {
        String hashedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        user.setPassword(hashedPassword);
        userRepository.save(user);
    }
}

