package com.english.learning.service.user;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface UserAvatarService {
    Map<String, String> updateUserAvatar(Long userId, MultipartFile file) throws Exception;

    Map<String, String> updateAdminAvatar(Long adminId, MultipartFile file) throws Exception;
}

