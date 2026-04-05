package hcmute.edu.vn.nguyenthetan.domain.model.profile;

public class MoodState {

    private final String label;
    private final String accentKey;

    public MoodState(String label, String accentKey) {
        this.label = label;
        this.accentKey = accentKey;
    }

    public String getLabel() {
        return label;
    }

    public String getAccentKey() {
        return accentKey;
    }
}
