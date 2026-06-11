package com.english.learning.util;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Caches GoogleIdTokenVerifier instances to avoid recreating them for each request.
 * GoogleIdTokenVerifier internally caches public keys from Google, so reusing the same
 * instance significantly speeds up token verification on subsequent calls.
 */
@Slf4j
@Component
public class CachedGoogleTokenVerifier {

    private final ConcurrentHashMap<String, GoogleIdTokenVerifier> verifierCache = new ConcurrentHashMap<>();

    public GoogleIdToken verify(String idTokenString, String googleClientId) throws Exception {
        GoogleIdTokenVerifier verifier = getOrCreateVerifier(googleClientId);
        return verifier.verify(idTokenString);
    }

    private GoogleIdTokenVerifier getOrCreateVerifier(String googleClientId) {
        return verifierCache.computeIfAbsent(googleClientId, clientId -> {
            log.info("Creating new GoogleIdTokenVerifier for clientId: {}", clientId);
            return new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(clientId))
                    .build();
        });
    }
}
