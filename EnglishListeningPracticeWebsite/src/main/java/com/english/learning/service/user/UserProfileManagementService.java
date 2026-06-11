package com.english.learning.service.user;

import com.english.learning.entity.User;

import java.util.Optional;

public interface UserProfileManagementService {
    Optional<User> findById(Long id);

    void updateUsername(Long id, String newUsername) throws Exception;

    void updateAvatarUrl(Long id, String avatarUrl) throws Exception;

    void updateAvatar(Long id, String avatarUrl, String avatarPublicId) throws Exception;

    void updateNotificationPreference(Long id, boolean notificationsEnabled, String timezone) throws Exception;
}

