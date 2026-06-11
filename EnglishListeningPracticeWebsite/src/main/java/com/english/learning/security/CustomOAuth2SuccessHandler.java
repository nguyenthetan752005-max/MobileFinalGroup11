package com.english.learning.security;

import com.english.learning.entity.User;
import com.english.learning.repository.UserRepository;
import com.english.learning.service.settings.AppSettingService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final AppSettingService appSettingService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        String email = token.getPrincipal().getAttribute("email");
        String picture = token.getPrincipal().getAttribute("picture");

        Optional<User> userOpt = userRepository.findByEmail(email);
        User user;

        if (userOpt.isPresent()) {
            user = userOpt.get();
            if (!"GOOGLE".equals(user.getProvider())) {
                user.setProvider("GOOGLE");
                if (user.getAvatarUrl() == null) {
                    user.setAvatarUrl(picture);
                }
            }
            user.setIsActive(true);
            user = userRepository.save(user);
        } else {
            if (!appSettingService.isUserRegistrationAllowed()) {
                getRedirectStrategy().sendRedirect(request, response, "/login?error=registration-disabled");
                return;
            }

            user = new User();
            user.setEmail(email);

            String baseUsername = email.split("@")[0];
            if (userRepository.findByUsername(baseUsername).isPresent()) {
                baseUsername = baseUsername + "_" + UUID.randomUUID().toString().substring(0, 5);
            }
            user.setUsername(baseUsername);

            user.setPassword(BCrypt.hashpw(UUID.randomUUID().toString(), BCrypt.gensalt()));
            user.setProvider("GOOGLE");
            user.setAvatarUrl(picture);
            user.setIsActive(true);
            user = userRepository.save(user);
        }

        request.getSession().setAttribute("loggedInUser", user);
        getRedirectStrategy().sendRedirect(request, response, "/");
    }
}
