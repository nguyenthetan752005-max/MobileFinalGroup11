package hcmute.edu.vn.nguyenthetan.data.remote.dto;

public class RegisterRequestDto {
    public String username;
    public String email;
    public String password;

    public RegisterRequestDto(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }
}
