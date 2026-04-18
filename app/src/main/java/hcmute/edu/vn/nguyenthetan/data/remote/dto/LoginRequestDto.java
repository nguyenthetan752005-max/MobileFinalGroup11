package hcmute.edu.vn.nguyenthetan.data.remote.dto;

public class LoginRequestDto {
    public String username;
    public String password;

    public LoginRequestDto(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
