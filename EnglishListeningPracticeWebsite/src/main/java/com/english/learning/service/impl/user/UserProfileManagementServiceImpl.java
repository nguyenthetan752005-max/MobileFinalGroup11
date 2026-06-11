package com.english.learning.service.impl.user;

import com.english.learning.entity.User;
import com.english.learning.repository.UserRepository;
import com.english.learning.service.user.UserProfileManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserProfileManagementServiceImpl implements UserProfileManagementService {

    private final UserRepository userRepository;

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public void updateUsername(Long id, String newUsername) throws Exception {
        String normalizedUsername = newUsername == null ? "" : newUsername.trim();
        if (normalizedUsername.isEmpty()) {
            throw new Exception("Ten nguoi dung khong duoc de trong.");
        }

        Optional<User> existingUserOpt = userRepository.findByUsername(normalizedUsername);
        if (existingUserOpt.isPresent() && !existingUserOpt.get().getId().equals(id)) {
            throw new Exception("Ten nguoi dung da ton tai!");
        }
        User user = userRepository.findById(id).orElseThrow(() -> new Exception("Khong tim thay nguoi dung"));
        user.setUsername(normalizedUsername);
        userRepository.save(user);
    }

    @Override
    public void updateAvatarUrl(Long id, String avatarUrl) throws Exception {
        User user = userRepository.findById(id).orElseThrow(() -> new Exception("Khong tim thay nguoi dung"));
        user.setAvatarUrl(avatarUrl);
        userRepository.save(user);
    }

    @Override
    public void updateAvatar(Long id, String avatarUrl, String avatarPublicId) throws Exception {
        User user = userRepository.findById(id).orElseThrow(() -> new Exception("Khong tim thay nguoi dung"));
        user.setAvatarUrl(avatarUrl);
        user.setAvatarPublicId(avatarPublicId);
        userRepository.save(user);
    }

    @Override
    public void updateNotificationPreference(Long id, boolean notificationsEnabled, String timezone) throws Exception {
        User user = userRepository.findById(id).orElseThrow(() -> new Exception("Khong tim thay nguoi dung"));
        user.setNotificationsEnabled(notificationsEnabled);
        user.setNotificationTimezone(StringUtils.hasText(timezone) ? timezone.trim() : null);
        userRepository.save(user);
    }
}

