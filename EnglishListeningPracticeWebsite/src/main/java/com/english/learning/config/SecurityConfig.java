package com.english.learning.config;

import com.english.learning.security.CustomOAuth2SuccessHandler;
import com.english.learning.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2SuccessHandler successHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints (không cần JWT)
                .requestMatchers("/api/health").permitAll()
                .requestMatchers("/api/mobile/auth/**").permitAll()
                .requestMatchers("/api/mobile/catalog/**").permitAll()
                .requestMatchers("/api/mobile/categories").permitAll()
                .requestMatchers("/api/mobile/categories/*/sections").permitAll()
                .requestMatchers("/api/mobile/app-settings/reminder").permitAll()
                .requestMatchers("/api/mobile/leaderboard").permitAll()
                .requestMatchers("/api/mobile/lessons/*").permitAll()  // Cho phép xem lesson không cần login
                .requestMatchers("/api/mobile/sections/*").permitAll() // Cho phép xem section không cần login
                .requestMatchers("/login").permitAll()
                .requestMatchers("/oauth2/**").permitAll()
                .requestMatchers("/web/**").permitAll()
                .requestMatchers("/").permitAll()
                .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                // Admin endpoints (cần ADMIN role)
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                // Mobile authenticated endpoints (cần JWT)
                .requestMatchers("/api/mobile/media/**").permitAll() // Cho phép lấy audio
                .requestMatchers("/api/mobile/sentences/*/comments").permitAll() // Cho phép guest xem comments
                .requestMatchers("/api/mobile/comments/*/replies").permitAll() // Cho phép guest xem replies
                .requestMatchers("/api/mobile/**").authenticated()
                // Mặc định permit all cho web
                .anyRequest().permitAll()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .successHandler(successHandler)
            )
            .formLogin(form -> form.disable())
            .logout(logout -> logout.disable())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
