package com.english.learning.config;

import com.english.learning.entity.User;
import com.english.learning.enums.Role;
import com.english.learning.repository.UserRepository;
import org.mindrot.jbcrypt.BCrypt;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

@Component
@RequiredArgsConstructor
public class AdminDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    @Value("${app.admin.username}")
    private String adminUsername;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Override
    public void run(String... args) {
        if (userRepository.findFirstByRole(Role.ADMIN).isEmpty()) {
            User admin = new User();
            admin.setUsername(adminUsername);
            admin.setEmail(adminEmail);
            admin.setPassword(BCrypt.hashpw(adminPassword, BCrypt.gensalt()));
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);

            System.out.println("=== [AdminDataInitializer] Đã tạo admin ===");
            System.out.println("Username: " + adminUsername);
            System.out.println("Password: " + adminPassword);
        }
    }
}