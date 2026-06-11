package com.english.learning.service.impl.user;

import com.english.learning.entity.User;
import com.english.learning.enums.Role;
import com.english.learning.repository.UserRepository;
import com.english.learning.service.auth.AuthService;
import com.english.learning.service.user.UserAdministrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserAdministrationServiceImpl implements UserAdministrationService {

    private final UserRepository userRepository;
    private final AuthService authService;

    @Override
    public void adminUpdateBasicInfo(Long id, String username, String avatarUrl, boolean isActive, Role role) throws Exception {
        User user = userRepository.findById(id).orElseThrow(() -> new Exception("Khong tim thay nguoi dung"));
        String normalizedUsername = username == null ? "" : username.trim();
        if (normalizedUsername.isEmpty()) {
            throw new Exception("Ten nguoi dung khong duoc de trong.");
        }

        Optional<User> existingUserOpt = userRepository.findByUsername(normalizedUsername);
        if (existingUserOpt.isPresent() && !existingUserOpt.get().getId().equals(id)) {
            throw new Exception("Ten nguoi dung da ton tai!");
        }

        user.setUsername(normalizedUsername);
        user.setAvatarUrl(normalizeBlank(avatarUrl));
        user.setIsActive(isActive);
        user.setRole(role != null ? role : Role.USER);
        userRepository.save(user);
    }

    @Override
    public void adminUpdatePassword(Long id, String newPassword) throws Exception {
        if (newPassword == null || newPassword.trim().length() < 6) {
            throw new Exception("Mat khau phai co it nhat 6 ky tu.");
        }
        User user = userRepository.findById(id).orElseThrow(() -> new Exception("Khong tim thay nguoi dung"));
        authService.updatePassword(user, newPassword.trim());
    }

    @Override
    public void updateActiveStatus(Long id, boolean isActive) {
        userRepository.findById(id).ifPresent(user -> {
            user.setIsActive(isActive);
            userRepository.save(user);
        });
    }

    @Override
    @Transactional
    public void resetAllActiveStatuses() {
        userRepository.resetAllActiveStatuses();
    }

    private String normalizeBlank(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}

