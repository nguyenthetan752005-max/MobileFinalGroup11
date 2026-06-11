package com.english.learning.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Component
public class MobileApiUrlResolver {

    private final String fallbackAppUrl;

    public MobileApiUrlResolver(@Value("${app.url:http://localhost:8080}") String fallbackAppUrl) {
        this.fallbackAppUrl = normalizeBaseUrl(fallbackAppUrl);
    }

    public String buildSentenceAudioUrl(Long sentenceId) {
        return resolveCurrentBaseUrl() + "/api/mobile/media/sentences/" + sentenceId + "/audio";
    }

    public String resolveCurrentBaseUrl() {
        try {
            return normalizeBaseUrl(ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString());
        } catch (IllegalStateException ignored) {
            return fallbackAppUrl;
        }
    }

    private String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            return "http://localhost:8080";
        }
        if (baseUrl.endsWith("/")) {
            return baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl;
    }
}
