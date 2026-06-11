package com.english.learning.service.impl.learning.speaking;

import com.english.learning.dto.SpeakingResultDTO;
import com.english.learning.entity.Sentence;
import com.english.learning.entity.SpeakingResult;
import com.english.learning.entity.User;
import com.english.learning.enums.SpeakingResultType;
import com.english.learning.repository.SentenceRepository;
import com.english.learning.repository.SpeakingResultRepository;
import com.english.learning.repository.UserRepository;
import com.english.learning.service.learning.speaking.SpeakingEvaluation;
import com.english.learning.service.learning.speaking.SpeakingMediaService;
import com.english.learning.service.learning.speaking.SpeakingPersistenceOutcome;
import com.english.learning.service.learning.speaking.SpeakingResultStoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SpeakingResultStoreServiceImpl implements SpeakingResultStoreService {

    private final SpeakingResultRepository speakingResultRepository;
    private final UserRepository userRepository;
    private final SentenceRepository sentenceRepository;
    private final SpeakingMediaService speakingMediaService;

    @Override
    @org.springframework.transaction.annotation.Transactional
    public SpeakingPersistenceOutcome saveEvaluation(byte[] audioBytes, Long userId, Long sentenceId, SpeakingEvaluation evaluation) throws Exception {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User khong ton tai!"));
        Sentence sentence = sentenceRepository.findById(sentenceId)
                .orElseThrow(() -> new RuntimeException("Sentence khong ton tai!"));

        Map<String, String> currentUpload = speakingMediaService.uploadCurrentAudio(audioBytes, userId, sentenceId);
        String currentAudioUrl = currentUpload.get("url");
        String currentPublicId = currentUpload.get("publicId");

        SpeakingResult currentResult = speakingResultRepository
                .findByUser_IdAndSentence_IdAndResultType(userId, sentenceId, SpeakingResultType.CURRENT)
                .orElse(new SpeakingResult());

        currentResult.setUser(user);
        currentResult.setSentence(sentence);
        currentResult.setResultType(SpeakingResultType.CURRENT);
        currentResult.setScore(evaluation.score());
        currentResult.setRecognizedText(evaluation.transcribedText());
        currentResult.setFeedback(evaluation.feedback());
        currentResult.setUserAudioUrl(currentAudioUrl);
        currentResult.setUserAudioPublicId(currentPublicId);
        speakingResultRepository.save(currentResult);

        SpeakingResultDTO.BestResult bestResultDto;
        Optional<SpeakingResult> bestOpt = speakingResultRepository
                .findByUser_IdAndSentence_IdAndResultType(userId, sentenceId, SpeakingResultType.BEST);

        if (bestOpt.isPresent()) {
            SpeakingResult bestResult = bestOpt.get();
            if (evaluation.score() > bestResult.getScore()) {
                Map<String, String> bestUpload = speakingMediaService.uploadBestAudio(audioBytes, userId, sentenceId);
                String bestAudioUrl = bestUpload.get("url");
                String bestPublicId = bestUpload.get("publicId");

                bestResult.setScore(evaluation.score());
                bestResult.setRecognizedText(evaluation.transcribedText());
                bestResult.setFeedback(evaluation.feedback());
                bestResult.setUserAudioUrl(bestAudioUrl);
                bestResult.setUserAudioPublicId(bestPublicId);
                speakingResultRepository.save(bestResult);

                bestResultDto = new SpeakingResultDTO.BestResult(
                        evaluation.score(),
                        evaluation.transcribedText(),
                        evaluation.feedback(),
                        bestAudioUrl
                );
            } else {
                bestResultDto = new SpeakingResultDTO.BestResult(
                        bestResult.getScore(),
                        bestResult.getRecognizedText(),
                        bestResult.getFeedback(),
                        bestResult.getUserAudioUrl()
                );
            }
        } else {
            Map<String, String> bestUpload = speakingMediaService.uploadBestAudio(audioBytes, userId, sentenceId);
            String bestAudioUrl = bestUpload.get("url");
            String bestPublicId = bestUpload.get("publicId");

            SpeakingResult newBest = new SpeakingResult();
            newBest.setUser(user);
            newBest.setSentence(sentence);
            newBest.setResultType(SpeakingResultType.BEST);
            newBest.setScore(evaluation.score());
            newBest.setRecognizedText(evaluation.transcribedText());
            newBest.setFeedback(evaluation.feedback());
            newBest.setUserAudioUrl(bestAudioUrl);
            newBest.setUserAudioPublicId(bestPublicId);
            speakingResultRepository.save(newBest);

            bestResultDto = new SpeakingResultDTO.BestResult(
                    evaluation.score(),
                    evaluation.transcribedText(),
                    evaluation.feedback(),
                    bestAudioUrl
            );
        }

        return new SpeakingPersistenceOutcome(currentAudioUrl, bestResultDto);
    }

    @Override
    public SpeakingResultDTO getSavedResults(Long userId, Long sentenceId) {
        if (userId == null || sentenceId == null) {
            return null;
        }

        List<SpeakingResult> results = speakingResultRepository.findByUser_IdAndSentence_Id(userId, sentenceId);
        if (results.isEmpty()) {
            return null;
        }

        SpeakingResultDTO dto = new SpeakingResultDTO();
        SpeakingResult currentResult = null;
        for (SpeakingResult result : results) {
            // Lấy referenceText từ sentence.content (chung cho cả CURRENT và BEST)
            if (dto.getReferenceText() == null && result.getSentence() != null) {
                dto.setReferenceText(result.getSentence().getContent());
            }
            if (result.getResultType() == SpeakingResultType.CURRENT) {
                currentResult = result;
                dto.setScore(result.getScore());
                dto.setTranscribedText(result.getRecognizedText());
                dto.setFeedback(result.getFeedback());
                dto.setAudioUrl(result.getUserAudioUrl());
            } else if (result.getResultType() == SpeakingResultType.BEST) {
                dto.setBestResult(new SpeakingResultDTO.BestResult(
                        result.getScore(),
                        result.getRecognizedText(),
                        result.getFeedback(),
                        result.getUserAudioUrl()
                ));
            }
        }

        // Nếu chưa có BEST nhưng có CURRENT → dùng CURRENT làm BEST
        if (dto.getBestResult() == null && currentResult != null) {
            dto.setBestResult(new SpeakingResultDTO.BestResult(
                    currentResult.getScore(),
                    currentResult.getRecognizedText(),
                    currentResult.getFeedback(),
                    currentResult.getUserAudioUrl()
            ));
        }

        return dto;
    }
}

