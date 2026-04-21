package hcmute.edu.vn.nguyenthetan.ui.lesson.manager;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.DefaultDataSource;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.datasource.HttpDataSource;
import androidx.media3.exoplayer.ExoPlayer;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import hcmute.edu.vn.nguyenthetan.R;
import hcmute.edu.vn.nguyenthetan.core.UserSessionStore;

public class AudioPlaybackManager {

    private static final String TAG = "AudioPlaybackManager";

    public interface Listener {
        void onProgressUpdate(long position, long duration, boolean isPlaying);
        void onPlaybackCompleted(long duration);
        void onPlaybackError(int errorMessageResId);
        void onPlaybackPaused();
    }

    private final Context context;
    private final UserSessionStore userSessionStore;
    private final Listener listener;
    private final Handler playbackHandler = new Handler(Looper.getMainLooper());

    private ExoPlayer mediaPlayer;
    private MediaPlayer userAudioPlayer;

    private boolean audioPrepared;
    private boolean audioPreparing;
    private long playingSentenceId = -1L;

    private final Runnable playbackProgressUpdater = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer == null) {
                return;
            }
            try {
                if (mediaPlayer.isPlaying()) {
                    listener.onProgressUpdate(
                            mediaPlayer.getCurrentPosition(),
                            mediaPlayer.getDuration(),
                            true
                    );
                    playbackHandler.postDelayed(this, 200L);
                }
            } catch (IllegalStateException ignored) {
                stopPlaybackUpdates();
            }
        }
    };

    public AudioPlaybackManager(Context context, UserSessionStore userSessionStore, Listener listener) {
        this.context = context;
        this.userSessionStore = userSessionStore;
        this.listener = listener;
    }

    public void playUserAudio(String url) {
        if (url == null || url.trim().isEmpty()) {
            return;
        }
        if (userAudioPlayer != null) {
            userAudioPlayer.release();
        }
        userAudioPlayer = new MediaPlayer();
        try {
            Map<String, String> headers = new HashMap<>();
            String token = userSessionStore.getToken();
            if (token != null && !token.isEmpty()) {
                headers.put("Authorization", "Bearer " + token);
            }
            userAudioPlayer.setDataSource(context, Uri.parse(url), headers);
            userAudioPlayer.prepareAsync();
            userAudioPlayer.setOnPreparedListener(MediaPlayer::start);
            userAudioPlayer.setOnErrorListener((mp, what, extra) -> {
                Log.e(TAG, "Error playing user audio: what=" + what + " extra=" + extra);
                return true;
            });
        } catch (Exception e) {
            Log.e(TAG, "Failed to start user audio", e);
        }
    }

    public void toggleAudioPlayback(long currentSentenceId, String currentAudioUrl, boolean replayRequested) {
        if (mediaPlayer != null && playingSentenceId == currentSentenceId) {
            if (!audioPrepared) {
                return;
            }
            if (mediaPlayer.isPlaying() && !replayRequested) {
                mediaPlayer.pause();
                stopPlaybackUpdates();
                listener.onProgressUpdate(mediaPlayer.getCurrentPosition(), mediaPlayer.getDuration(), false);
                return;
            }
            if (replayRequested) {
                mediaPlayer.seekTo(0L);
            }
            mediaPlayer.play();
            listener.onProgressUpdate(mediaPlayer.getCurrentPosition(), mediaPlayer.getDuration(), true);
            startPlaybackUpdates();
            return;
        }

        releaseAudioPlayer(false, true);

        // Build new ExoPlayer
        androidx.media3.exoplayer.source.DefaultMediaSourceFactory mediaSourceFactory = 
                new androidx.media3.exoplayer.source.DefaultMediaSourceFactory(context)
                        .setDataSourceFactory(buildAudioDataSourceFactory());
                        
        mediaPlayer = new ExoPlayer.Builder(context)
                .setMediaSourceFactory(mediaSourceFactory)
                .build();
                
        playingSentenceId = currentSentenceId;
        audioPrepared = false;
        audioPreparing = true;
        
        mediaPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (mediaPlayer == null) return;
                
                if (playbackState == Player.STATE_READY) {
                    audioPrepared = true;
                    audioPreparing = false;
                    mediaPlayer.play();
                    listener.onProgressUpdate(
                            Math.max(mediaPlayer.getCurrentPosition(), 0L),
                            Math.max(mediaPlayer.getDuration(), 0L),
                            true
                    );
                    startPlaybackUpdates();
                } else if (playbackState == Player.STATE_ENDED) {
                    stopPlaybackUpdates();
                    audioPrepared = true;
                    audioPreparing = false;
                    listener.onPlaybackCompleted(Math.max(mediaPlayer.getDuration(), 0L));
                }
            }

            @Override
            public void onPlayerError(@NonNull PlaybackException error) {
                stopPlaybackUpdates();
                logAudioPlaybackError(error, currentAudioUrl);
                audioPrepared = false;
                audioPreparing = false;
                releaseAudioPlayer(false, false);
                listener.onPlaybackError(resolveAudioErrorMessage(error));
            }
        });

        try {
            String source = resolveAudioPlaybackSource(currentSentenceId, currentAudioUrl);
            if (source.isEmpty()) {
                listener.onPlaybackError(R.string.lesson_audio_failed);
                return;
            }
            mediaPlayer.setMediaItem(buildAudioMediaItem(source));
            mediaPlayer.prepare();
        } catch (RuntimeException e) {
            audioPrepared = false;
            audioPreparing = false;
            releaseAudioPlayer(false, false);
            listener.onPlaybackError(R.string.lesson_audio_failed);
        }
    }

    public void releaseLessonMedia() {
        releaseAudioPlayer(true, true);
        if (userAudioPlayer != null) {
            userAudioPlayer.release();
            userAudioPlayer = null;
        }
    }

    public void releaseAudioPlayer(boolean notifyListener, boolean stopUpdates) {
        if (stopUpdates) {
            stopPlaybackUpdates();
        }
        if (mediaPlayer != null) {
            try {
                if (notifyListener && audioPrepared) {
                    long position = mediaPlayer.getCurrentPosition();
                    long duration = mediaPlayer.getDuration();
                    listener.onProgressUpdate(position, duration, false);
                }
            } catch (IllegalStateException ignored) {
                if (notifyListener) {
                    listener.onPlaybackPaused();
                }
            }
            mediaPlayer.pause();
            mediaPlayer.release();
            mediaPlayer = null;
        } else if (notifyListener) {
            listener.onPlaybackPaused();
        }
        playingSentenceId = -1L;
        audioPrepared = false;
        audioPreparing = false;
    }

    private void startPlaybackUpdates() {
        stopPlaybackUpdates();
        playbackHandler.post(playbackProgressUpdater);
    }

    private void stopPlaybackUpdates() {
        playbackHandler.removeCallbacks(playbackProgressUpdater);
    }

    private MediaItem buildAudioMediaItem(@NonNull String source) {
        if (source.startsWith("/") || source.startsWith("file:")) {
            File file = source.startsWith("file:") ? new File(Uri.parse(source).getPath()) : new File(source);
            return MediaItem.fromUri(Uri.fromFile(file));
        }
        return MediaItem.fromUri(Uri.parse(source));
    }

    @OptIn(markerClass = UnstableApi.class)
    private DataSource.Factory buildAudioDataSourceFactory() {
        DefaultHttpDataSource.Factory httpFactory = new DefaultHttpDataSource.Factory();
        String token = userSessionStore == null ? null : userSessionStore.getToken();
        if (token != null && !token.trim().isEmpty()) {
            httpFactory.setDefaultRequestProperties(Collections.singletonMap(
                    "Authorization",
                    "Bearer " + token
            ));
        }
        return new DefaultDataSource.Factory(context, httpFactory);
    }

    private String resolveAudioPlaybackSource(long sentenceId, String currentAudioUrl) {
        File cachedFile = findCachedAudioFile(sentenceId);
        if (cachedFile.exists() && cachedFile.isFile() && cachedFile.length() > 0L) {
            return cachedFile.getAbsolutePath();
        }
        return currentAudioUrl == null ? "" : currentAudioUrl;
    }

    @NonNull
    private File findCachedAudioFile(long sentenceId) {
        File cacheDir = new File(context.getFilesDir(), "audio_cache");
        File[] candidates = cacheDir.listFiles((dir, name) -> name.startsWith("audio_" + sentenceId + "."));
        if (candidates != null) {
            for (File candidate : candidates) {
                if (candidate != null && candidate.isFile() && candidate.length() > 0L) {
                    return candidate;
                }
            }
        }
        return new File(cacheDir, "audio_" + sentenceId + ".mp3");
    }

    private int resolveAudioErrorMessage(@NonNull PlaybackException error) {
        Throwable cause = error.getCause();
        if (cause instanceof HttpDataSource.InvalidResponseCodeException) {
            HttpDataSource.InvalidResponseCodeException httpError =
                    (HttpDataSource.InvalidResponseCodeException) cause;
            if (httpError.responseCode == 401 || httpError.responseCode == 403) {
                return R.string.lesson_audio_unauthorized;
            }
        }
        return R.string.lesson_audio_failed;
    }

    private void logAudioPlaybackError(@NonNull PlaybackException error, String audioUrl) {
        Throwable cause = error.getCause();
        if (cause instanceof HttpDataSource.InvalidResponseCodeException) {
            HttpDataSource.InvalidResponseCodeException httpError =
                    (HttpDataSource.InvalidResponseCodeException) cause;
            Log.e(
                    TAG,
                    "Audio playback failed. HTTP "
                            + httpError.responseCode
                            + " for URL: "
                            + audioUrl,
                    error
            );
            return;
        }
        Log.e(TAG, "Audio playback failed for URL: " + audioUrl, error);
    }
}
