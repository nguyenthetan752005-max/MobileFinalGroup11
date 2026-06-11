package com.english.learning.service.content.sentence;

import com.english.learning.entity.Sentence;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface SentenceQueryService {
    List<Sentence> getSentencesByLessonId(Long lessonId);

    List<Sentence> getPublishedSentencesByLessonId(Long lessonId);

    Optional<Sentence> getSentenceById(Long id);

    Optional<Sentence> getPublishedSentenceById(Long id);

    Map<Long, List<String>> getProperNounHints(Long lessonId);
}

