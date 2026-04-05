package hcmute.edu.vn.nguyenthetan.domain.model.lesson;

public class Sentence {

    private final long id;
    private final int orderIndex;
    private final String content;
    private final String hintText;
    private final long durationMillis;

    public Sentence(long id, int orderIndex, String content, String hintText, long durationMillis) {
        this.id = id;
        this.orderIndex = orderIndex;
        this.content = content;
        this.hintText = hintText;
        this.durationMillis = durationMillis;
    }

    public long getId() {
        return id;
    }

    public int getOrderIndex() {
        return orderIndex;
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
}
