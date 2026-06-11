package com.english.learning.service.impl.user;

import com.english.learning.entity.User;
import com.english.learning.exception.ResourceNotFoundException;
import com.english.learning.repository.UserRepository;
import com.english.learning.service.integration.media.MediaStorageGateway;
import com.english.learning.service.user.UserAvatarService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserAvatarServiceImpl implements UserAvatarService {

    private final UserRepository userRepository;
    private final MediaStorageGateway mediaStorageGateway;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, String> updateUserAvatar(Long userId, MultipartFile file) throws Exception {
        return updateAvatar(userId, file, "avatars/users", "user_avatar_");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, String> updateAdminAvatar(Long adminId, MultipartFile file) throws Exception {
        return updateAvatar(adminId, file, "avatars/admins", "admin_avatar_");
    }

    private Map<String, String> updateAvatar(Long userId, MultipartFile file, String folder, String prefix) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay nguoi dung"));

        String previousAvatarPublicId = user.getAvatarPublicId();

        Map<String, String> uploadResult = mediaStorageGateway.uploadFile(
                file,
                "image",
                folder,
                prefix + userId,
                true
        );

        user.setAvatarUrl(uploadResult.get("url"));
        user.setAvatarPublicId(uploadResult.get("publicId"));
        userRepository.save(user);

        String nextAvatarPublicId = uploadResult.get("publicId");
        if (previousAvatarPublicId != null
                && !previousAvatarPublicId.isBlank()
                && !previousAvatarPublicId.equals(nextAvatarPublicId)) {
            mediaStorageGateway.deleteFile(previousAvatarPublicId);
        }

        return uploadResult;
    }
}

