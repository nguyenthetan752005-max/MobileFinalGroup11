package hcmute.edu.vn.nguyenthetan.domain.usecase.lesson;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import hcmute.edu.vn.nguyenthetan.domain.model.lesson.DictationFeedback;
import hcmute.edu.vn.nguyenthetan.domain.model.lesson.Sentence;

/**
 * Unit tests for the offline {@link CheckDictationAnswerUseCase}.
 * No mocking needed — pure algorithmic logic.
 */
public class CheckDictationAnswerUseCaseTest {

    private CheckDictationAnswerUseCase useCase;

    @Before
    public void setUp() {
        useCase = new CheckDictationAnswerUseCase();
    }

    // ────── Correct answers ──────

    @Test
    public void exactMatch_isCorrect() {
        Sentence sentence = createSentence("It snowed all day");
        DictationFeedback fb = useCase.execute(sentence, "It snowed all day");
        assertTrue(fb.isCorrect());
        assertEquals("It snowed all day", fb.getFullAnswer());
    }

    @Test
    public void caseInsensitive_isCorrect() {
        Sentence sentence = createSentence("Hello World");
        DictationFeedback fb = useCase.execute(sentence, "hello world");
        assertTrue(fb.isCorrect());
    }

    @Test
    public void punctuationIgnored_isCorrect() {
        Sentence sentence = createSentence("Hello, World!");
        DictationFeedback fb = useCase.execute(sentence, "Hello World");
        assertTrue(fb.isCorrect());
    }

    // ────── Incorrect answers ──────

    @Test
    public void wrongFirstWord_noMatchedWords() {
        Sentence sentence = createSentence("It snowed all day");
        DictationFeedback fb = useCase.execute(sentence, "He snowed all day");
        assertFalse(fb.isCorrect());
        assertEquals("", fb.getCorrectWords());
        assertEquals("Incorrect. Try again.", fb.getTitle());
    }

    @Test
    public void partialMatch_returnsCorrectWordsPrefix() {
        Sentence sentence = createSentence("It snowed all day");
        DictationFeedback fb = useCase.execute(sentence, "It snowed wrong day");
        assertFalse(fb.isCorrect());
        assertEquals("It snowed", fb.getCorrectWords());
    }

    @Test
    public void emptyAnswer_isIncorrect() {
        Sentence sentence = createSentence("It snowed");
        DictationFeedback fb = useCase.execute(sentence, "");
        assertFalse(fb.isCorrect());
    }

    @Test
    public void tooFewWords_isIncorrect() {
        Sentence sentence = createSentence("It snowed all day");
        DictationFeedback fb = useCase.execute(sentence, "It snowed");
        assertFalse(fb.isCorrect());
        assertEquals("It snowed", fb.getCorrectWords());
    }

    // ────── Hint and masked words ──────

    @Test
    public void hint_revealsOneMoreWord() {
        Sentence sentence = createSentence("It snowed all day");
        DictationFeedback fb = useCase.execute(sentence, "It wrong");
        // Matched 1 word ("It"), hint reveals 1+1=2 words: "It snowed"
        assertEquals("It snowed", fb.getNewHint());
    }

    @Test
    public void maskedWords_containsAsterisks() {
        Sentence sentence = createSentence("It snowed all day");
        DictationFeedback fb = useCase.execute(sentence, "wrong");
        assertFalse(fb.getMaskedWords().isEmpty());
        assertTrue(fb.getMaskedWords().contains("*"));
    }

    @Test
    public void singleWordSentence_correctSingleWord() {
        Sentence sentence = createSentence("Hello");
        DictationFeedback fb = useCase.execute(sentence, "Hello");
        assertTrue(fb.isCorrect());
    }

    // ────── Helper ──────

    private Sentence createSentence(String content) {
        return new Sentence(1L, 0, null, null, content, null, 0L, null, null, null);
    }
}
