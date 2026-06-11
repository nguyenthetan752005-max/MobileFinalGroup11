package com.english.learning.service.impl.content.sentence;

import com.english.learning.entity.Sentence;
import com.english.learning.enums.ContentStatus;
import com.english.learning.repository.SentenceRepository;
import com.english.learning.service.learning.hint.HintService;
import com.english.learning.service.content.sentence.SentenceQueryService;
import com.english.learning.util.TextNormalizerUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SentenceQueryServiceImpl implements SentenceQueryService {

    private final SentenceRepository sentenceRepository;
    private final HintService hintService;

    @Override
    public List<Sentence> getSentencesByLessonId(Long lessonId) {
        List<Sentence> sentences = sentenceRepository.findByLesson_IdOrderByOrderIndex(lessonId);
        hydrateSentences(sentences);
        return sentences;
    }

    @Override
    public List<Sentence> getPublishedSentencesByLessonId(Long lessonId) {
        List<Sentence> sentences =
                sentenceRepository.findByLesson_IdAndStatusOrderByOrderIndexAsc(lessonId, ContentStatus.PUBLISHED);
        hydrateSentences(sentences);
        return sentences;
    }

    @Override
    public Optional<Sentence> getSentenceById(Long id) {
        return sentenceRepository.findById(id);
    }

    @Override
    public Optional<Sentence> getPublishedSentenceById(Long id) {
        return sentenceRepository.findPublishedById(id, ContentStatus.PUBLISHED);
    }

    @Override
    public Map<Long, List<String>> getProperNounHints(Long lessonId) {
        List<Sentence> sentences =
                sentenceRepository.findByLesson_IdAndStatusOrderByOrderIndexAsc(lessonId, ContentStatus.PUBLISHED);
        return hintService.getHintsMap(sentences);
    }

    private void hydrateSentences(List<Sentence> sentences) {
        for (Sentence sentence : sentences) {
            String cleanText = TextNormalizerUtil.cleanHtmlAndBrackets(sentence.getContent());
            sentence.setContent(cleanText);
            sentence.setProperNouns(hintService.extractProperNouns(cleanText));
        }
    }
}

