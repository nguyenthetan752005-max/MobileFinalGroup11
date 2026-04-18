package hcmute.edu.vn.nguyenthetan.domain.model.lesson;

public class SpeakingAttempt {

    private final int score;
    private final String transcript;
    private final String feedback;
    private final String audioUrl;

    public SpeakingAttempt(int score, String transcript, String feedback, String audioUrl) {
        this.score = score;
        this.transcript = transcript;
        this.feedback = feedback;
        this.audioUrl = audioUrl;
    }

    public int getScore() {
        return score;
    }

    public String getTranscript() {
        return transcript;
    }

    public String getFeedback() {
        return feedback;
    }

    public String getAudioUrl() {
        return audioUrl;
    }
}
