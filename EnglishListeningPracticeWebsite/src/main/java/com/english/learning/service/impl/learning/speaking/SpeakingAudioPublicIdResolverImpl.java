package com.english.learning.service.impl.learning.speaking;

import com.english.learning.entity.SpeakingResult;
import com.english.learning.service.learning.speaking.SpeakingAudioPublicIdResolver;
import org.springframework.stereotype.Service;

@Service
public class SpeakingAudioPublicIdResolverImpl implements SpeakingAudioPublicIdResolver {

    @Override
    public String resolveStoredPublicId(SpeakingResult result) {
        if (result == null) {
            return null;
        }
        if (result.getUserAudioPublicId() != null && !result.getUserAudioPublicId().isBlank()) {
            return result.getUserAudioPublicId();
        }
        if (result.getUser() == null || result.getSentence() == null || result.getResultType() == null) {
            return null;
        }
        return "speaking_audio/user_" + result.getUser().getId()
                + "_sentence_" + result.getSentence().getId()
                + "_" + result.getResultType().name().toLowerCase();
    }
}
