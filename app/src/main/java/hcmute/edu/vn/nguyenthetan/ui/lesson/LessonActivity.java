package hcmute.edu.vn.nguyenthetan.ui.lesson;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.tabs.TabLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.nguyenthetan.R;
import hcmute.edu.vn.nguyenthetan.TungTungApplication;
import hcmute.edu.vn.nguyenthetan.core.ThemeColorResolver;
import hcmute.edu.vn.nguyenthetan.core.UserSessionStore;
import hcmute.edu.vn.nguyenthetan.databinding.ActivityLessonBinding;
import hcmute.edu.vn.nguyenthetan.databinding.DialogCommentComposeBinding;
import hcmute.edu.vn.nguyenthetan.domain.model.lesson.SentenceStatus;
import hcmute.edu.vn.nguyenthetan.ui.auth.AccountLockUiHandler;
import hcmute.edu.vn.nguyenthetan.ui.common.ThemedActivity;
import hcmute.edu.vn.nguyenthetan.ui.lesson.manager.AudioPlaybackManager;
import hcmute.edu.vn.nguyenthetan.ui.lesson.manager.AudioRecordingManager;
import hcmute.edu.vn.nguyenthetan.ui.lesson.manager.YouTubePlaybackManager;
import hcmute.edu.vn.nguyenthetan.work.AudioDownloadWorker;

public class LessonActivity extends ThemedActivity implements TranscriptAdapter.Listener {

    private static final String EXTRA_LESSON_ID = "extra_lesson_id";
    private static final String EXTRA_TARGET_SENTENCE_ID = "extra_target_sentence_id";
    private static final int RECORD_AUDIO_REQUEST_CODE = 7001;

    private final CommentsAdapter.CommentActionListener commentActionListener = new CommentsAdapter.CommentActionListener() {
        @Override
        public void onLike(long commentId) {
            if (!userSessionStore.isLoggedIn()) {
                requireLoginForRestrictedFeature();
                return;
            }
            viewModel.voteComment(commentId, true);
        }

        @Override
        public void onDislike(long commentId) {
            if (!userSessionStore.isLoggedIn()) {
                requireLoginForRestrictedFeature();
                return;
            }
            viewModel.voteComment(commentId, false);
        }

        @Override
        public void onReply(long commentId, String authorName) {
            if (!userSessionStore.isLoggedIn()) {
                requireLoginForRestrictedFeature();
                return;
            }
            showCommentComposer(commentId, authorName);
        }

        @Override
        public void onDelete(long commentId) {
            if (!userSessionStore.isLoggedIn()) {
                requireLoginForRestrictedFeature();
                return;
            }
            new AlertDialog.Builder(LessonActivity.this)
                    .setTitle(R.string.comment_delete_confirm_title)
                    .setMessage(R.string.comment_delete_confirm_message)
                    .setPositiveButton(R.string.action_delete, (dialog, which) -> viewModel.deleteComment(commentId))
                    .setNegativeButton(R.string.action_cancel, null)
                    .show();
        }
    };

    private ActivityLessonBinding binding;
    private LessonViewModel viewModel;
    private TranscriptAdapter transcriptAdapter;
    private CommentsAdapter commentsAdapter;
    private UserSessionStore userSessionStore;

    private final List<Integer> visibleTabs = new ArrayList<>();
    private long lessonId;
    private LessonViewModel.UiState latestState;
    private boolean updatingTabs;
    private boolean audioPrefetchEnqueued;
    private boolean initialSpeakingFetched;
    private long sessionStartTime;

    private AudioPlaybackManager audioPlaybackManager;
    private YouTubePlaybackManager youtubePlaybackManager;
    private AudioRecordingManager audioRecordingManager;
    private final Handler autoAdvanceHandler = new Handler(Looper.getMainLooper());
    private Runnable pendingAutoAdvanceRunnable;

    public static Intent newIntent(Context context, long lessonId) {
        return newIntent(context, lessonId, -1L);
    }

