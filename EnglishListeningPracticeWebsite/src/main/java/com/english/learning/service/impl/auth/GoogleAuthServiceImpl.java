package com.english.learning.service.impl.auth;

import com.english.learning.entity.User;
import com.english.learning.enums.Role;
import com.english.learning.repository.UserRepository;
import com.english.learning.service.auth.GoogleAuthService;
import com.english.learning.util.CachedGoogleTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GoogleAuthServiceImpl implements GoogleAuthService {

    private final UserRepository userRepository;
    private final CachedGoogleTokenVerifier cachedGoogleTokenVerifier;

    @Value("${spring.security.oauth2.client.registration.google.client-id:}")
    private String googleClientId;

    @Override
    public Optional<User> authenticateWithGoogle(String idTokenString) {
        try {
            if (googleClientId == null || googleClientId.isEmpty()) {
                throw new RuntimeException("Chưa cấu hình Google Client ID trên máy chủ.");
            }

            GoogleIdToken idToken = cachedGoogleTokenVerifier.verify(idTokenString, googleClientId);
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                
                String email = payload.getEmail();
                String subjectId = payload.getSubject();
                String name = (String) payload.get("name");
                String pictureUrl = (String) payload.get("picture");

                Optional<User> existingUserOpt = userRepository.findByEmail(email);
                
                if (existingUserOpt.isPresent()) {
                    User user = existingUserOpt.get();
                    // Update provider info if needed
                    if (user.getProvider() == null || !user.getProvider().equals("GOOGLE")) {
                        user.setProvider("GOOGLE");
                        user.setProviderId(subjectId);
                    }
                    if (user.getAvatarUrl() == null && pictureUrl != null) {
                        user.setAvatarUrl(pictureUrl);
                    }
                    userRepository.save(user);
                    return Optional.of(user);
                } else {
                    // Create new user
                    User newUser = new User();
                    newUser.setEmail(email);
                    
                    // Generate a unique username from email prefix or assign random
                    String baseUsername = email.split("@")[0].replaceAll("[^a-zA-Z0-9]", "");
                    if (baseUsername.isEmpty()) {
                        baseUsername = "user";
                    }
                    String username = baseUsername;
                    int counter = 1;
                    while (userRepository.findByUsername(username).isPresent()) {
                        username = baseUsername + counter++;
                    }
                    newUser.setUsername(username);
                    
                    // Generate random password as they log in with Google
                    String randomPassword = UUID.randomUUID().toString();
                    newUser.setPassword(BCrypt.hashpw(randomPassword, BCrypt.gensalt()));
                    
                    newUser.setRole(Role.USER);
                    newUser.setProvider("GOOGLE");
                    newUser.setProviderId(subjectId);
                    newUser.setIsActive(true);  // Đảm bảo user Google được active
                    newUser.setIsDeleted(false);
                    if (pictureUrl != null) {
                        newUser.setAvatarUrl(pictureUrl);
                    }

                    User savedUser = userRepository.save(newUser);
                    return Optional.of(savedUser);
                }
            } else {
                throw new RuntimeException("Google token không hợp lệ.");
            }
        } catch (Exception e) {
             throw new RuntimeException("Lỗi xác thực Google: " + e.getMessage());
        }
    }
}
