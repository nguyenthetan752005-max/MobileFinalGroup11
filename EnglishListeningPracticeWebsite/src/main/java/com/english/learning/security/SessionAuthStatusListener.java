package com.english.learning.security;

import com.english.learning.entity.User;
import com.english.learning.service.user.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionAttributeListener;
import jakarta.servlet.http.HttpSessionBindingEvent;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class SessionAuthStatusListener implements HttpSessionAttributeListener, HttpSessionListener {

    private static final List<String> AUTH_SESSION_KEYS = List.of("loggedInUser", "loggedInAdmin");
    private final UserService userService;

    @Override
    public void attributeAdded(HttpSessionBindingEvent event) {
        if (!isAuthKey(event.getName())) {
            return;
        }
        updateStatus(event.getValue(), true);
    }

    @Override
    public void attributeRemoved(HttpSessionBindingEvent event) {
        if (!isAuthKey(event.getName())) {
            return;
        }
        updateStatus(event.getValue(), false);
    }

    @Override
    public void attributeReplaced(HttpSessionBindingEvent event) {
        if (!isAuthKey(event.getName())) {
            return;
        }
        updateStatus(event.getValue(), false);
        HttpSession session = event.getSession();
        Object replacedValue = session.getAttribute(event.getName());
        updateStatus(replacedValue, true);
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        HttpSession session = se.getSession();
        for (String key : AUTH_SESSION_KEYS) {
            updateStatus(session.getAttribute(key), false);
        }
    }

    private boolean isAuthKey(String key) {
        return AUTH_SESSION_KEYS.contains(key);
    }

    private void updateStatus(Object value, boolean isActive) {
        if (!(value instanceof User user) || user.getId() == null) {
            return;
        }
        userService.updateActiveStatus(user.getId(), isActive);
    }
}
