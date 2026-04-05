package hcmute.edu.vn.nguyenthetan.domain.usecase.lesson;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import hcmute.edu.vn.nguyenthetan.domain.model.lesson.DictationFeedback;
import hcmute.edu.vn.nguyenthetan.domain.model.lesson.Sentence;

public class CheckDictationAnswerUseCase {

    public DictationFeedback execute(Sentence sentence, String userAnswer) {
        String[] expectedWords = splitPreservingCase(sentence.getContent());
        String[] normalizedExpected = normalize(sentence.getContent());
        String[] normalizedAnswer = normalize(userAnswer);

        int matchedPrefixCount = 0;
        int maxWords = Math.min(normalizedExpected.length, normalizedAnswer.length);
        while (matchedPrefixCount < maxWords
                && normalizedExpected[matchedPrefixCount].equals(normalizedAnswer[matchedPrefixCount])) {
            matchedPrefixCount++;
        }

        boolean exactMatch = normalizedExpected.length == normalizedAnswer.length
                && matchedPrefixCount == normalizedExpected.length;

        if (exactMatch) {
            return new DictationFeedback(
                    true,
                    "Correct! Well done.",
                    sentence.getContent(),
                    sentence.getContent(),
                    "",
                    ""
            );
        }

        String hint = sentence.getHintText();
        if ((hint == null || hint.isEmpty()) && matchedPrefixCount < expectedWords.length) {
            hint = expectedWords[matchedPrefixCount];
        }

        return new DictationFeedback(
                false,
                "Almost there. Use the hint and try again.",
                sentence.getContent(),
                joinWords(expectedWords, 0, matchedPrefixCount),
                hint == null ? "" : hint,
                buildMaskedWords(expectedWords, matchedPrefixCount, hint)
        );
    }

    private String[] splitPreservingCase(String value) {
        return value == null || value.trim().isEmpty() ? new String[0] : value.trim().split("\\s+");
    }

    private String[] normalize(String value) {
        String sanitized = value == null ? "" : value
                .toLowerCase(Locale.US)
                .replaceAll("[^a-z0-9' ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
        return sanitized.isEmpty() ? new String[0] : sanitized.split(" ");
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

    private String buildMaskedWords(String[] words, int matchedPrefixCount, String hint) {
        Set<String> hintWords = new HashSet<>();
        for (String normalizedHintWord : normalize(hint)) {
            hintWords.add(normalizedHintWord);
        }

        List<String> masked = new ArrayList<>();
        for (int index = matchedPrefixCount; index < words.length; index++) {
            String[] normalized = normalize(words[index]);
            if (normalized.length > 0 && hintWords.contains(normalized[0])) {
                continue;
            }
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
