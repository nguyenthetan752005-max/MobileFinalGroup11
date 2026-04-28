package hcmute.edu.vn.nguyenthetan.ui.lesson;

import hcmute.edu.vn.nguyenthetan.domain.model.lesson.SentenceStatus;

/**
 * UI-side row model for the lesson transcript list. Lifted out of
 * {@code TranscriptAdapter} so the ViewModel can produce these without
 * pulling in adapter/view-layer types.
 */
public final class LessonTranscriptRow {

    public final int order;
    public final String sentence;
    public final SentenceStatus status;
    public final boolean selected;
    public final boolean playing;

    public LessonTranscriptRow(int order, String sentence, SentenceStatus status, boolean selected, boolean playing) {
        this.order = order;
        this.sentence = sentence;
        this.status = status;
        this.selected = selected;
        this.playing = playing;
    }
}
