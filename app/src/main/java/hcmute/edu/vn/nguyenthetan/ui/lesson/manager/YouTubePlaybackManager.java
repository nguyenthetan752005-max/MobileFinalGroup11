package hcmute.edu.vn.nguyenthetan.ui.lesson.manager;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.webkit.WebViewCompat;

import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import java.util.Locale;

public class YouTubePlaybackManager {

    private static final String TAG = "YouTubePlaybackManager";

    public interface Listener {
        void onPlayerReadyInitSync();
        void onPlayerFailed(); 
        void onVideoDurationFound(long durationMillis);
        void onProgressUpdate(long position, long duration, boolean isPlaying);
        void onResetPlayback();
        void onCompletePlayback(long durationMillis);
    }

    private final Context context;
    private final LifecycleOwner lifecycleOwner;
    private final YouTubePlayerView youTubePlayerView;
    private final Listener listener;
    private final Handler playbackHandler = new Handler(Looper.getMainLooper());

    private YouTubePlayer youtubePlayer;
    private boolean youtubePlayerReady;
    private boolean youtubePlaybackReleased;
    private boolean youtubePlayerFallbackTriggered;
    private Boolean inlineYoutubeSupported;
    private boolean youtubePlayerInitializationStarted;
    private String loadedVideoClipKey = "";

    private Runnable completeVideoPlaybackRunnable;

    public YouTubePlaybackManager(Context context, LifecycleOwner lifecycleOwner, YouTubePlayerView youTubePlayerView, Listener listener) {
        this.context = context;
        this.lifecycleOwner = lifecycleOwner;
        this.youTubePlayerView = youTubePlayerView;
        this.listener = listener;

        completeVideoPlaybackRunnable = () -> {
            if (this.listener != null) {
                // Because we pass the duration in the scheduled call, we rely on the caller to know it or we pass it
                // Actually the LessonActivity used to resolve the duration. The manager can calculate it.
            }
        };
    }

    public void initialize() {
        if (!supportsEmbeddedYoutubePlayback()) {
            youtubePlayerReady = false;
            youtubePlayer = null;
            Log.w(TAG, "Embedded YouTube playback is not supported on this WebView build.");
            return;
        }
        lifecycleOwner.getLifecycle().addObserver(youTubePlayerView);
    }

