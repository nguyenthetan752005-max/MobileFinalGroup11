package com.english.learning.service.impl.auth;

import com.english.learning.entity.PasswordResetToken;
import com.english.learning.entity.User;
import com.english.learning.repository.PasswordResetTokenRepository;
import com.english.learning.service.auth.AuthService;
import com.english.learning.service.auth.PasswordResetService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

    private static final int TOKEN_EXPIRY_MINUTES = 30;

    private final PasswordResetTokenRepository tokenRepository;
    private final AuthService authService;
    private final JavaMailSender mailSender;
    private final com.english.learning.service.auth.TokenBlacklistService tokenBlacklistService;
    private final TemplateEngine templateEngine;

    @Value("${app.url}")
    private String appUrl;

    @Value("${app.external.url:${app.url}}")
    private String externalUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    @Transactional
    public String createTokenForUser(User user) {
        // XÃ³a token cÅ© náº¿u cÃ³
        tokenRepository.deleteByUser(user);

        // Táº¡o token má»›i
        String tokenValue = UUID.randomUUID().toString();
        PasswordResetToken token = new PasswordResetToken();
        token.setToken(tokenValue);
        token.setUser(user);
        token.setExpiryDate(LocalDateTime.now().plusMinutes(TOKEN_EXPIRY_MINUTES));

        tokenRepository.save(token);
        return tokenValue;
    }

    @Override
    public void sendResetEmail(String email, String token) {
        sendEmail(email, token, "forgot-password");
    }

    @Override
    public void sendProfilePasswordChangeEmail(String email, String token) {
        sendEmail(email, token, "user-profile");
    }

    @Override
    public void sendAdminPasswordChangeEmail(String email, String token) {
        sendEmail(email, token, "admin-profile");
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByToken(token);

        if (tokenOpt.isEmpty()) {
            throw new RuntimeException("Token khÃ´ng há»£p lá»‡!");
        }

        PasswordResetToken resetToken = tokenOpt.get();

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(resetToken);
            throw new RuntimeException("Token Ä‘Ã£ háº¿t háº¡n! Vui lÃ²ng yÃªu cáº§u Ä‘áº·t láº¡i máº­t kháº©u má»›i.");
        }

        User user = resetToken.getUser();
        authService.updatePassword(user, newPassword);
        tokenBlacklistService.revokeAllUserTokens(user.getId());

        // XÃ³a token sau khi Ä‘Ã£ sá»­ dá»¥ng
        tokenRepository.delete(resetToken);
    }

    private void sendEmail(String email, String token, String source) {
        boolean fromProfile = !"forgot-password".equals(source);
        boolean fromAdminProfile = "admin-profile".equals(source);
        String resetUrl = externalUrl + "/reset-password?token=" + token + "&source=" + source;

        String subject = fromProfile
                ? (fromAdminProfile
                    ? "English Learning - Xác nhận đổi mật khẩu Admin"
                    : "English Learning - Xác nhận đổi mật khẩu")
                : "English Learning - Đặt lại mật khẩu";

        String title = fromAdminProfile
                ? "Xác nhận đổi mật khẩu Admin"
                : (fromProfile ? "Xác nhận đổi mật khẩu" : "Đặt lại mật khẩu");

        String description = fromAdminProfile
                ? "Bạn đã yêu cầu đổi mật khẩu cho tài khoản admin đang đăng nhập. Vui lòng nhấn vào nút bên dưới để xác nhận và đặt mật khẩu mới:"
                : (fromProfile
                ? "Bạn đã yêu cầu đổi mật khẩu từ trang hồ sơ. Vui lòng nhấn vào nút bên dưới để xác nhận và đặt mật khẩu mới:"
                : "Bạn đã yêu cầu đặt lại mật khẩu cho tài khoản English Learning. Vui lòng nhấn vào nút bên dưới để đặt mật khẩu mới:");

        String buttonText = fromProfile ? "Đổi mật khẩu" : "Đặt lại mật khẩu";

        // Build Thymeleaf context
        Map<String, Object> variables = new HashMap<>();
        variables.put("title", title);
        variables.put("description", description);
        variables.put("resetUrl", resetUrl);
        variables.put("buttonText", buttonText);
        variables.put("expiryMinutes", TOKEN_EXPIRY_MINUTES);
        variables.put("showSecurityNote", fromProfile);

        Context context = new Context();
        context.setVariables(variables);

        // Process template
        String htmlContent = templateEngine.process("mail/password-reset-email", context);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail, "English Learning Team");
            helper.setTo(email);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new RuntimeException("Không thể gửi email: " + e.getMessage(), e);
        }
    }
}

