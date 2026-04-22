package hcmute.edu.vn.nguyenthetan.domain.model.lesson;

public class Sentence {

    private final long id;
    private final int orderIndex;
    private final String audioUrl;
    private final String localAudioPath;
    private final String content;
    private final String hintText;
    private final long durationMillis;
    private final Double startTime;
    private final Double endTime;
    private final java.util.List<String> properNouns;

    public Sentence(
            long id,
            int orderIndex,
            String audioUrl,
            String localAudioPath,
            String content,
            String hintText,
            long durationMillis,
            Double startTime,
            Double endTime,
            java.util.List<String> properNouns
    ) {
        this.id = id;
        this.orderIndex = orderIndex;
        this.audioUrl = audioUrl;
        this.localAudioPath = localAudioPath;
        this.content = content;
        this.hintText = hintText;
        this.durationMillis = durationMillis;
        this.startTime = startTime;
        this.endTime = endTime;
        this.properNouns = properNouns;
    }

    public long getId() {
        return id;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public String getLocalAudioPath() {
        return localAudioPath;
    }

    public String getContent() {
        return content;
    }

    public String getHintText() {
        return hintText;
    }

    public long getDurationMillis() {
        return durationMillis;
    }

    public Double getStartTime() {
        return startTime;
    }

    public Double getEndTime() {
        return endTime;
    }

    public java.util.List<String> getProperNouns() {
        return properNouns;
    }
}
