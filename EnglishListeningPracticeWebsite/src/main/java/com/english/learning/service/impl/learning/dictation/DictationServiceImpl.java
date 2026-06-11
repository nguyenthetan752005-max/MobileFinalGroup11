// TRIỂN KHAI THUẬT TOÁN CHẤM CHÍNH TẢ THEO TỪNG TỪ.
// - So sánh từng từ người dùng gõ với đáp án trong Database.
// - BỎ QUA dấu câu (.,?!;:) khi so sánh nhưng GIỮ NGUYÊN khi hiển thị.
// - Tạo mảng gợi ý: từ đúng giữ nguyên, từ chưa đúng thành "***".
// - Đánh dấu chỉ số từ MỚI được gợi ý (newHintIndex) để Frontend tô xanh.

package com.english.learning.service.impl.learning.dictation;

import com.english.learning.dto.DictationResultDTO;
import com.english.learning.entity.Sentence;
import com.english.learning.exception.SentenceNotFoundException;
import com.english.learning.repository.SentenceRepository;
import com.english.learning.service.learning.dictation.DictationService;
import com.english.learning.util.TextNormalizerUtil;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DictationServiceImpl implements DictationService {

    private final SentenceRepository sentenceRepository;

    public DictationServiceImpl(SentenceRepository sentenceRepository) {
        this.sentenceRepository = sentenceRepository;
    }

    /**
     * THUẬT TOÁN CHÍNH: Chấm đáp án theo từng từ.
     *
     * Luật:
     * - So sánh không phân biệt hoa thường, bỏ qua dấu câu.
     * - Đếm số từ đúng liên tiếp từ đầu (matchedCount).
     * - Gợi ý: hiện (matchedCount + 1) từ gốc, ẩn phần còn lại.
     * - Từ thứ (matchedCount) là từ MỚI được gợi ý (tô xanh).
     */
    @Override
    public DictationResultDTO checkAnswer(Long sentenceId, String userInput) {
        Sentence sentence = sentenceRepository.findById(sentenceId)
                .orElseThrow(() -> new SentenceNotFoundException(sentenceId));

        String correctContent = sentence.getContent();

        // Tách từ (giữ nguyên dấu câu gốc để hiển thị)
        String[] correctWords = correctContent.trim().split("\\s+");
        String[] userWords = userInput.trim().split("\\s+");

        // Đếm số từ đúng liên tiếp (bỏ qua dấu câu khi so sánh)
        int matchedCount = 0;
        for (int i = 0; i < Math.min(correctWords.length, userWords.length); i++) {
            if (TextNormalizerUtil.removePunctuationAndLowercase(correctWords[i])
                    .equals(TextNormalizerUtil.removePunctuationAndLowercase(userWords[i]))) {
                matchedCount++;
            } else {
                break;
            }
        }

        boolean isCorrect = (matchedCount == correctWords.length);

        // Tạo mảng hint
        List<String> hintWords = new ArrayList<>();
        int newHintIndex = -1;

        if (isCorrect) {
            // Đúng hết: hiện toàn bộ câu gốc
            for (String w : correctWords) {
                hintWords.add(w);
            }
        } else {
            // Số từ hiện ra = matchedCount + 1
            int revealCount = Math.min(matchedCount + 1, correctWords.length);
            newHintIndex = matchedCount; // Từ mới gợi ý = vị trí ngay sau phần đúng

            for (int i = 0; i < correctWords.length; i++) {
                if (i < revealCount) {
                    hintWords.add(correctWords[i]); // Hiện từ gốc (giữ nguyên hoa/thường + dấu câu)
                } else {
                    hintWords.add("***");
                }
            }
        }

        return new DictationResultDTO(
                isCorrect,
                matchedCount,
                correctWords.length,
                hintWords,
                newHintIndex,
                isCorrect ? correctContent : null
        );
    }

    /**
     * Chức năng Skip: Trả về toàn bộ đáp án.
     */
    @Override
    public DictationResultDTO skipSentence(Long sentenceId) {
        Sentence sentence = sentenceRepository.findById(sentenceId)
                .orElseThrow(() -> new SentenceNotFoundException(sentenceId));

        String correctContent = sentence.getContent();
        String[] correctWords = correctContent.trim().split("\\s+");

        List<String> hintWords = new ArrayList<>();
        for (String w : correctWords) {
            hintWords.add(w);
        }

        return new DictationResultDTO(
                false,
                0,
                correctWords.length,
                hintWords,
                -1,
                correctContent
        );
    }
}
