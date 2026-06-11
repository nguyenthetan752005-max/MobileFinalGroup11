package com.english.learning.service.progress;

import com.english.learning.entity.UserProgress;

public interface UserProgressMutationService {
    UserProgress updateProgress(Long userId, Long sentenceId);

    UserProgress completeSentence(Long userId, Long sentenceId);

    UserProgress skipSentence(Long userId, Long sentenceId);
}

