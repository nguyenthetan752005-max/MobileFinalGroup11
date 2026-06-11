// INTERFACE Dá»ŠCH Vá»¤ CHáº¤M CHÃNH Táº¢.
// - Äá»‹nh nghÄ©a há»£p Ä‘á»“ng (contract) cho DictationServiceImpl.
// - TuÃ¢n thá»§ nguyÃªn lÃ½ D trong SOLID (Dependency Inversion Principle).

package com.english.learning.service.learning.dictation;

import com.english.learning.dto.DictationResultDTO;

public interface DictationService {

    /**
     * Cháº¥m Ä‘Ã¡p Ã¡n chÃ©p chÃ­nh táº£ theo tá»«ng tá»«.
     * 
     * @param sentenceId ID cá»§a cÃ¢u trong Database
     * @param userInput  Ná»™i dung ngÆ°á»i dÃ¹ng gÃµ vÃ o
     * @return DTO chá»©a káº¿t quáº£ (hint, matchedCount, isCorrect...)
     */
    DictationResultDTO checkAnswer(Long sentenceId, String userInput);

    /**
     * Láº¥y Ä‘Ã¡p Ã¡n Ä‘áº§y Ä‘á»§ (dÃ¹ng cho chá»©c nÄƒng Skip).
     * 
     * @param sentenceId ID cá»§a cÃ¢u trong Database
     * @return DTO chá»©a cÃ¢u gá»‘c Ä‘áº§y Ä‘á»§
     */
    DictationResultDTO skipSentence(Long sentenceId);
}

