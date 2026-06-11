package com.english.learning.config;

import com.english.learning.security.SessionAuthStatusListener;
import com.english.learning.service.user.UserService;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SessionListenerConfig {

    @Bean
    public ServletListenerRegistrationBean<SessionAuthStatusListener> sessionAuthStatusListener(UserService userService) {
        ServletListenerRegistrationBean<SessionAuthStatusListener> registrationBean =
                new ServletListenerRegistrationBean<>();
        registrationBean.setListener(new SessionAuthStatusListener(userService));
        return registrationBean;
    }
}
