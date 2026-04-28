package hcmute.edu.vn.nguyenthetan.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Sanity-check the audio URL builder. Catches regressions in URL shape that
 * would silently break user playback (no exception, just 404).
 */
public class SpeakingAudioUrlsTest {

    @Test
    public void best_appendsSentenceIdQueryParam() {
        String url = SpeakingAudioUrls.best(42L);
        assertTrue("URL should end with sentenceId query param: " + url,
                url.endsWith("api/mobile/speaking/audio/best?sentenceId=42"));
    }

    @Test
    public void current_appendsSentenceIdQueryParam() {
        String url = SpeakingAudioUrls.current(7L);
        assertTrue("URL should end with sentenceId query param: " + url,
                url.endsWith("api/mobile/speaking/audio/current?sentenceId=7"));
    }

    @Test
    public void bestAndCurrent_useDifferentEndpoints() {
        assertTrue(SpeakingAudioUrls.best(1L).contains("/best"));
        assertTrue(SpeakingAudioUrls.current(1L).contains("/current"));
    }

    @Test
    public void zeroSentenceId_isStillFormatted() {
        // Defensive: builders should not silently drop a 0 id.
        assertTrue(SpeakingAudioUrls.best(0L).contains("sentenceId=0"));
    }
}
