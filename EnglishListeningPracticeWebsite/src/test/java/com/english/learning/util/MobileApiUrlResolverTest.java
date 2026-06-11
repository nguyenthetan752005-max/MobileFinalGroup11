package com.english.learning.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MobileApiUrlResolverTest {

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void shouldBuildAudioUrlFromCurrentRequestHost() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/mobile/catalog/bootstrap-lite");
        request.setScheme("http");
        request.setServerName("127.0.0.1");
        request.setServerPort(8081);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        MobileApiUrlResolver resolver = new MobileApiUrlResolver("http://localhost:8080");

        assertEquals("http://127.0.0.1:8081", resolver.resolveCurrentBaseUrl());
        assertEquals(
                "http://127.0.0.1:8081/api/mobile/media/sentences/42/audio",
                resolver.buildSentenceAudioUrl(42L)
        );
    }

    @Test
    void shouldFallbackToConfiguredAppUrlWhenNoRequestExists() {
        MobileApiUrlResolver resolver = new MobileApiUrlResolver("http://localhost:8081/");

        assertEquals("http://localhost:8081", resolver.resolveCurrentBaseUrl());
    }
}