    public static Intent newIntent(Context context, long lessonId, long targetSentenceId) {
        Intent intent = new Intent(context, LessonActivity.class);
        intent.putExtra(EXTRA_LESSON_ID, lessonId);
        intent.putExtra(EXTRA_TARGET_SENTENCE_ID, targetSentenceId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLessonBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        AccountLockUiHandler.attach(this);

        TungTungApplication application = (TungTungApplication) getApplication();
        userSessionStore = application.getAppContainer().getUserSessionStore();

        lessonId = getIntent().getLongExtra(EXTRA_LESSON_ID, 0L);

        LessonViewModelFactory factory = new LessonViewModelFactory(
                application.getAppContainer().getLessonSessionUseCase(),
                application.getAppContainer().getLessonProgressUseCase(),
                application.getAppContainer().getSyncLessonProgressUseCase(),
                application.getAppContainer().getCommentsUseCase(),
                application.getAppContainer().getSyncSentenceCommentsUseCase(),
                application.getAppContainer().getCheckDictationAnswerUseCase(),
                application.getAppContainer().getSaveSentenceStatusUseCase(),
                application.getAppContainer().getSaveSpeakingAttemptUseCase(),
                application.getAppContainer().getCheckDictationOnlineUseCase(),
                application.getAppContainer().getSkipDictationUseCase(),
                application.getAppContainer().getEvaluateSpeakingUseCase(),
                application.getAppContainer().getSpeakingResultsUseCase(),
                application.getAppContainer().getTrackLessonTimeUseCase(),
                application.getAppContainer().getAddCommentUseCase(),
                application.getAppContainer().getVoteCommentUseCase(),
                application.getAppContainer().getDeleteCommentUseCase(),
                userSessionStore,
                lessonId,
                userSessionStore != null && userSessionStore.isLoggedIn()
        );
        viewModel = new ViewModelProvider(this, factory).get(LessonViewModel.class);

        initializeManagers();

        transcriptAdapter = new TranscriptAdapter(this);
        transcriptAdapter.setShowStatus(userSessionStore != null && userSessionStore.isLoggedIn());
        commentsAdapter = new CommentsAdapter();
        commentsAdapter.setCurrentUserId(userSessionStore.getUserId());
        commentsAdapter.setActionListener(commentActionListener);
        binding.recyclerTranscript.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerTranscript.setAdapter(transcriptAdapter);
        binding.recyclerComments.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerComments.setAdapter(commentsAdapter);

        setupClickListeners();
        setupTextWatchers();

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (updatingTabs || tab == null) return;
                int position = tab.getPosition();
                if (position >= 0 && position < visibleTabs.size()) {
                    viewModel.selectTab(visibleTabs.get(position));
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        viewModel.getUiState().observe(this, state -> {
            boolean wasTranscriptSelected = latestState != null && latestState.selectedTab == LessonViewModel.TAB_TRANSCRIPT;
            long previousSentenceId = latestState != null ? latestState.currentSentenceId : -1L;
            LessonViewModel.UiState previousState = latestState;
            render(state);
            maybeAutoAdvanceAfterCompletion(previousState, state);
            if (state.currentSentenceId > 0 && state.showSpeakingTab
                    && userSessionStore.isLoggedIn() && !initialSpeakingFetched) {
                initialSpeakingFetched = true;
                viewModel.fetchSpeakingResultsForCurrentSentence();
            }
            
            boolean tabJustSelected = state.selectedTab == LessonViewModel.TAB_TRANSCRIPT && !wasTranscriptSelected;
            boolean sentenceChanged = state.selectedTab == LessonViewModel.TAB_TRANSCRIPT && previousSentenceId != -1L && previousSentenceId != state.currentSentenceId;
            boolean shouldAutoScroll = viewModel.isAutoScrollTranscriptMode();
            
            if (state.transcriptRows != null && (tabJustSelected || (sentenceChanged && shouldAutoScroll))) {
                int targetIndex = -1;
                for (int i = 0; i < state.transcriptRows.size(); i++) {
                    if (state.transcriptRows.get(i).selected) {
                        targetIndex = i;
                        break;
                    }
                }
                if (targetIndex >= 0) {
                    int finalIndex = targetIndex;
                    binding.scrollMain.post(() -> {
                        if (binding.recyclerTranscript.getLayoutManager() != null) {
                            View child = binding.recyclerTranscript.getLayoutManager().findViewByPosition(finalIndex);
                            if (child != null) {
                                int targetY = binding.transcriptContainer.getTop() + binding.recyclerTranscript.getTop() + child.getTop();
                                int offset = binding.scrollMain.getHeight() / 3;
                                binding.scrollMain.smoothScrollTo(0, Math.max(0, targetY - offset));
                            } else {
                                binding.scrollMain.smoothScrollTo(0, binding.transcriptContainer.getTop() + binding.recyclerTranscript.getTop());
                            }
                        }
                    });
                }
            }
        });
        viewModel.getLoadingState().observe(this, loading ->
                binding.progressLessonLoad.setVisibility(Boolean.TRUE.equals(loading) ? View.VISIBLE : View.GONE)
        );
        viewModel.getCommentEvent().observe(this, this::onCommentEvent);
        application.getAppContainer().getSyncErrorMessage().observe(this, message -> {
            if (message != null && !message.trim().isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        });

        long targetSentenceId = getIntent().getLongExtra(EXTRA_TARGET_SENTENCE_ID, -1L);
        if (targetSentenceId > 0L) {
            viewModel.loadWithTargetSentence(targetSentenceId);
        } else {
            viewModel.load();
        }
    }

    private void onCommentEvent(LessonViewModel.CommentOpResult result) {
        if (result == null) return;
        switch (result) {
            case ADD_SUCCESS:
                Toast.makeText(this, R.string.comment_sent_success, Toast.LENGTH_SHORT).show();
                break;
            case ADD_FAIL:
                Toast.makeText(this, R.string.comment_sent_failed, Toast.LENGTH_SHORT).show();
                break;
            case DELETE_SUCCESS:
                Toast.makeText(this, R.string.comment_deleted, Toast.LENGTH_SHORT).show();
                break;
            case DELETE_FAIL:
                Toast.makeText(this, R.string.comment_delete_failed, Toast.LENGTH_SHORT).show();
                break;
        }
        viewModel.consumeCommentEvent();
    }

    private void initializeManagers() {
        audioPlaybackManager = new AudioPlaybackManager(this, userSessionStore, new AudioPlaybackManager.Listener() {
            @Override
            public void onProgressUpdate(long position, long duration, boolean isPlaying) {
                viewModel.updatePlaybackProgress(position, duration, isPlaying);
            }
            @Override
            public void onPlaybackCompleted(long duration) {
                viewModel.completePlayback(duration);
                handleTranscriptAutoPlay();
            }
            @Override
            public void onPlaybackError(int errorMessageResId) {
                Toast.makeText(LessonActivity.this, errorMessageResId, Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onPlaybackPaused() {
                viewModel.pausePlayback();
            }
        });

        youtubePlaybackManager = new YouTubePlaybackManager(this, this, binding.youtubePlayerView, new YouTubePlaybackManager.Listener() {
            @Override
            public void onPlayerReadyInitSync() {
                if (latestState != null) {
                    youtubePlaybackManager.syncVideoPlayers(latestState.youtubeVideoId, latestState.startTime, latestState.endTime, latestState.currentSentenceId);
                }
            }
            @Override
            public void onPlayerFailed() {
                Toast.makeText(LessonActivity.this, R.string.lesson_video_player_failed, Toast.LENGTH_SHORT).show();
                if (latestState != null && latestState.videoLesson) {
                    youtubePlaybackManager.openYoutubeExternally(latestState.youtubeVideoId, latestState.startTime);
                }
            }
            @Override
            public void onVideoDurationFound(long durationMillis) {
            }
            @Override
            public void onProgressUpdate(long position, long duration, boolean isPlaying) {
                viewModel.updatePlaybackProgress(position, duration, isPlaying);
                if (latestState != null && latestState.videoLesson && !viewModel.isRepeatTranscriptMode() 
                        && latestState.selectedTab == LessonViewModel.TAB_TRANSCRIPT) {
                    viewModel.syncVideoSentence(position);
                }
            }
            @Override
            public void onResetPlayback() {
                viewModel.resetPlayback();
            }
            @Override
            public void onCompletePlayback(long durationMillis) {
                viewModel.completePlayback(durationMillis);
                handleTranscriptAutoPlay();
            }
        });
        youtubePlaybackManager.initialize();

        audioRecordingManager = new AudioRecordingManager(this, new AudioRecordingManager.Listener() {
            @Override
            public void onRecordingStarted() {
                viewModel.setRecordingState(true);
            }
            @Override
            public void onRecordingStopped(File audioFile) {
                viewModel.setRecordingState(false);
                stopRecordingAndEvaluate(audioFile);
            }
            @Override
            public void onRecordingFailed() {
                viewModel.setRecordingState(false);
                viewModel.showSpeakingMessage(getString(R.string.lesson_record_failed));
            }
            @Override
            public void onPermissionDenied() {
                viewModel.setRecordingState(false);
                Toast.makeText(LessonActivity.this, R.string.lesson_record_permission_denied, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupClickListeners() {
        binding.buttonBack.setOnClickListener(v -> finish());
        binding.buttonPlay.setOnClickListener(v -> handlePrimaryMediaAction(false));
        binding.buttonReplay.setOnClickListener(v -> handlePrimaryMediaAction(true));
        binding.buttonCheck.setOnClickListener(v -> {
            cancelPendingAutoAdvance();
            if (latestState == null || safeInput().trim().isEmpty()) return;
            viewModel.submitDictationCheck();
        });
        binding.buttonSkip.setOnClickListener(v -> {
            cancelPendingAutoAdvance();
            releaseLessonMedia(true, false);
            viewModel.submitDictationSkip();
        });
        binding.buttonPrevious.setOnClickListener(v -> {
            cancelPendingAutoAdvance();
            releaseLessonMedia(true, false);
            viewModel.previousSentence();
        });
        binding.buttonNext.setOnClickListener(v -> {
            cancelPendingAutoAdvance();
            releaseLessonMedia(true, false);
            viewModel.nextSentence();
        });
        binding.buttonTranscriptPrev.setOnClickListener(v -> {
            cancelPendingAutoAdvance();
            releaseLessonMedia(true, false);
            viewModel.previousSentence();
        });
        binding.buttonTranscriptNext.setOnClickListener(v -> {
            cancelPendingAutoAdvance();
            releaseLessonMedia(true, false);
            viewModel.nextSentence();
        });
        binding.buttonSpeakingPlay.setOnClickListener(v -> handlePrimaryMediaAction(false));
        binding.buttonSpeakingPrevious.setOnClickListener(v -> {
            cancelPendingAutoAdvance();
            releaseLessonMedia(true, false);
            viewModel.previousSentence();
        });
        binding.buttonSpeakingSkip.setOnClickListener(v -> {
            cancelPendingAutoAdvance();
            releaseLessonMedia(true, false);
            viewModel.skipSpeakingSentence();
        });
        binding.buttonRecord.setOnClickListener(v -> handleRecordAction());
        binding.buttonPlayBestAudio.setOnClickListener(v -> {
            if (latestState != null) audioPlaybackManager.playUserAudio(latestState.bestAudioUrl);
        });
        binding.buttonPlayCurrentAudio.setOnClickListener(v -> {
            if (latestState != null) audioPlaybackManager.playUserAudio(latestState.currentUserAudioUrl);
        });
        binding.buttonSpeakingNext.setOnClickListener(v -> {
            cancelPendingAutoAdvance();
            releaseLessonMedia(true, false);
            viewModel.nextSentence();
        });
        binding.buttonWriteComment.setOnClickListener(v -> {
            if (!userSessionStore.isLoggedIn()) {
                requireLoginForRestrictedFeature();
                return;
            }
            showCommentComposer(null, null);
        });
        binding.checkboxRepeat.setOnCheckedChangeListener((buttonView, isChecked) ->
                viewModel.setRepeatTranscriptMode(isChecked));
        binding.checkboxAutoScroll.setChecked(viewModel.isAutoScrollTranscriptMode());
        binding.checkboxAutoScroll.setOnCheckedChangeListener((buttonView, isChecked) ->
                viewModel.setAutoScrollTranscriptMode(isChecked));
    }

    private void setupTextWatchers() {
        binding.inputDictation.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                viewModel.onInputChanged(s == null ? "" : s.toString());
            }
        });
    }

    private void render(@NonNull LessonViewModel.UiState state) {
        if (state.title == null) return;

        long previousSentenceId = latestState == null ? -1L : latestState.currentSentenceId;
        latestState = state;

        if (previousSentenceId != -1L && previousSentenceId != state.currentSentenceId) {
            boolean isContinuousVideoPlay = state.videoLesson && !viewModel.isRepeatTranscriptMode() && state.selectedTab == LessonViewModel.TAB_TRANSCRIPT;
            if (!isContinuousVideoPlay) {
                releaseLessonMedia(false, false);
            }
            if (state.showSpeakingTab && userSessionStore.isLoggedIn() && state.currentSentenceId > 0L) {
                viewModel.fetchSpeakingResultsForCurrentSentence();
            }
        }

        if (state.videoLesson) {
            youtubePlaybackManager.ensureInitialized(state.youtubeVideoId, state.startTime, state.currentSentenceId);
            if (!state.playing) {
                youtubePlaybackManager.syncVideoPlayers(state.youtubeVideoId, state.startTime, state.endTime, state.currentSentenceId);
            }
        } else {
            youtubePlaybackManager.stopVideoPlaybackCallbacks();
            youtubePlaybackManager.pauseYoutubePlayer();
        }

        updateTabs(state);

        binding.textLessonTitle.setText(state.title);
        binding.textLessonSubtitle.setText(state.subtitle);
        binding.chipSentenceCounter.setText(state.sentenceCounter);
        if (viewModel.isLoggedIn()) {
            binding.progressLesson.setVisibility(View.VISIBLE);
            binding.progressLesson.setProgressCompat(state.progressPercent, true);
            binding.chipStatus.setVisibility(View.VISIBLE);
            applyStatusChip(state.currentStatus);
        } else {
            binding.progressLesson.setVisibility(View.GONE);
            binding.chipStatus.setVisibility(View.GONE);
        }

        binding.textHint.setText(state.hint);
        binding.textHint.setVisibility(state.hint != null && !state.hint.isEmpty() ? View.VISIBLE : View.GONE);
        binding.textMediaSummary.setText(state.mediaSummary);
        binding.textMediaSummary.setVisibility(state.mediaSummary != null && !state.mediaSummary.isEmpty() ? View.VISIBLE : View.GONE);
        binding.textTranscriptMediaSummary.setText(state.mediaSummary);
        binding.textTranscriptMediaSummary.setVisibility(state.mediaSummary != null && !state.mediaSummary.isEmpty() ? View.VISIBLE : View.GONE);

        if (!safeInput().equals(state.inputText)) {
            binding.inputDictation.setText(state.inputText);
            if (binding.inputDictation.getText() != null) {
                binding.inputDictation.setSelection(binding.inputDictation.getText().length());
            }
        }

        binding.cardFeedback.setVisibility(state.showFeedback ? View.VISIBLE : View.GONE);
        binding.textFeedbackTitle.setText(state.feedbackTitle);
        setTextOrHide(binding.textCorrectWords, state.correctWords);
        setTextOrHide(binding.textNewHint, state.newHint);
        setTextOrHide(binding.textMaskedWords, state.maskedWords);
        binding.buttonPrevious.setVisibility(state.hasPreviousSentence && state.selectedTab == LessonViewModel.TAB_DICTATION ? View.VISIBLE : View.GONE);
        binding.buttonNext.setVisibility(state.showNextButton && state.selectedTab == LessonViewModel.TAB_DICTATION ? View.VISIBLE : View.GONE);

        binding.textTranscriptSentence.setText(state.transcriptSentence);
        binding.textPlaybackTime.setText(state.videoLesson ? state.playbackLabel : state.playbackTime);
        binding.textTranscriptPlaybackTime.setText(state.videoLesson ? state.playbackLabel : state.playbackTime);
        binding.progressPlayback.setProgressCompat(state.playbackPercent, true);
        binding.progressTranscript.setProgressCompat(state.playbackPercent, true);
        binding.progressPlayback.setVisibility(state.videoLesson ? View.GONE : View.VISIBLE);
        binding.progressTranscript.setVisibility(state.videoLesson ? View.GONE : View.VISIBLE);
        binding.buttonReplay.setVisibility(state.videoLesson ? View.GONE : View.VISIBLE);
        binding.videoPlayerCard.setVisibility(state.videoLesson && youtubePlaybackManager.supportsEmbeddedYoutubePlayback() ? View.VISIBLE : View.GONE);
        binding.buttonTranscriptPrev.setVisibility(state.hasPreviousSentence ? View.VISIBLE : View.INVISIBLE);
        binding.buttonTranscriptNext.setVisibility(state.hasNextSentence ? View.VISIBLE : View.INVISIBLE);

        binding.buttonPlay.setImageResource(state.playing ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
        binding.buttonSpeakingPlay.setImageResource(state.playing ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
        binding.buttonPlay.setEnabled(state.videoLesson || state.canPlayAudio);
        binding.buttonReplay.setEnabled(state.canPlayAudio);
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
        binding.buttonSpeakingPrevious.setVisibility(state.hasPreviousSentence ? View.VISIBLE : View.GONE);
        binding.buttonSpeakingSkip.setVisibility(state.showSpeakingSkipButton ? View.VISIBLE : View.GONE);
        binding.buttonSpeakingNext.setVisibility(state.showSpeakingNextButton ? View.VISIBLE : View.GONE);
        binding.buttonSpeakingNext.setEnabled(state.showSpeakingNextButton);
        binding.textCommentsTitle.setText(state.commentCount == 0
                ? getString(R.string.lesson_comments_zero)
                : getResources().getQuantityString(
                R.plurals.lesson_comments_count,
                state.commentCount,
                state.commentCount
        ));
        binding.buttonWriteComment.setText(userSessionStore.isLoggedIn() ? R.string.label_write_comment : R.string.label_sign_in_to_comment);
        commentsAdapter.submitList(state.comments);

        binding.dictationContainer.setVisibility(state.selectedTab == LessonViewModel.TAB_DICTATION ? View.VISIBLE : View.GONE);
        binding.transcriptContainer.setVisibility(state.selectedTab == LessonViewModel.TAB_TRANSCRIPT ? View.VISIBLE : View.GONE);
        binding.speakingContainer.setVisibility(state.selectedTab == LessonViewModel.TAB_SPEAKING ? View.VISIBLE : View.GONE);
        binding.commentsCard.setVisibility(state.selectedTab == LessonViewModel.TAB_TRANSCRIPT ? View.GONE : View.VISIBLE);
    }

    private void setTextOrHide(@NonNull TextView view, String value) {
        boolean visible = value != null && !value.trim().isEmpty();
        view.setVisibility(visible ? View.VISIBLE : View.GONE);
        if (visible) {
            view.setText(value);
        }
    }

    private void applyStatusChip(SentenceStatus status) {
        if (status == null) {
            binding.chipStatus.setText("LOADING");
            binding.chipStatus.setChipBackgroundColor(ColorStateList.valueOf(
                    ThemeColorResolver.resolveColor(this, R.attr.ttColorBorder)
            ));
            binding.chipStatus.setTextColor(ThemeColorResolver.resolveColor(this, R.attr.ttColorTextSecondary));
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
            background = ThemeColorResolver.resolveColor(this, R.attr.ttColorBorder);
            textColor = ThemeColorResolver.resolveColor(this, R.attr.ttColorTextSecondary);
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
        cancelPendingAutoAdvance();
        releaseLessonMedia(true, false);
        viewModel.selectSentence(position);
    }

    @Override
    public void onPlayRow(int position, boolean isCurrentlyPlaying) {
        if (isCurrentlyPlaying) {
            handlePrimaryMediaAction(false);
        } else {
            cancelPendingAutoAdvance();
            releaseLessonMedia(true, false);
            viewModel.selectSentence(position);
            binding.getRoot().post(() -> handlePrimaryMediaAction(true));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        sessionStartTime = System.currentTimeMillis();
    }

    @Override
    protected void onPause() {
        cancelPendingAutoAdvance();
        releaseLessonMedia(true, false);
        trackTimeSpent();
        super.onPause();
    }

    private void trackTimeSpent() {
        if (sessionStartTime > 0) {
            viewModel.trackSessionTime(System.currentTimeMillis() - sessionStartTime);
        }
        sessionStartTime = 0;
    }

    @Override
    protected void onDestroy() {
        cancelPendingAutoAdvance();
        releaseLessonMedia(false, false);
        if (youtubePlaybackManager != null) {
            youtubePlaybackManager.release();
        }
        binding.youtubePlayerView.release();
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RECORD_AUDIO_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                audioRecordingManager.startRecording();
            } else {
                Toast.makeText(this, R.string.lesson_record_permission_denied, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateTabs(@NonNull LessonViewModel.UiState state) {
        List<Integer> desiredTabs = new ArrayList<>();
        if (state.showDictationTab) desiredTabs.add(LessonViewModel.TAB_DICTATION);
        if (state.showSpeakingTab) desiredTabs.add(LessonViewModel.TAB_SPEAKING);
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
        if (selectedPosition < 0) selectedPosition = 0;
        TabLayout.Tab selectedTab = binding.tabLayout.getTabAt(selectedPosition);
        if (selectedTab != null && !selectedTab.isSelected()) {
            selectedTab.select();
        }
    }

    private String resolveTabTitle(int tabId) {
        if (tabId == LessonViewModel.TAB_DICTATION) return getString(R.string.lesson_dictation);
        if (tabId == LessonViewModel.TAB_SPEAKING) return getString(R.string.lesson_speaking);
        return getString(R.string.lesson_transcript);
    }

    private void handlePrimaryMediaAction(boolean replayRequested) {
        if (latestState == null) return;
        if (latestState.videoLesson) {
            if (!youtubePlaybackManager.supportsEmbeddedYoutubePlayback()
                    || !youtubePlaybackManager.isYoutubePlayerReady()) {
                youtubePlaybackManager.openYoutubeExternally(latestState.youtubeVideoId, latestState.startTime);
                return;
            }
            boolean loopAtEnd = false;
            Double effectiveEndTime = null;
            if (latestState.selectedTab == LessonViewModel.TAB_DICTATION || latestState.selectedTab == LessonViewModel.TAB_SPEAKING) {
                effectiveEndTime = latestState.endTime;
                loopAtEnd = false;
            } else if (viewModel.isRepeatTranscriptMode()) {
                effectiveEndTime = latestState.endTime;
                loopAtEnd = true;
            }
            youtubePlaybackManager.toggleVideoPlayback(latestState.youtubeVideoId, latestState.startTime, effectiveEndTime, latestState.currentSentenceId, replayRequested, latestState.playing, loopAtEnd);
        } else {
            if (latestState.currentAudioUrl == null || latestState.currentAudioUrl.trim().isEmpty()) {
                Toast.makeText(this, R.string.lesson_no_audio_source, Toast.LENGTH_SHORT).show();
                return;
            }
            audioPlaybackManager.toggleAudioPlayback(latestState.currentSentenceId, latestState.currentAudioUrl, replayRequested);
        }
    }

    private void handleRecordAction() {
        if (latestState == null) return;
        if (!userSessionStore.isLoggedIn()) {
            requireLoginForRestrictedFeature();
            return;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    RECORD_AUDIO_REQUEST_CODE
            );
            return;
        }
        if (audioRecordingManager.isRecording()) {
            audioRecordingManager.stopRecording();
        } else {
            audioRecordingManager.startRecording();
        }
    }

    private void requireLoginForRestrictedFeature() {
        new android.app.AlertDialog.Builder(this)
                .setTitle(R.string.dialog_login_required_title)
                .setMessage(R.string.dialog_login_required_message)
                .setPositiveButton(R.string.dialog_login_required_positive, (dialog, which) -> {
                    startActivity(new Intent(this, hcmute.edu.vn.nguyenthetan.ui.onboarding.OnboardingActivity.class));
                })
                .setNegativeButton(R.string.dialog_login_required_negative, null)
                .show();
    }

    private void stopRecordingAndEvaluate(File audioFile) {
        if (audioFile == null || !audioFile.exists() || latestState == null) {
            viewModel.showSpeakingMessage(getString(R.string.lesson_record_failed));
            return;
        }
        viewModel.evaluateSpeaking(
                audioFile,
                latestState.speakingReference,
                latestState.currentSentenceId,
                getString(R.string.lesson_evaluating_speaking),
                getString(R.string.lesson_speaking_failed)
        );
    }

    private void releaseLessonMedia(boolean notifyViewModel, boolean deletePendingRecording) {
        if (youtubePlaybackManager != null) {
            youtubePlaybackManager.stopVideoPlaybackCallbacks();
            youtubePlaybackManager.pauseYoutubePlayer();
        }
        if (audioPlaybackManager != null) {
            audioPlaybackManager.releaseAudioPlayer(notifyViewModel, true);
        }
        if (audioRecordingManager != null) {
            audioRecordingManager.releaseRecorder(deletePendingRecording);
        }
        if (notifyViewModel && latestState != null && latestState.videoLesson) {
            viewModel.pausePlayback();
        }
    }

    private void maybeAutoAdvanceAfterCompletion(LessonViewModel.UiState previousState, LessonViewModel.UiState currentState) {
        if (previousState == null || currentState == null) {
            return;
        }
        if (previousState.currentSentenceId != currentState.currentSentenceId) {
            cancelPendingAutoAdvance();
            return;
        }
        if (currentState.currentStatus != SentenceStatus.COMPLETED
                || previousState.currentStatus == SentenceStatus.COMPLETED
                || !currentState.hasNextSentence) {
            return;
        }
        if (currentState.selectedTab != LessonViewModel.TAB_DICTATION) {
            return;
        }
        scheduleAutoAdvance();
    }

    private void scheduleAutoAdvance() {
        cancelPendingAutoAdvance();
        pendingAutoAdvanceRunnable = () -> {
            if (isFinishing() || latestState == null || latestState.currentStatus != SentenceStatus.COMPLETED) {
                return;
            }
            releaseLessonMedia(true, false);
            viewModel.nextSentence();
        };
        autoAdvanceHandler.postDelayed(pendingAutoAdvanceRunnable, 900L);
    }

    private void cancelPendingAutoAdvance() {
        if (pendingAutoAdvanceRunnable != null) {
            autoAdvanceHandler.removeCallbacks(pendingAutoAdvanceRunnable);
            pendingAutoAdvanceRunnable = null;
        }
    }

    private void handleTranscriptAutoPlay() {
        if (viewModel.getSelectedTab() != LessonViewModel.TAB_TRANSCRIPT) return;
        if (viewModel.isRepeatTranscriptMode()) {
            binding.getRoot().postDelayed(() -> handlePrimaryMediaAction(true), 200);
        } else if (latestState != null && latestState.hasNextSentence) {
            releaseLessonMedia(true, false);
            viewModel.nextSentence();
            binding.getRoot().postDelayed(() -> handlePrimaryMediaAction(true), 200);
        }
    }

    private void scheduleAudioPrefetchIfNeeded(@NonNull LessonViewModel.UiState state) {
        if (audioPrefetchEnqueued || lessonId <= 0L || state.videoLesson) return;
        audioPrefetchEnqueued = true;
        AudioDownloadWorker.enqueue(this, lessonId);
    }

    private void showCommentComposer(Long parentCommentId, String replyingToAuthor) {
        if (latestState == null) return;

        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.Theme_TungTung);
        DialogCommentComposeBinding dialogBinding = DialogCommentComposeBinding.inflate(getLayoutInflater());
        dialog.setContentView(dialogBinding.getRoot());

        if (replyingToAuthor != null) {
            dialogBinding.textReplyingTo.setVisibility(View.VISIBLE);
            dialogBinding.textReplyingTo.setText(getString(R.string.comment_replying_to, replyingToAuthor));
            dialogBinding.inputCommentContent.setText("@[" + replyingToAuthor + "] ");
            dialogBinding.inputCommentContent.setSelection(dialogBinding.inputCommentContent.getText().length());
        }

        dialogBinding.buttonCancelComment.setOnClickListener(v -> dialog.dismiss());
        dialogBinding.buttonSendComment.setOnClickListener(v -> {
            String content = dialogBinding.inputCommentContent.getText() != null ?
                    dialogBinding.inputCommentContent.getText().toString().trim() : "";
            if (content.isEmpty()) return;
            dialog.dismiss();
            viewModel.submitComment(parentCommentId, content);
        });
        dialog.show();
        dialogBinding.inputCommentContent.requestFocus();
    }
}

