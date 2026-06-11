package com.english.learning.service.content.sentence;

import com.english.learning.dto.AdminSentenceRequest;
import com.english.learning.entity.Sentence;

public interface SentenceAdminService {
    Sentence createSentence(AdminSentenceRequest request);

    Sentence updateSentence(Long id, AdminSentenceRequest request);

    void softDeleteSentence(Long id);

    void restoreSentence(Long id);

    void hardDeleteSentence(Long id) throws Exception;
}

