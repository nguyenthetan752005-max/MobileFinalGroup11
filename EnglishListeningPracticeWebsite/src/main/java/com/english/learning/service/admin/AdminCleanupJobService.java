package com.english.learning.service.admin;

import com.english.learning.entity.SpeakingResult;
import com.english.learning.repository.PasswordResetTokenRepository;
import com.english.learning.repository.SpeakingResultRepository;
import com.english.learning.service.integration.media.MediaStorageGateway;
import com.english.learning.service.learning.speaking.SpeakingAudioPublicIdResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminCleanupJobService {

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final SpeakingResultRepository speakingResultRepository;
    private final MediaStorageGateway mediaStorageGateway;
    private final SpeakingAudioPublicIdResolver speakingAudioPublicIdResolver;

    // Cháº¡y vÃ o 2:00 AM má»—i ngÃ y
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanExpiredTokens() {
        log.info("Báº¯t Ä‘áº§u dá»n dáº¹p PasswordResetToken háº¿t háº¡n...");
        try {
            passwordResetTokenRepository.deleteByExpiryDateBefore(LocalDateTime.now());
            log.info("Dá»n dáº¹p PasswordResetToken hoÃ n táº¥t.");
        } catch (Exception e) {
            log.error("Lá»—i dá»n dáº¹p PasswordResetToken: " + e.getMessage());
        }
    }

    // Cháº¡y vÃ o ngÃ y 1 hÃ ng thÃ¡ng lÃºc 3:00 AM
    @Scheduled(cron = "0 0 3 1 * ?")
    @Transactional(rollbackFor = Exception.class)
    public void cleanOldSpeakingResults() {
        log.info("Báº¯t Ä‘áº§u dá»n dáº¹p SpeakingResult cÅ© hÆ¡n 6 thÃ¡ng...");
        LocalDateTime sixMonthsAgo = LocalDateTime.now().minusMonths(6);
        List<SpeakingResult> oldResults = speakingResultRepository.findByUpdatedAtBefore(sixMonthsAgo);

        int count = 0;
        for (SpeakingResult result : oldResults) {
            try {
                String audioPublicId = speakingAudioPublicIdResolver.resolveStoredPublicId(result);
                if (audioPublicId != null && !audioPublicId.isBlank()) {
                    mediaStorageGateway.deleteFile(audioPublicId);
                } else {
                    log.warn("SpeakingResult ID {} khong co storage key de xoa tren media storage.", result.getId());
                }
                speakingResultRepository.delete(result);
                count++;
            } catch (Exception e) {
                log.error("Loi xoa file media cho SpeakingResult ID {}: {}", result.getId(), e.getMessage());
                // KhÃ´ng throw exception Ä‘á»ƒ tiáº¿p tá»¥c xoÃ¡ cÃ¡c record khÃ¡c
            }
        }
        
        log.info("ÄÃ£ dá»n dáº¹p thÃ nh cÃ´ng " + count + " báº£n ghi SpeakingResult cÅ©.");
    }
}

