package hcmute.edu.vn.nguyenthetan.ui.lesson;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.annotation.OptIn;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.util.UnstableApi;
import androidx.webkit.WebViewCompat;
import android.media.MediaPlayer;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.datasource.DataSource;
import androidx.media3.datasource.DefaultDataSource;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.datasource.HttpDataSource;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.tabs.TabLayout;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import hcmute.edu.vn.nguyenthetan.R;
import hcmute.edu.vn.nguyenthetan.TungTungApplication;
import hcmute.edu.vn.nguyenthetan.core.UserSessionStore;
import hcmute.edu.vn.nguyenthetan.data.remote.api.MobileApiService;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.CheckDictationRequestDto;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.DictationResultDto;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.ProgressUpdateRequestDto;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.SpeakingResultDto;
import hcmute.edu.vn.nguyenthetan.databinding.ActivityLessonBinding;
import hcmute.edu.vn.nguyenthetan.domain.model.lesson.DictationFeedback;
import hcmute.edu.vn.nguyenthetan.domain.model.lesson.SentenceStatus;
import hcmute.edu.vn.nguyenthetan.ui.onboarding.OnboardingActivity;
import hcmute.edu.vn.nguyenthetan.ui.auth.AccountLockUiHandler;
import hcmute.edu.vn.nguyenthetan.work.AudioDownloadWorker;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LessonActivity extends AppCompatActivity implements TranscriptAdapter.Listener {

    private static final String TAG = "LessonActivity";
    private static final String EXTRA_LESSON_ID = "extra_lesson_id";
    private static final int RECORD_AUDIO_REQUEST_CODE = 7001;
    private static final int SPEAKING_SAMPLE_RATE = 16000;

    private ActivityLessonBinding binding;
    private LessonViewModel viewModel;
    private TranscriptAdapter transcriptAdapter;
    private CommentsAdapter commentsAdapter;
    private MobileApiService mobileApiService;
    private UserSessionStore userSessionStore;
    private final Handler playbackHandler = new Handler(Looper.getMainLooper());
    private final List<Integer> visibleTabs = new ArrayList<>();
    private long lessonId;
    private LessonViewModel.UiState latestState;
    private ExoPlayer mediaPlayer;
    private AudioRecord audioRecord;
    private File pendingRecordingFile;
    private Thread recordingThread;
    private volatile boolean isRecordingAudio;
    private int audioBufferSize;
    private MediaPlayer userAudioPlayer;
    private long playingSentenceId = -1L;
    private boolean updatingTabs;
    private String loadedVideoClipKey = "";
    private boolean audioPrepared;
    private boolean audioPreparing;
    private YouTubePlayer youtubePlayer;
    private boolean youtubePlayerReady;
    private boolean youtubePlaybackReleased;
    private boolean youtubePlayerFallbackTriggered;
    private Boolean inlineYoutubeSupported;
    private boolean audioPrefetchEnqueued;
    private boolean youtubePlayerInitializationStarted;
    private final Runnable playbackProgressUpdater = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer == null) {
                return;
            }
            try {
                if (mediaPlayer.isPlaying()) {
                    viewModel.updatePlaybackProgress(
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
    private final Runnable completeVideoPlaybackRunnable = new Runnable() {
        @Override
        public void run() {
            if (latestState == null) {
                return;
            }
            viewModel.completePlayback(resolveVideoDurationMillis(latestState));
        }
    };

    public static Intent newIntent(Context context, long lessonId) {
        Intent intent = new Intent(context, LessonActivity.class);
        intent.putExtra(EXTRA_LESSON_ID, lessonId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLessonBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        AccountLockUiHandler.attach(this);

        initializeYoutubePlayer();

        transcriptAdapter = new TranscriptAdapter(this);
        commentsAdapter = new CommentsAdapter();
        binding.recyclerTranscript.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerTranscript.setAdapter(transcriptAdapter);
        binding.recyclerComments.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerComments.setAdapter(commentsAdapter);

        lessonId = getIntent().getLongExtra(EXTRA_LESSON_ID, 0L);
        TungTungApplication application = (TungTungApplication) getApplication();
        mobileApiService = application.getAppContainer().getMobileApiService();
        userSessionStore = application.getAppContainer().getUserSessionStore();

        LessonViewModelFactory factory = new LessonViewModelFactory(
                application.getAppContainer().getLessonSessionUseCase(),
                application.getAppContainer().getLessonProgressUseCase(),
                application.getAppContainer().getCommentsUseCase(),
                application.getAppContainer().getCheckDictationAnswerUseCase(),
                application.getAppContainer().getSaveSentenceStatusUseCase(),
                application.getAppContainer().getSaveSpeakingAttemptUseCase(),
                lessonId
        );
        viewModel = new ViewModelProvider(this, factory).get(LessonViewModel.class);

        binding.buttonBack.setOnClickListener(v -> finish());
        binding.buttonPlay.setOnClickListener(v -> handlePrimaryMediaAction(false));
        binding.buttonReplay.setOnClickListener(v -> handlePrimaryMediaAction(true));
        binding.buttonCheck.setOnClickListener(v -> submitDictationCheck());
        binding.buttonSkip.setOnClickListener(v -> {
            releaseLessonMedia(true, false);
            submitDictationSkip();
        });
        binding.buttonNext.setOnClickListener(v -> {
            releaseLessonMedia(true, false);
            viewModel.nextSentence();
        });
        binding.buttonTranscriptPrev.setOnClickListener(v -> {
            releaseLessonMedia(true, false);
            viewModel.previousSentence();
        });
        binding.buttonTranscriptPlay.setOnClickListener(v -> handlePrimaryMediaAction(false));
        binding.buttonTranscriptNext.setOnClickListener(v -> {
            releaseLessonMedia(true, false);
            viewModel.nextSentence();
        });
        binding.buttonSpeakingPlay.setOnClickListener(v -> handlePrimaryMediaAction(false));
        binding.buttonRecord.setOnClickListener(v -> handleRecordAction());
        binding.buttonPlayBestAudio.setOnClickListener(v -> playUserAudio(latestState != null ? latestState.bestAudioUrl : null));
        binding.buttonPlayCurrentAudio.setOnClickListener(v -> playUserAudio(latestState != null ? latestState.currentUserAudioUrl : null));
        binding.buttonTryAgain.setOnClickListener(v -> viewModel.retrySpeaking());
        binding.buttonSpeakingNext.setOnClickListener(v -> {
            releaseLessonMedia(true, false);
            viewModel.nextSentence();
        });
        binding.buttonWriteComment.setOnClickListener(v -> {
            if (!userSessionStore.isLoggedIn()) {
                requireLoginForRestrictedFeature();
                return;
            }
            Toast.makeText(this, "Comment composer will be connected after backend wiring.", Toast.LENGTH_SHORT).show();
        });

        binding.inputDictation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                viewModel.onInputChanged(s == null ? "" : s.toString());
            }
        });

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (updatingTabs || tab == null) {
                    return;
                }
                int position = tab.getPosition();
                if (position >= 0 && position < visibleTabs.size()) {
                    viewModel.selectTab(visibleTabs.get(position));
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        viewModel.getUiState().observe(this, this::render);
        viewModel.getLoadingState().observe(this, loading ->
                binding.progressLessonLoad.setVisibility(Boolean.TRUE.equals(loading) ? View.VISIBLE : View.GONE)
        );
        application.getAppContainer().getSyncErrorMessage().observe(this, message -> {
            if (message != null && !message.trim().isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        });
        viewModel.load();
    }

    private void render(@NonNull LessonViewModel.UiState state) {
        if (state.title == null) {
            Log.w(TAG, "Render skipped because title is null.");
            return;
        }

        long previousSentenceId = latestState == null ? -1L : latestState.currentSentenceId;
        logRenderState("before-render", state, previousSentenceId);
        latestState = state;
        if (previousSentenceId != -1L && previousSentenceId != state.currentSentenceId) {
            Log.d(
                    TAG,
                    "Sentence changed. Releasing lesson media. previousSentenceId="
                            + previousSentenceId
                            + ", nextSentenceId="
                            + state.currentSentenceId
            );
            releaseLessonMedia(false, false);
        }
        ensureYoutubePlayerInitialized(state);
        updateTabs(state);
        syncVideoPlayers(state);

        binding.textLessonTitle.setText(state.title);
        binding.textLessonSubtitle.setText(state.subtitle);
        binding.chipSentenceCounter.setText(state.sentenceCounter);
        binding.progressLesson.setProgressCompat(state.progressPercent, true);
        applyStatusChip(state.currentStatus);

        binding.textHint.setText(state.hint);
        binding.textMediaSummary.setText(state.mediaSummary);
        binding.textTranscriptMediaSummary.setText(state.mediaSummary);
        if (!safeInput().equals(state.inputText)) {
            binding.inputDictation.setText(state.inputText);
            if (binding.inputDictation.getText() != null) {
                binding.inputDictation.setSelection(binding.inputDictation.getText().length());
            }
        }
        binding.cardFeedback.setVisibility(state.showFeedback ? View.VISIBLE : View.GONE);
        binding.textFeedbackTitle.setText(state.feedbackTitle);
        binding.textCorrectWords.setText(state.correctWords);
        binding.textNewHint.setText(state.newHint);
        binding.textMaskedWords.setText(state.maskedWords);
        binding.buttonNext.setVisibility(state.showNextButton && state.selectedTab == LessonViewModel.TAB_DICTATION ? View.VISIBLE : View.GONE);

        binding.textTranscriptSentence.setText(state.transcriptSentence);
        binding.textPlaybackTime.setText(state.videoLesson ? state.playbackLabel : state.playbackTime);
        binding.textTranscriptPlaybackTime.setText(state.videoLesson ? state.playbackLabel : state.playbackTime);
        binding.progressPlayback.setProgressCompat(state.playbackPercent, true);
        binding.progressTranscript.setProgressCompat(state.playbackPercent, true);
        binding.progressPlayback.setVisibility(state.videoLesson ? View.GONE : View.VISIBLE);
        binding.progressTranscript.setVisibility(state.videoLesson ? View.GONE : View.VISIBLE);
        binding.buttonReplay.setVisibility(state.videoLesson ? View.GONE : View.VISIBLE);
        binding.videoPlayerCard.setVisibility(state.videoLesson && supportsEmbeddedYoutubePlayback() ? View.VISIBLE : View.GONE);
        binding.buttonPlay.setImageResource(state.playing ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
        binding.buttonTranscriptPlay.setImageResource(state.playing ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
        binding.buttonSpeakingPlay.setImageResource(state.playing ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
        binding.buttonPlay.setEnabled(state.videoLesson || state.canPlayAudio);
        binding.buttonReplay.setEnabled(state.canPlayAudio);
        binding.buttonTranscriptPlay.setEnabled(state.videoLesson || state.canPlayAudio);
        binding.buttonSpeakingPlay.setEnabled(state.videoLesson || state.canPlayAudio);
        scheduleAudioPrefetchIfNeeded(state);
        transcriptAdapter.submitList(state.transcriptRows);

        binding.textSpeakingReference.setText(state.speakingReference);
        binding.textRecordingState.setText(state.recordingLabel);
        binding.buttonRecord.setImageResource(state.recording ? android.R.drawable.ic_media_pause : android.R.drawable.ic_btn_speak_now);
        binding.buttonRecord.setEnabled(!state.speakingBusy);
        binding.textBestScore.setText(state.bestScore);
        binding.buttonPlayBestAudio.setVisibility(state.bestAudioUrl != null && !state.bestAudioUrl.trim().isEmpty() ? View.VISIBLE : View.GONE);
        binding.textBestTranscript.setText(state.bestTranscript);
        binding.textCurrentScore.setText(state.currentScore);
        binding.buttonPlayCurrentAudio.setVisibility(state.currentUserAudioUrl != null && !state.currentUserAudioUrl.trim().isEmpty() ? View.VISIBLE : View.GONE);
        binding.textCurrentTranscript.setText(state.currentTranscript);
        binding.buttonSpeakingNext.setEnabled(state.speakingNextEnabled);
        binding.buttonWriteComment.setText(userSessionStore.isLoggedIn()
                ? R.string.label_write_comment
                : R.string.label_sign_in_to_comment);
        commentsAdapter.submitList(state.comments);

        binding.dictationContainer.setVisibility(state.selectedTab == LessonViewModel.TAB_DICTATION ? View.VISIBLE : View.GONE);
        binding.transcriptContainer.setVisibility(state.selectedTab == LessonViewModel.TAB_TRANSCRIPT ? View.VISIBLE : View.GONE);
        binding.speakingContainer.setVisibility(state.selectedTab == LessonViewModel.TAB_SPEAKING ? View.VISIBLE : View.GONE);
        binding.commentsCard.setVisibility(state.selectedTab == LessonViewModel.TAB_SPEAKING ? View.GONE : View.VISIBLE);
        logRenderState("after-render", state, previousSentenceId);
    }

    private void initializeYoutubePlayer() {
        if (!supportsEmbeddedYoutubePlayback()) {
            youtubePlayerReady = false;
            youtubePlayer = null;
            Log.w(TAG, "Embedded YouTube playback is not supported on this WebView build.");
            return;
        }
        getLifecycle().addObserver(binding.youtubePlayerView);
        Log.d(TAG, "YouTubePlayerView lifecycle observer attached. Waiting for manual initialization.");
    }

    private void ensureYoutubePlayerInitialized(@NonNull LessonViewModel.UiState state) {
        if (!state.videoLesson) {
            Log.d(TAG, "Skipping YouTube initialization because current lesson is not a video lesson.");
            return;
        }
        if (!supportsEmbeddedYoutubePlayback()) {
            Log.w(TAG, "Skipping YouTube initialization because embedded playback is unsupported.");
            return;
        }
        if (youtubePlayerInitializationStarted) {
            return;
        }
        if (state.youtubeVideoId == null || state.youtubeVideoId.trim().isEmpty()) {
            Log.w(TAG, "Skipping YouTube initialization because youtubeVideoId is empty.");
            return;
        }

        youtubePlayerInitializationStarted = true;
        youtubePlayerReady = false;
        youtubePlayer = null;
        youtubePlaybackReleased = false;
        youtubePlayerFallbackTriggered = false;

        float startSeconds = state.startTime == null ? 0f : Math.max(0f, state.startTime.floatValue());
        IFramePlayerOptions iframePlayerOptions = new IFramePlayerOptions.Builder(getApplicationContext())
                .start((int) startSeconds)
                .controls(1)
                .rel(0)
                .build();

        Log.d(
                TAG,
                "Initializing YouTube player manually. videoId="
                        + sanitizeForLog(state.youtubeVideoId)
                        + ", startSeconds="
                        + startSeconds
                        + ", sentenceId="
                        + state.currentSentenceId
        );

        binding.youtubePlayerView.initialize(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NonNull YouTubePlayer initializedPlayer) {
                youtubePlayer = initializedPlayer;
                youtubePlayerReady = true;
                youtubePlaybackReleased = false;
                youtubePlayerFallbackTriggered = false;
                Log.d(TAG, "YouTube player is ready.");
                if (latestState != null) {
                    Log.d(
                            TAG,
                            "Syncing video after player ready. videoId="
                                    + sanitizeForLog(latestState.youtubeVideoId)
                                    + ", sentenceId="
                                    + latestState.currentSentenceId
                                    + ", start="
                                    + latestState.startTime
                                    + ", end="
                                    + latestState.endTime
                    );
                    syncVideoPlayers(latestState);
                }
            }

            @Override
            public void onError(@NonNull YouTubePlayer initializedPlayer, @NonNull PlayerConstants.PlayerError error) {
                youtubePlayerReady = false;
                youtubePlayer = null;
                Log.e(
                        TAG,
                        "YouTube player error: "
                                + error
                                + ", videoId="
                                + sanitizeForLog(latestState == null ? null : latestState.youtubeVideoId)
                                + ", sentenceId="
                                + (latestState == null ? -1L : latestState.currentSentenceId)
                                + ", start="
                                + (latestState == null ? null : latestState.startTime)
                                + ", end="
                                + (latestState == null ? null : latestState.endTime)
                );
                if (latestState != null && latestState.videoLesson && !youtubePlayerFallbackTriggered) {
                    youtubePlayerFallbackTriggered = true;
                    Toast.makeText(LessonActivity.this, R.string.lesson_video_player_failed, Toast.LENGTH_SHORT).show();
                    openYoutubeExternally();
                }
            }
        }, true, iframePlayerOptions);
    }

    private void syncVideoPlayers(@NonNull LessonViewModel.UiState state) {
        if (!state.videoLesson) {
            Log.d(TAG, "syncVideoPlayers skipped because current lesson is not a video lesson.");
            loadedVideoClipKey = "";
            stopVideoPlaybackCallbacks();
            pauseYoutubePlayer();
            return;
        }
        if (!supportsEmbeddedYoutubePlayback()) {
            Log.w(
                    TAG,
                    "syncVideoPlayers disabled because embedded playback is unsupported. videoId="
                            + sanitizeForLog(state.youtubeVideoId)
            );
            loadedVideoClipKey = "";
            stopVideoPlaybackCallbacks();
            return;
        }
        if (!youtubePlayerReady || youtubePlayer == null) {
            Log.d(
                    TAG,
                    "syncVideoPlayers waiting for player readiness. ready="
                            + youtubePlayerReady
                            + ", playerNull="
                            + (youtubePlayer == null)
                            + ", released="
                            + youtubePlaybackReleased
                            + ", fallbackTriggered="
                            + youtubePlayerFallbackTriggered
                            + ", videoId="
                            + sanitizeForLog(state.youtubeVideoId)
                            + ", sentenceId="
                            + state.currentSentenceId
            );
            return;
        }

        String clipKey = buildVideoClipKey(state);
        if (!clipKey.equals(loadedVideoClipKey)) {
            loadedVideoClipKey = clipKey;
            Log.d(
                    TAG,
                    "Preparing YouTube preview. videoId="
                            + sanitizeForLog(state.youtubeVideoId)
                            + ", clipKey="
                            + clipKey
            );
            loadVideoPreview(state);
        } else {
            Log.d(
                    TAG,
                    "Skipping preview reload because clip key is unchanged. clipKey="
                            + clipKey
                            + ", playerReady="
                            + youtubePlayerReady
                            + ", released="
                            + youtubePlaybackReleased
            );
        }
    }

    private void applyStatusChip(SentenceStatus status) {
        if (status == null) {
            binding.chipStatus.setText("LOADING");
            binding.chipStatus.setChipBackgroundColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.tt_border)));
            binding.chipStatus.setTextColor(ContextCompat.getColor(this, R.color.tt_text_secondary));
            return;
        }
        int background;
        int textColor;
        if (status == SentenceStatus.COMPLETED) {
            background = ContextCompat.getColor(this, R.color.tt_success);
            textColor = ContextCompat.getColor(this, R.color.white);
        } else if (status == SentenceStatus.SKIPPED) {
            background = ContextCompat.getColor(this, R.color.tt_warning);
            textColor = ContextCompat.getColor(this, R.color.white);
        } else {
            background = ContextCompat.getColor(this, R.color.tt_border);
            textColor = ContextCompat.getColor(this, R.color.tt_text_secondary);
        }
        binding.chipStatus.setText(status.name().replace('_', ' '));
        binding.chipStatus.setChipBackgroundColor(ColorStateList.valueOf(background));
        binding.chipStatus.setTextColor(textColor);
    }

    private String safeInput() {
        Editable editable = binding.inputDictation.getText();
        return editable == null ? "" : editable.toString();
    }

    @Override
    public void onSentenceSelected(int position) {
        releaseLessonMedia(true, false);
        viewModel.selectSentence(position);
    }

    @Override
    public void onPlayRow(int position) {
        releaseLessonMedia(true, false);
        viewModel.selectSentence(position);
        binding.getRoot().post(() -> handlePrimaryMediaAction(true));
    }

    @Override
    protected void onPause() {
        Log.d(
                TAG,
                "onPause: releasing lesson media. ready="
                        + youtubePlayerReady
                        + ", playerNull="
                        + (youtubePlayer == null)
                        + ", released="
                        + youtubePlaybackReleased
                        + ", latestVideoId="
                        + sanitizeForLog(latestState == null ? null : latestState.youtubeVideoId)
        );
        releaseLessonMedia(true, false);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.d(
                TAG,
                "onDestroy: releasing lesson media and YouTube view. ready="
                        + youtubePlayerReady
                        + ", playerNull="
                        + (youtubePlayer == null)
                        + ", released="
                        + youtubePlaybackReleased
                        + ", latestVideoId="
                        + sanitizeForLog(latestState == null ? null : latestState.youtubeVideoId)
        );
        releaseLessonMedia(false, false);
        binding.youtubePlayerView.release();
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RECORD_AUDIO_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startRecording();
            } else {
                Toast.makeText(this, R.string.lesson_record_permission_denied, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateTabs(@NonNull LessonViewModel.UiState state) {
        List<Integer> desiredTabs = new ArrayList<>();
        if (state.showDictationTab) {
            desiredTabs.add(LessonViewModel.TAB_DICTATION);
        }
        if (state.showSpeakingTab) {
            desiredTabs.add(LessonViewModel.TAB_SPEAKING);
        }
        desiredTabs.add(LessonViewModel.TAB_TRANSCRIPT);

        if (desiredTabs.equals(visibleTabs)) {
            ensureSelectedTab(state.selectedTab);
            return;
        }

        updatingTabs = true;
        visibleTabs.clear();
        visibleTabs.addAll(desiredTabs);
        binding.tabLayout.removeAllTabs();
        for (Integer tabId : visibleTabs) {
            binding.tabLayout.addTab(binding.tabLayout.newTab().setText(resolveTabTitle(tabId)));
        }
        ensureSelectedTab(state.selectedTab);
        updatingTabs = false;
    }

    private void ensureSelectedTab(int logicalTab) {
        int selectedPosition = visibleTabs.indexOf(logicalTab);
        if (selectedPosition < 0) {
            selectedPosition = 0;
        }
        TabLayout.Tab selectedTab = binding.tabLayout.getTabAt(selectedPosition);
        if (selectedTab != null && !selectedTab.isSelected()) {
            selectedTab.select();
        }
    }

    private String resolveTabTitle(int tabId) {
        if (tabId == LessonViewModel.TAB_DICTATION) {
            return getString(R.string.lesson_dictation);
        }
        if (tabId == LessonViewModel.TAB_SPEAKING) {
            return getString(R.string.lesson_speaking);
        }
        return getString(R.string.lesson_transcript);
    }

    private void handlePrimaryMediaAction(boolean replayRequested) {
        if (latestState == null) {
            return;
        }
        if (latestState.videoLesson) {
            toggleVideoPlayback(replayRequested);
            return;
        }
        if (latestState.currentAudioUrl == null || latestState.currentAudioUrl.trim().isEmpty()) {
            Toast.makeText(this, R.string.lesson_no_audio_source, Toast.LENGTH_SHORT).show();
            return;
        }
        toggleAudioPlayback(replayRequested);
    }

    private void playUserAudio(String url) {
        if (url == null || url.trim().isEmpty()) {
            return;
        }
        if (userAudioPlayer != null) {
            userAudioPlayer.release();
        }
        userAudioPlayer = new MediaPlayer();
        try {
            java.util.Map<String, String> headers = new java.util.HashMap<>();
            String token = userSessionStore.getToken();
            if (token != null && !token.isEmpty()) {
                headers.put("Authorization", "Bearer " + token);
            }
            userAudioPlayer.setDataSource(this, android.net.Uri.parse(url), headers);
            userAudioPlayer.prepareAsync();
            userAudioPlayer.setOnPreparedListener(MediaPlayer::start);
            userAudioPlayer.setOnErrorListener((mp, what, extra) -> {
                Log.e(TAG, "Error playing user audio: what=" + what + " extra=" + extra);
                return true;
            });
        } catch (Exception e) {
            Log.e(TAG, "Failed to start user audio", e);
            Toast.makeText(this, R.string.lesson_audio_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleAudioPlayback(boolean replayRequested) {
        if (latestState == null) {
            return;
        }
        if (mediaPlayer != null && playingSentenceId == latestState.currentSentenceId) {
            if (!audioPrepared) {
                return;
            }
            if (mediaPlayer.isPlaying() && !replayRequested) {
                mediaPlayer.pause();
                stopPlaybackUpdates();
                viewModel.updatePlaybackProgress(mediaPlayer.getCurrentPosition(), mediaPlayer.getDuration(), false);
                return;
            }
            if (replayRequested) {
                mediaPlayer.seekTo(0L);
            }
            mediaPlayer.play();
            viewModel.updatePlaybackProgress(mediaPlayer.getCurrentPosition(), mediaPlayer.getDuration(), true);
            startPlaybackUpdates();
            return;
        }

        releaseAudioPlayer(false);
        mediaPlayer = new ExoPlayer.Builder(this)
                .setMediaSourceFactory(new androidx.media3.exoplayer.source.DefaultMediaSourceFactory(this)
                        .setDataSourceFactory(buildAudioDataSourceFactory()))
                .build();
        playingSentenceId = latestState.currentSentenceId;
        audioPrepared = false;
        audioPreparing = true;
        mediaPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (mediaPlayer == null) {
                    return;
                }
                if (playbackState == Player.STATE_READY) {
                    audioPrepared = true;
                    audioPreparing = false;
                    mediaPlayer.play();
                    viewModel.updatePlaybackProgress(
                            Math.max(mediaPlayer.getCurrentPosition(), 0L),
                            Math.max(mediaPlayer.getDuration(), 0L),
                            true
                    );
                    startPlaybackUpdates();
                } else if (playbackState == Player.STATE_ENDED) {
                    stopPlaybackUpdates();
                    audioPrepared = true;
                    audioPreparing = false;
                    viewModel.completePlayback(Math.max(mediaPlayer.getDuration(), 0L));
                }
            }

            @Override
            public void onPlayerError(@NonNull PlaybackException error) {
                stopPlaybackUpdates();
                logAudioPlaybackError(error);
                audioPrepared = false;
                audioPreparing = false;
                releaseAudioPlayer(false);
                Toast.makeText(
                        LessonActivity.this,
                        resolveAudioErrorMessage(error),
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
        try {
            mediaPlayer.setMediaItem(buildAudioMediaItem(resolveAudioPlaybackSource()));
            mediaPlayer.prepare();
        } catch (RuntimeException e) {
            audioPrepared = false;
            audioPreparing = false;
            releaseAudioPlayer(false);
            Toast.makeText(this, R.string.lesson_audio_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleVideoPlayback(boolean replayRequested) {
        if (latestState == null || latestState.youtubeVideoId == null || latestState.youtubeVideoId.trim().isEmpty()) {
            Log.w(TAG, "Cannot play video. Missing youtubeVideoId.");
            Toast.makeText(this, R.string.lesson_video_unavailable, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!supportsEmbeddedYoutubePlayback()) {
            Log.w(
                    TAG,
                    "Embedded playback unavailable. Falling back externally for videoId="
                            + sanitizeForLog(latestState.youtubeVideoId)
            );
            openYoutubeExternally();
            return;
        }
        if (!youtubePlayerReady || youtubePlayer == null) {
            Log.w(
                    TAG,
                    "YouTube player not ready. Falling back externally for videoId="
                            + sanitizeForLog(latestState.youtubeVideoId)
            );
            Toast.makeText(this, R.string.lesson_video_player_failed, Toast.LENGTH_SHORT).show();
            openYoutubeExternally();
            return;
        }

        if (latestState.playing && !replayRequested) {
            pauseYoutubePlayer();
            stopVideoPlaybackCallbacks();
            viewModel.pausePlayback();
            return;
        }

        youtubePlaybackReleased = false;
        float startSeconds = latestState.startTime == null ? 0f : Math.max(0f, latestState.startTime.floatValue());
        Log.d(
                TAG,
                "Loading YouTube video. replay="
                        + replayRequested
                        + ", videoId="
                        + sanitizeForLog(latestState.youtubeVideoId)
                        + ", startSeconds="
                        + startSeconds
                        + ", endSeconds="
                        + latestState.endTime
                        + ", sentenceId="
                        + latestState.currentSentenceId
        );
        if (replayRequested) {
            youtubePlayer.loadVideo(latestState.youtubeVideoId, startSeconds);
        } else {
            youtubePlayer.loadVideo(latestState.youtubeVideoId, startSeconds);
        }
        long durationMillis = resolveVideoDurationMillis(latestState);
        viewModel.updatePlaybackProgress(0L, durationMillis, true);
        stopVideoPlaybackCallbacks();
        if (durationMillis > 0L) {
            playbackHandler.postDelayed(completeVideoPlaybackRunnable, durationMillis);
        }
    }

    private void loadVideoPreview(@NonNull LessonViewModel.UiState state) {
        if (!youtubePlayerReady || youtubePlayer == null || state.youtubeVideoId == null || state.youtubeVideoId.trim().isEmpty()) {
            Log.w(
                    TAG,
                    "Skipping video preview. ready="
                            + youtubePlayerReady
                            + ", playerNull="
                            + (youtubePlayer == null)
                            + ", videoId="
                            + sanitizeForLog(state.youtubeVideoId)
            );
            return;
        }
        float startSeconds = state.startTime == null ? 0f : Math.max(0f, state.startTime.floatValue());
        Log.d(
                TAG,
                "Cueing YouTube preview. videoId="
                        + sanitizeForLog(state.youtubeVideoId)
                        + ", startSeconds="
                        + startSeconds
                        + ", sentenceId="
                        + state.currentSentenceId
        );
        youtubePlayer.cueVideo(state.youtubeVideoId, startSeconds);
        youtubePlaybackReleased = false;
        viewModel.resetPlayback();
    }

    private void openYoutubeExternally() {
        if (latestState == null || latestState.youtubeVideoId == null || latestState.youtubeVideoId.trim().isEmpty()) {
            Log.w(TAG, "Cannot open YouTube externally. Missing youtubeVideoId.");
            Toast.makeText(this, R.string.lesson_video_unavailable, Toast.LENGTH_SHORT).show();
            return;
        }
        long startSeconds = latestState.startTime == null ? 0L : Math.max(0L, Math.round(latestState.startTime));
        String url = "https://www.youtube.com/watch?v=" + latestState.youtubeVideoId + "&t=" + startSeconds + "s";
        Log.i(
                TAG,
                "Opening YouTube externally. videoId="
                        + sanitizeForLog(latestState.youtubeVideoId)
                        + ", url="
                        + url
        );
        startActivity(new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url)));
        Toast.makeText(this, R.string.lesson_video_external_fallback, Toast.LENGTH_SHORT).show();
    }

    private void pauseYoutubePlayer() {
        if (youtubePlayer != null && !youtubePlaybackReleased) {
            Log.d(TAG, "Pausing embedded YouTube player.");
            youtubePlayer.pause();
        } else {
            Log.d(
                    TAG,
                    "pauseYoutubePlayer skipped. playerNull="
                            + (youtubePlayer == null)
                            + ", released="
                            + youtubePlaybackReleased
            );
        }
    }

    private boolean supportsEmbeddedYoutubePlayback() {
        if (inlineYoutubeSupported != null) {
            return inlineYoutubeSupported;
        }
        try {
            PackageInfo webViewPackage = WebViewCompat.getCurrentWebViewPackage(this);

            if (webViewPackage == null || webViewPackage.versionName == null) {
                inlineYoutubeSupported = false;
                Log.w(TAG, "WebView package is unavailable. Embedded YouTube disabled.");
                return false;
            }
            String[] parts = webViewPackage.versionName.split("\\.");
            int majorVersion = Integer.parseInt(parts[0]);
            inlineYoutubeSupported = majorVersion >= 80;
            Log.d(
                    TAG,
                    "Detected WebView package="
                            + webViewPackage.packageName
                            + ", version="
                            + webViewPackage.versionName
                            + ", embeddedYoutubeSupported="
                            + inlineYoutubeSupported
            );
            return inlineYoutubeSupported;
        } catch (Exception ignored) {
            inlineYoutubeSupported = false;
            Log.e(TAG, "Failed to inspect WebView package for YouTube playback support.", ignored);
            return false;
        }
    }

    private String buildVideoClipKey(@NonNull LessonViewModel.UiState state) {
        return String.format(
                Locale.US,
                "%s:%s:%s:%d",
                state.youtubeVideoId == null ? "" : state.youtubeVideoId,
                state.startTime == null ? "" : state.startTime.toString(),
                state.endTime == null ? "" : state.endTime.toString(),
                state.currentSentenceId
        );
    }

    private String sanitizeForLog(String value) {
        if (value == null) {
            return "null";
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? "<empty>" : trimmed;
    }

    private long resolveVideoDurationMillis(@NonNull LessonViewModel.UiState state) {
        if (state.startTime == null || state.endTime == null || state.endTime <= state.startTime) {
            return 0L;
        }
        return Math.round((state.endTime - state.startTime) * 1000d);
    }

    private void handleRecordAction() {
        if (latestState == null) {
            return;
        }
        if (!userSessionStore.isLoggedIn()) {
            Toast.makeText(this, R.string.lesson_sign_in_for_ai, Toast.LENGTH_SHORT).show();
            viewModel.toggleRecording();
            return;
        }
        if (isRecordingAudio) {
            stopRecordingAndEvaluate();
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_REQUEST_CODE);
            return;
        }
        startRecording();
    }

    private void requireLoginForRestrictedFeature() {
        new android.app.AlertDialog.Builder(this)
                .setTitle(hcmute.edu.vn.nguyenthetan.R.string.dialog_login_required_title)
                .setMessage(hcmute.edu.vn.nguyenthetan.R.string.dialog_login_required_message)
                .setPositiveButton(hcmute.edu.vn.nguyenthetan.R.string.dialog_login_required_positive, (dialog, which) -> {
                    startActivity(new android.content.Intent(this, hcmute.edu.vn.nguyenthetan.ui.onboarding.OnboardingActivity.class));
                })
                .setNegativeButton(hcmute.edu.vn.nguyenthetan.R.string.dialog_login_required_negative, null)
                .show();
    }

    private void startRecording() {
        if (latestState == null) {
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, R.string.lesson_record_permission_denied, Toast.LENGTH_SHORT).show();
            return;
        }
        releaseRecorder(false);
        audioBufferSize = AudioRecord.getMinBufferSize(
                SPEAKING_SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
        );
        if (audioBufferSize <= 0) {
            viewModel.showSpeakingMessage(getString(R.string.lesson_record_failed));
            return;
        }
        pendingRecordingFile = new File(getCacheDir(), "speaking_" + System.currentTimeMillis() + ".wav");
        audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SPEAKING_SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                audioBufferSize
        );
        try {
            if (audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                releaseRecorder(false);
                viewModel.showSpeakingMessage(getString(R.string.lesson_record_failed));
                return;
            }
            writeEmptyWaveHeader(pendingRecordingFile);
            audioRecord.startRecording();
            isRecordingAudio = true;
            recordingThread = new Thread(() -> writeAudioToWaveFile(pendingRecordingFile), "speaking-wav-recorder");
            recordingThread.start();
            viewModel.setRecordingState(true);
        } catch (SecurityException e) {
            releaseRecorder(false);
            Toast.makeText(this, R.string.lesson_record_permission_denied, Toast.LENGTH_SHORT).show();
        } catch (IOException | RuntimeException e) {
            releaseRecorder(false);
            viewModel.showSpeakingMessage(getString(R.string.lesson_record_failed));
        }
    }

    private void stopRecordingAndEvaluate() {
        File audioFile = pendingRecordingFile;
        releaseRecorder(false);
        if (audioFile == null || !audioFile.exists() || latestState == null) {
            viewModel.showSpeakingMessage(getString(R.string.lesson_record_failed));
            return;
        }
        if (!userSessionStore.isLoggedIn()) {
            deleteTempFile(audioFile);
            return;
        }
        viewModel.setSpeakingBusy(getString(R.string.lesson_evaluating_speaking));

        RequestBody audioRequest = RequestBody.create(MediaType.parse("audio/wav"), audioFile);
        MultipartBody.Part audioPart = MultipartBody.Part.createFormData("audio", audioFile.getName(), audioRequest);
        RequestBody referenceText = RequestBody.create(MediaType.parse("text/plain"), latestState.speakingReference);
        RequestBody sentenceId = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(latestState.currentSentenceId));

        mobileApiService.evaluateSpeaking(audioPart, referenceText, sentenceId).enqueue(new Callback<SpeakingResultDto>() {
            @Override
            public void onResponse(@NonNull Call<SpeakingResultDto> call, @NonNull Response<SpeakingResultDto> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    viewModel.showSpeakingMessage(getString(R.string.lesson_speaking_failed));
                    deleteTempFile(audioFile);
                    return;
                }
                SpeakingResultDto body = response.body();
                String transcript = body.recognizedText == null ? "" : body.recognizedText;
                String feedback = body.feedback == null ? "" : body.feedback;
                if (body.accuracy > 0d) {
                    feedback = feedback.isEmpty()
                            ? String.format(Locale.US, "Accuracy: %.1f%%", body.accuracy)
                            : feedback + "\nAccuracy: " + String.format(Locale.US, "%.1f%%", body.accuracy);
                }

                hcmute.edu.vn.nguyenthetan.domain.model.lesson.SpeakingAttempt bestAttempt = null;
                if (body.bestResult != null) {
                    bestAttempt = new hcmute.edu.vn.nguyenthetan.domain.model.lesson.SpeakingAttempt(
                            body.bestResult.score,
                            body.bestResult.recognizedText == null ? "" : body.bestResult.recognizedText,
                            body.bestResult.feedback == null ? "" : body.bestResult.feedback,
                            body.bestResult.audioUrl == null ? "" : body.bestResult.audioUrl
                    );
                }

                viewModel.applySpeakingEvaluation(body.score, transcript, feedback, body.audioUrl, bestAttempt);
                deleteTempFile(audioFile);
            }

            @Override
            public void onFailure(@NonNull Call<SpeakingResultDto> call, @NonNull Throwable throwable) {
                viewModel.showSpeakingMessage(getString(R.string.lesson_speaking_failed));
                deleteTempFile(audioFile);
            }
        });
    }

    private void startPlaybackUpdates() {
        stopPlaybackUpdates();
        playbackHandler.post(playbackProgressUpdater);
    }

    private void stopPlaybackUpdates() {
        playbackHandler.removeCallbacks(playbackProgressUpdater);
    }

    private void stopVideoPlaybackCallbacks() {
        playbackHandler.removeCallbacks(completeVideoPlaybackRunnable);
    }

    private void releaseLessonMedia(boolean notifyViewModel, boolean deletePendingRecording) {
        Log.d(
                TAG,
                "releaseLessonMedia(notifyViewModel="
                        + notifyViewModel
                        + ", deletePendingRecording="
                        + deletePendingRecording
                        + ") before release: ready="
                        + youtubePlayerReady
                        + ", playerNull="
                        + (youtubePlayer == null)
                        + ", released="
                        + youtubePlaybackReleased
                        + ", latestVideoId="
                        + sanitizeForLog(latestState == null ? null : latestState.youtubeVideoId)
                        + ", latestSentenceId="
                        + (latestState == null ? -1L : latestState.currentSentenceId)
        );
        stopVideoPlaybackCallbacks();
        pauseYoutubePlayer();
        releaseAudioPlayer(notifyViewModel);
        if (userAudioPlayer != null) {
            userAudioPlayer.release();
            userAudioPlayer = null;
        }
        releaseRecorder(deletePendingRecording);
        if (notifyViewModel && latestState != null && latestState.videoLesson) {
            viewModel.pausePlayback();
        }
        youtubePlaybackReleased = true;
        Log.d(
                TAG,
                "releaseLessonMedia completed. ready="
                        + youtubePlayerReady
                        + ", playerNull="
                        + (youtubePlayer == null)
                        + ", released="
                        + youtubePlaybackReleased
        );
    }

    private void logRenderState(String stage, @NonNull LessonViewModel.UiState state, long previousSentenceId) {
        Log.d(
                TAG,
                "render["
                        + stage
                        + "]: title="
                        + sanitizeForLog(state.title)
                        + ", videoLesson="
                        + state.videoLesson
                        + ", selectedTab="
                        + state.selectedTab
                        + ", sentenceId="
                        + state.currentSentenceId
                        + ", previousSentenceId="
                        + previousSentenceId
                        + ", videoId="
                        + sanitizeForLog(state.youtubeVideoId)
                        + ", start="
                        + state.startTime
                        + ", end="
                        + state.endTime
                        + ", playing="
                        + state.playing
                        + ", canPlayAudio="
                        + state.canPlayAudio
                        + ", playerReady="
                        + youtubePlayerReady
                        + ", playerNull="
                        + (youtubePlayer == null)
                        + ", released="
                        + youtubePlaybackReleased
                        + ", fallbackTriggered="
                        + youtubePlayerFallbackTriggered
                        + ", cardVisible="
                        + (binding.videoPlayerCard.getVisibility() == View.VISIBLE)
        );
    }

    private void releaseAudioPlayer(boolean notifyViewModel) {
        stopPlaybackUpdates();
        if (mediaPlayer != null) {
            try {
                if (notifyViewModel && audioPrepared) {
                    long position = mediaPlayer.getCurrentPosition();
                    long duration = mediaPlayer.getDuration();
                    viewModel.updatePlaybackProgress(position, duration, false);
                }
            } catch (IllegalStateException ignored) {
                if (notifyViewModel) {
                    viewModel.pausePlayback();
                }
            }
            mediaPlayer.pause();
            mediaPlayer.release();
            mediaPlayer = null;
        } else if (notifyViewModel && (latestState == null || !latestState.videoLesson)) {
            viewModel.pausePlayback();
        }
        playingSentenceId = -1L;
        audioPrepared = false;
        audioPreparing = false;
    }

    private void releaseRecorder(boolean deletePendingRecording) {
        isRecordingAudio = false;
        if (audioRecord != null) {
            try {
                audioRecord.stop();
            } catch (IllegalStateException ignored) {
            }
            audioRecord.release();
            audioRecord = null;
        }
        if (recordingThread != null) {
            try {
                recordingThread.join(500L);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            recordingThread = null;
        }
        viewModel.setRecordingState(false);
        if (deletePendingRecording && pendingRecordingFile != null) {
            deleteTempFile(pendingRecordingFile);
        }
        pendingRecordingFile = null;
    }

    private void deleteTempFile(File file) {
        if (file != null && file.exists()) {
            file.delete();
        }
    }

    private void scheduleAudioPrefetchIfNeeded(@NonNull LessonViewModel.UiState state) {
        if (audioPrefetchEnqueued || lessonId <= 0L || state.videoLesson) {
            return;
        }
        audioPrefetchEnqueued = true;
        AudioDownloadWorker.enqueue(this, lessonId);
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
            httpFactory.setDefaultRequestProperties(java.util.Collections.singletonMap(
                    "Authorization",
                    "Bearer " + token
            ));
        }
        return new DefaultDataSource.Factory(this, httpFactory);
    }

    private String resolveAudioPlaybackSource() {
        if (latestState == null) {
            return "";
        }
        File cachedFile = findCachedAudioFile(latestState.currentSentenceId);
        if (cachedFile.exists() && cachedFile.isFile() && cachedFile.length() > 0L) {
            return cachedFile.getAbsolutePath();
        }
        return latestState.currentAudioUrl == null ? "" : latestState.currentAudioUrl;
    }

    @NonNull
    private File findCachedAudioFile(long sentenceId) {
        File cacheDir = new File(getFilesDir(), "audio_cache");
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

    private void logAudioPlaybackError(@NonNull PlaybackException error) {
        String audioUrl = latestState == null ? "" : latestState.currentAudioUrl;
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

    private void submitDictationCheck() {
        if (latestState == null || safeInput().trim().isEmpty()) {
            return;
        }
        mobileApiService.checkDictation(
                new CheckDictationRequestDto(viewModel.getCurrentSentenceId(), safeInput().trim())
        ).enqueue(new Callback<DictationResultDto>() {
            @Override
            public void onResponse(@NonNull Call<DictationResultDto> call, @NonNull Response<DictationResultDto> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    viewModel.checkAnswer();
                    return;
                }
                viewModel.applyDictationFeedback(mapDictationResponse(response.body()), response.body().correct, false);
            }

            @Override
            public void onFailure(@NonNull Call<DictationResultDto> call, @NonNull Throwable throwable) {
                viewModel.checkAnswer();
            }
        });
    }

    private void submitDictationSkip() {
        if (latestState == null) {
            return;
        }
        mobileApiService.skipDictation(
                new ProgressUpdateRequestDto(userSessionStore.isLoggedIn() ? userSessionStore.getUserId() : 0L, viewModel.getCurrentSentenceId())
        ).enqueue(new Callback<DictationResultDto>() {
            @Override
            public void onResponse(@NonNull Call<DictationResultDto> call, @NonNull Response<DictationResultDto> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    viewModel.skip();
                    return;
                }
                DictationFeedback feedback = mapDictationResponse(response.body());
                viewModel.applyDictationFeedback(feedback, false, true);
            }

            @Override
            public void onFailure(@NonNull Call<DictationResultDto> call, @NonNull Throwable throwable) {
                viewModel.skip();
            }
        });
    }

    private DictationFeedback mapDictationResponse(@NonNull DictationResultDto dto) {
        String fullAnswer = dto.correctSentence == null || dto.correctSentence.trim().isEmpty()
                ? viewModel.getCurrentSentenceContent()
                : dto.correctSentence;
        String hint = dto.hint == null ? viewModel.getCurrentHintText() : dto.hint;
        String correctWords = dto.correct ? fullAnswer : "";
        String maskedWords = dto.correct ? "" : buildMaskedWords(fullAnswer, hint);
        return new DictationFeedback(
                dto.correct,
                dto.message == null || dto.message.trim().isEmpty()
                        ? (dto.correct ? "Correct! Well done." : "Almost there. Use the hint and try again.")
                        : dto.message,
                fullAnswer,
                correctWords,
                hint == null ? "" : hint,
                maskedWords
        );
    }

    private String buildMaskedWords(String sentence, String hint) {
        if (sentence == null || sentence.trim().isEmpty()) {
            return "";
        }
        String[] words = sentence.trim().split("\\s+");
        String normalizedHint = hint == null ? "" : hint.trim().toLowerCase(Locale.US);
        StringBuilder builder = new StringBuilder();
        for (String word : words) {
            if (!normalizedHint.isEmpty() && word.toLowerCase(Locale.US).contains(normalizedHint)) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            int stars = Math.max(3, Math.min(word.length(), 8));
            for (int i = 0; i < stars; i++) {
                builder.append('*');
            }
        }
        return builder.toString();
    }

    private void writeAudioToWaveFile(File outputFile) {
        byte[] buffer = new byte[audioBufferSize];
        long totalAudioLen = 0L;
        try (RandomAccessFile raf = new RandomAccessFile(outputFile, "rw")) {
            raf.seek(44L);
            while (isRecordingAudio && audioRecord != null) {
                int read = audioRecord.read(buffer, 0, buffer.length);
                if (read > 0) {
                    raf.write(buffer, 0, read);
                    totalAudioLen += read;
                }
            }
            updateWaveHeader(raf, totalAudioLen);
        } catch (IOException ignored) {
        }
    }

    private void writeEmptyWaveHeader(File outputFile) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(outputFile, "rw")) {
            byte[] header = new byte[44];
            header[0] = 'R';
            header[1] = 'I';
            header[2] = 'F';
            header[3] = 'F';
            header[8] = 'W';
            header[9] = 'A';
            header[10] = 'V';
            header[11] = 'E';
            header[12] = 'f';
            header[13] = 'm';
            header[14] = 't';
            header[15] = ' ';
            header[16] = 16;
            header[20] = 1;
            header[22] = 1;
            writeIntLE(header, 24, SPEAKING_SAMPLE_RATE);
            writeIntLE(header, 28, SPEAKING_SAMPLE_RATE * 2);
            header[32] = 2;
            header[34] = 16;
            header[36] = 'd';
            header[37] = 'a';
            header[38] = 't';
            header[39] = 'a';
            raf.write(header);
        }
    }

    private void updateWaveHeader(RandomAccessFile raf, long totalAudioLen) throws IOException {
        long totalDataLen = totalAudioLen + 36L;
        raf.seek(4L);
        writeIntLE(raf, totalDataLen);
        raf.seek(40L);
        writeIntLE(raf, totalAudioLen);
    }

    private void writeIntLE(byte[] target, int offset, long value) {
        target[offset] = (byte) (value & 0xff);
        target[offset + 1] = (byte) ((value >> 8) & 0xff);
        target[offset + 2] = (byte) ((value >> 16) & 0xff);
        target[offset + 3] = (byte) ((value >> 24) & 0xff);
    }

    private void writeIntLE(RandomAccessFile raf, long value) throws IOException {
        raf.write((byte) (value & 0xff));
        raf.write((byte) ((value >> 8) & 0xff));
        raf.write((byte) ((value >> 16) & 0xff));
        raf.write((byte) ((value >> 24) & 0xff));
    }
}
