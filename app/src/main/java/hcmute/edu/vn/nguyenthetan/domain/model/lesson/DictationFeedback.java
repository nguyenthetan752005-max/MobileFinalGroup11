package hcmute.edu.vn.nguyenthetan.domain.model.lesson;

public class DictationFeedback {

    private final boolean correct;
    private final String title;
    private final String fullAnswer;
    private final String correctWords;
    private final String newHint;
    private final String maskedWords;

    public DictationFeedback(
            boolean correct,
            String title,
            String fullAnswer,
            String correctWords,
            String newHint,
            String maskedWords
    ) {
        this.correct = correct;
        this.title = title;
        this.fullAnswer = fullAnswer;
        this.correctWords = correctWords;
        this.newHint = newHint;
        this.maskedWords = maskedWords;
    }

    public boolean isCorrect() {
        return correct;
    }

    public String getTitle() {
        return title;
    }

    public String getFullAnswer() {
        return fullAnswer;
    }

    public String getCorrectWords() {
        return correctWords;
    }

    public String getNewHint() {
        return newHint;
    }

    public String getMaskedWords() {
        return maskedWords;
    }
}
