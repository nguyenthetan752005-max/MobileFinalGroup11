package com.english.learning.service.impl.content.sentence;

import com.english.learning.dto.AdminSentenceRequest;
import com.english.learning.entity.Sentence;
import com.english.learning.service.content.sentence.SentenceAdminService;
import com.english.learning.service.content.sentence.SentenceQueryService;
import com.english.learning.service.content.sentence.SentenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SentenceServiceImpl implements SentenceService {

    private final SentenceQueryService sentenceQueryService;
    private final SentenceAdminService sentenceAdminService;

    @Override
    public List<Sentence> getSentencesByLessonId(Long lessonId) {
        return sentenceQueryService.getSentencesByLessonId(lessonId);
    }

    @Override
    public List<Sentence> getPublishedSentencesByLessonId(Long lessonId) {
        return sentenceQueryService.getPublishedSentencesByLessonId(lessonId);
    }

    @Override
    public Optional<Sentence> getSentenceById(Long id) {
        return sentenceQueryService.getSentenceById(id);
    }

    @Override
    public Optional<Sentence> getPublishedSentenceById(Long id) {
        return sentenceQueryService.getPublishedSentenceById(id);
    }

    @Override
    public Map<Long, List<String>> getProperNounHints(Long lessonId) {
        return sentenceQueryService.getProperNounHints(lessonId);
    }

    @Override
    public Sentence createSentence(AdminSentenceRequest request) {
        return sentenceAdminService.createSentence(request);
    }

    @Override
    public Sentence updateSentence(Long id, AdminSentenceRequest request) {
        return sentenceAdminService.updateSentence(id, request);
    }

    @Override
    public void softDeleteSentence(Long id) {
        sentenceAdminService.softDeleteSentence(id);
    }

    @Override
    public void restoreSentence(Long id) {
        sentenceAdminService.restoreSentence(id);
    }

    @Override
    public void hardDeleteSentence(Long id) throws Exception {
        sentenceAdminService.hardDeleteSentence(id);
    }
}

