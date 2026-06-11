package com.english.learning.service.impl.user;

import com.english.learning.entity.SpeakingResult;
import com.english.learning.entity.User;
import com.english.learning.repository.SpeakingResultRepository;
import com.english.learning.repository.UserRepository;
import com.english.learning.service.integration.media.MediaStorageGateway;
import com.english.learning.service.learning.speaking.SpeakingAudioPublicIdResolver;
import com.english.learning.service.user.UserLifecycleManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UserLifecycleManagementServiceImpl implements UserLifecycleManagementService {

    private static final Pattern DELETED_SUFFIX_PATTERN = Pattern.compile("_deleted_\\d+$");

    private final UserRepository userRepository;
    private final SpeakingResultRepository speakingResultRepository;
    private final MediaStorageGateway mediaStorageGateway;
    private final SpeakingAudioPublicIdResolver speakingAudioPublicIdResolver;

    @Override
    public void softDeleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new com.english.learning.exception.ResourceNotFoundException("Nguoi dung khong ton tai"));

        String modifier = "_deleted_" + System.currentTimeMillis();
        user.setEmail(user.getEmail() + modifier);
        user.setUsername(user.getUsername() + modifier);
        user.setIsDeleted(true);
        user.setIsActive(false);
        userRepository.save(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void hardDeleteUser(Long id) throws Exception {
        User user = userRepository.findAnyUserById(id)
                .orElseThrow(() -> new com.english.learning.exception.ResourceNotFoundException("Nguoi dung khong ton tai"));

        for (SpeakingResult speakingResult : speakingResultRepository.findByUser_Id(id)) {
            String audioPublicId = speakingAudioPublicIdResolver.resolveStoredPublicId(speakingResult);
            if (audioPublicId != null && !audioPublicId.isBlank()) {
                mediaStorageGateway.deleteFile(audioPublicId);
            }
        }
        if (user.getAvatarPublicId() != null && !user.getAvatarPublicId().isBlank()) {
            mediaStorageGateway.deleteFile(user.getAvatarPublicId());
        }

        userRepository.delete(user);
    }

    @Override
    public void restoreUser(Long id) {
        User user = userRepository.findAnyUserById(id)
                .orElseThrow(() -> new com.english.learning.exception.ResourceNotFoundException("Nguoi dung khong ton tai"));

        String restoredUsername = stripDeletedSuffix(user.getUsername());
        String restoredEmail = stripDeletedSuffix(user.getEmail());

        userRepository.findByUsername(restoredUsername)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new RuntimeException("Username goc da duoc su dung, khong the khoi phuc tu dong.");
                });

        userRepository.findByEmail(restoredEmail)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new RuntimeException("Email goc da duoc su dung, khong the khoi phuc tu dong.");
                });

        user.setUsername(restoredUsername);
        user.setEmail(restoredEmail);
        user.setIsDeleted(false);
        user.setIsActive(false);
        userRepository.save(user);
    }

    private String stripDeletedSuffix(String value) {
        if (value == null) {
            return null;
        }
        return DELETED_SUFFIX_PATTERN.matcher(value).replaceFirst("");
    }
}

