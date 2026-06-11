package com.english.learning.dto.mobile;

import lombok.Data;

@Data
public class MobileRegisterRequest {
    private String username;
    private String email;
    private String password;
}
