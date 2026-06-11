package com.english.learning.service.learning.hint;

import com.english.learning.entity.Sentence;
import java.util.List;
import java.util.Map;

public interface HintService {
    /**
     * TrÃ­ch xuáº¥t danh tá»« riÃªng tá»« ná»™i dung cÃ¢u.
     */
    List<String> extractProperNouns(String content);

    /**
     * Táº¡o Map chá»©a hints cho danh sÃ¡ch cÃ¡c cÃ¢u.
     */
    Map<Long, List<String>> getHintsMap(List<Sentence> sentences);
}

