package hcmute.edu.vn.nguyenthetan.data.remote.dto;

public class CheckDictationRequestDto {
    public long sentenceId;
    public String userInput;

    public CheckDictationRequestDto(long sentenceId, String userInput) {
        this.sentenceId = sentenceId;
        this.userInput = userInput;
    }
}
