package com.english.learning.config;

import com.english.learning.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ActiveStatusInitializer {

    private final UserRepository userRepository;

    @Bean
    public ApplicationRunner resetOnlinePresenceOnStartup() {
        return args -> {
            // Sửa lỗi dữ liệu: phiên bản cũ đã SET tất cả users thành is_active=false.
            // Khôi phục lại cho những user không bị Admin tạm khóa (suspension_reason IS NULL).
            userRepository.repairWronglyDeactivatedUsers();
            // Reset trạng thái online (lastActiveAt) khi server khởi động,
            // KHÔNG reset is_active vì đó là trạng thái tài khoản do Admin quản lý.
            userRepository.resetAllOnlinePresence();
        };
    }
}