    public void ensureInitialized(String youtubeVideoId, Double startTime, long currentSentenceId) {
        if (!supportsEmbeddedYoutubePlayback()) {
            return;
        }
        if (youtubePlayerInitializationStarted) {
            return;
        }
        if (youtubeVideoId == null || youtubeVideoId.trim().isEmpty()) {
            return;
        }

        youtubePlayerInitializationStarted = true;
        youtubePlayerReady = false;
        youtubePlayer = null;
        youtubePlaybackReleased = false;
        youtubePlayerFallbackTriggered = false;

        float startSeconds = startTime == null ? 0f : (float) Math.max(0d, startTime);
        IFramePlayerOptions iframePlayerOptions = new IFramePlayerOptions.Builder(context)
                .start((int) startSeconds)
                .controls(1)
                .rel(0)
                .build();

        youTubePlayerView.initialize(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NonNull YouTubePlayer initializedPlayer) {
                youtubePlayer = initializedPlayer;
                youtubePlayerReady = true;
                youtubePlaybackReleased = false;
                youtubePlayerFallbackTriggered = false;
                listener.onPlayerReadyInitSync();
            }

            @Override
            public void onError(@NonNull YouTubePlayer initializedPlayer, @NonNull PlayerConstants.PlayerError error) {
                youtubePlayerReady = false;
                youtubePlayer = null;
                if (!youtubePlayerFallbackTriggered) {
                    youtubePlayerFallbackTriggered = true;
                    listener.onPlayerFailed();
                }
            }
        }, true, iframePlayerOptions);
    }

    public void syncVideoPlayers(String youtubeVideoId, Double startTime, Double endTime, long currentSentenceId) {
        if (!supportsEmbeddedYoutubePlayback()) {
            loadedVideoClipKey = "";
            stopVideoPlaybackCallbacks();
            return;
        }
        if (!youtubePlayerReady || youtubePlayer == null) {
            return;
        }

        String clipKey = buildVideoClipKey(youtubeVideoId, startTime, endTime, currentSentenceId);
        if (!clipKey.equals(loadedVideoClipKey)) {
            loadedVideoClipKey = clipKey;
            loadVideoPreview(youtubeVideoId, startTime);
        }
    }

    public void toggleVideoPlayback(String youtubeVideoId, Double startTime, Double endTime, long currentSentenceId, boolean replayRequested, boolean isPlaying) {
        if (youtubeVideoId == null || youtubeVideoId.trim().isEmpty()) {
            return; // Handled in UI
        }
        if (!supportsEmbeddedYoutubePlayback() || !youtubePlayerReady || youtubePlayer == null) {
            return; // Handled in UI
        }

        if (isPlaying && !replayRequested) {
            pauseYoutubePlayer();
            stopVideoPlaybackCallbacks();
            listener.onProgressUpdate(0, 0, false); // Paused
            return;
        }

        youtubePlaybackReleased = false;
        float startSeconds = startTime == null ? 0f : (float) Math.max(0d, startTime);
        if (replayRequested) {
            youtubePlayer.loadVideo(youtubeVideoId, startSeconds);
        } else {
            youtubePlayer.loadVideo(youtubeVideoId, startSeconds);
        }

        long durationMillis = resolveVideoDurationMillis(startTime, endTime);
        listener.onProgressUpdate(0L, durationMillis, true);
        stopVideoPlaybackCallbacks();
        
        if (durationMillis > 0L) {
            completeVideoPlaybackRunnable = () -> {
                pauseYoutubePlayer();
                listener.onCompletePlayback(durationMillis);
            };
            playbackHandler.postDelayed(completeVideoPlaybackRunnable, durationMillis);
        }
    }

    public void loadVideoPreview(String youtubeVideoId, Double startTime) {
        if (!youtubePlayerReady || youtubePlayer == null || youtubeVideoId == null || youtubeVideoId.trim().isEmpty()) {
            return;
        }
        float startSeconds = startTime == null ? 0f : (float) Math.max(0d, startTime);
        youtubePlayer.cueVideo(youtubeVideoId, startSeconds);
        youtubePlaybackReleased = false;
        listener.onResetPlayback();
    }

    public void pauseYoutubePlayer() {
        if (youtubePlayer != null && !youtubePlaybackReleased) {
            youtubePlayer.pause();
        }
    }

    public void stopVideoPlaybackCallbacks() {
        if (completeVideoPlaybackRunnable != null) {
            playbackHandler.removeCallbacks(completeVideoPlaybackRunnable);
        }
    }

    public void release() {
        stopVideoPlaybackCallbacks();
        pauseYoutubePlayer();
        youtubePlaybackReleased = true;
    }

    public boolean supportsEmbeddedYoutubePlayback() {
        if (inlineYoutubeSupported != null) {
            return inlineYoutubeSupported;
        }
        try {
            PackageInfo webViewPackage = WebViewCompat.getCurrentWebViewPackage(context);

            if (webViewPackage == null || webViewPackage.versionName == null) {
                inlineYoutubeSupported = false;
                return false;
            }
            String[] parts = webViewPackage.versionName.split("\\.");
            int majorVersion = Integer.parseInt(parts[0]);
            inlineYoutubeSupported = majorVersion >= 80;
            return inlineYoutubeSupported;
        } catch (Exception ignored) {
            inlineYoutubeSupported = false;
            return false;
        }
    }

    public void openYoutubeExternally(String youtubeVideoId, Double startTime) {
        if (youtubeVideoId == null || youtubeVideoId.trim().isEmpty()) {
            return;
        }
        long startSeconds = startTime == null ? 0L : Math.max(0L, Math.round(startTime));
        String url = "https://www.youtube.com/watch?v=" + youtubeVideoId + "&t=" + startSeconds + "s";
        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    public boolean isYoutubePlayerReady() {
        return youtubePlayerReady && youtubePlayer != null;
    }

    private String buildVideoClipKey(String youtubeVideoId, Double startTime, Double endTime, long currentSentenceId) {
        return String.format(
                Locale.US,
                "%s:%s:%s:%d",
                youtubeVideoId == null ? "" : youtubeVideoId,
                startTime == null ? "" : startTime.toString(),
                endTime == null ? "" : endTime.toString(),
                currentSentenceId
        );
    }

    private long resolveVideoDurationMillis(Double startTime, Double endTime) {
        if (startTime == null || endTime == null || endTime <= startTime) {
            return 0L;
        }
        return Math.round((endTime - startTime) * 1000d);
    }
}
