package com.english.learning.service.learning.speaking;

import com.english.learning.entity.SpeakingResult;

public interface SpeakingAudioPublicIdResolver {
    String resolveStoredPublicId(SpeakingResult result);
}
