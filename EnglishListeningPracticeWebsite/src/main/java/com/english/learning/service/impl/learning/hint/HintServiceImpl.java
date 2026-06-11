package com.english.learning.service.impl.learning.hint;

import com.english.learning.entity.Sentence;
import com.english.learning.service.learning.hint.HintService;
import com.english.learning.util.TextNormalizerUtil;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class HintServiceImpl implements HintService {

    private static final Set<String> EXCLUDED_WORDS = Set.of(
            "i", "me", "my", "mine", "myself",
            "we", "us", "our", "ours", "ourselves",
            "you", "your", "yours", "yourself", "yourselves",
            "he", "him", "his", "himself",
            "she", "her", "hers", "herself",
            "it", "its", "itself",
            "they", "them", "their", "theirs", "themselves"
    );

    @Override
    public List<String> extractProperNouns(String content) {
        List<String> properNouns = new ArrayList<>();
        if (content == null || content.isBlank()) return properNouns;

        String[] words = content.trim().split("\\s+");
        if (words.length <= 1) return properNouns;

        for (int i = 1; i < words.length; i++) {
            // Náº¿u tá»« trÆ°á»›c káº¿t thÃºc báº±ng . ? ! thÃ¬ tá»« hiá»‡n táº¡i lÃ  Ä‘áº§u cÃ¢u má»›i â†’ bá» qua
            String prevWord = words[i - 1];
            if (prevWord.isEmpty()) continue;
            
            char lastChar = prevWord.charAt(prevWord.length() - 1);
            if (lastChar == '.' || lastChar == '?' || lastChar == '!') {
                continue;
            }

            // Kiá»ƒm tra kÃ½ tá»± Ä‘áº§u cÃ³ viáº¿t hoa khÃ´ng
            String currentWord = words[i];
            if (currentWord.isEmpty()) continue;
            
            char firstChar = currentWord.charAt(0);
            if (Character.isUpperCase(firstChar)) {
                // Loáº¡i bá» dáº¥u cÃ¢u Ä‘i kÃ¨m (vÃ­ dá»¥: "Smith," â†’ "Smith") báº±ng TextNormalizerUtil
                String cleanWord = TextNormalizerUtil.keepLettersAndHyphens(currentWord);
                if (!cleanWord.isEmpty() && !EXCLUDED_WORDS.contains(cleanWord.toLowerCase())) {
                    properNouns.add(cleanWord);
                }
            }
        }
        return properNouns;
    }

    @Override
    public Map<Long, List<String>> getHintsMap(List<Sentence> sentences) {
        return sentences.stream()
                .filter(s -> {
                    // Äáº£m báº£o properNouns Ä‘Æ°á»£c trÃ­ch xuáº¥t náº¿u chÆ°a cÃ³
                    if (s.getProperNouns() == null || s.getProperNouns().isEmpty()) {
                        s.setProperNouns(extractProperNouns(s.getContent()));
                    }
                    return s.getProperNouns() != null && !s.getProperNouns().isEmpty();
                })
                .collect(Collectors.toMap(Sentence::getId, Sentence::getProperNouns));
    }
}

