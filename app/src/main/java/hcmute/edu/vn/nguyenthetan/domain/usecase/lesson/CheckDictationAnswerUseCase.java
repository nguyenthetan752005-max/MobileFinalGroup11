package hcmute.edu.vn.nguyenthetan.domain.usecase.lesson;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import hcmute.edu.vn.nguyenthetan.domain.model.lesson.DictationFeedback;
import hcmute.edu.vn.nguyenthetan.domain.model.lesson.Sentence;

public class CheckDictationAnswerUseCase {

    public DictationFeedback execute(Sentence sentence, String userAnswer) {
        String[] expectedWords = splitPreservingCase(sentence.getContent());
        String[] answerWords = splitPreservingCase(userAnswer);

        int matchedPrefixCount = 0;
        int maxWords = Math.min(expectedWords.length, answerWords.length);
        while (matchedPrefixCount < maxWords
                && normalizeWord(expectedWords[matchedPrefixCount]).equals(normalizeWord(answerWords[matchedPrefixCount]))) {
            matchedPrefixCount++;
        }

        boolean correct = matchedPrefixCount == expectedWords.length;

        if (correct) {
            return new DictationFeedback(
                    true,
                    "Correct! Well done.",
                    sentence.getContent(),
                    sentence.getContent(),
                    "",
                    ""
            );
        }

        int revealCount = Math.min(matchedPrefixCount + 1, expectedWords.length);
        String visibleHint = joinWords(expectedWords, 0, revealCount);

        return new DictationFeedback(
                false,
                "Incorrect. Try again.",
                sentence.getContent(),
                joinWords(expectedWords, 0, matchedPrefixCount),
                visibleHint,
                buildMaskedWords(expectedWords, revealCount)
        );
    }

    private String[] splitPreservingCase(String value) {
        return value == null || value.trim().isEmpty() ? new String[0] : value.trim().split("\\s+");
    }

    private String normalizeWord(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("[.,?!;:'\"-]", "")
                .toLowerCase(Locale.US)
                .trim();
    }

    private String joinWords(String[] words, int start, int end) {
        StringBuilder builder = new StringBuilder();
        for (int index = start; index < end && index < words.length; index++) {
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(words[index]);
        }
        return builder.toString();
    }

    private String buildMaskedWords(String[] words, int revealedCount) {
        List<String> masked = new ArrayList<>();
        for (int index = revealedCount; index < words.length; index++) {
            masked.add(maskWord(words[index]));
        }

        StringBuilder builder = new StringBuilder();
        for (String word : masked) {
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(word);
        }
        return builder.toString();
    }

    private String maskWord(String word) {
        int visibleLength = Math.max(3, Math.min(word.length(), 8));
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < visibleLength; index++) {
            builder.append('*');
        }
        return builder.toString();
    }
}
