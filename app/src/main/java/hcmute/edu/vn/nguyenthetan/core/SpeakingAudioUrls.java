package hcmute.edu.vn.nguyenthetan.core;

import hcmute.edu.vn.nguyenthetan.BuildConfig;

/**
 * Resolves backend URLs for the user's speaking audio recordings. Centralised
 * here so the ViewModel does not have to know about {@link BuildConfig}.
 */
public final class SpeakingAudioUrls {

    private SpeakingAudioUrls() {
    }

    public static String best(long sentenceId) {
        return baseUrl() + "api/mobile/speaking/audio/best?sentenceId=" + sentenceId;
    }

    public static String current(long sentenceId) {
        return baseUrl() + "api/mobile/speaking/audio/current?sentenceId=" + sentenceId;
    }

    private static String baseUrl() {
        String base = BuildConfig.TUNGTUNG_API_BASE_URL;
        return base.endsWith("/") ? base : base + "/";
    }
}
