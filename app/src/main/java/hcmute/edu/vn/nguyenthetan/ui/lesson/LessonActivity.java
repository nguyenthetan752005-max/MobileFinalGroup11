package hcmute.edu.vn.nguyenthetan.ui.lesson;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.tabs.TabLayout;

import hcmute.edu.vn.nguyenthetan.R;
import hcmute.edu.vn.nguyenthetan.TungTungApplication;
import hcmute.edu.vn.nguyenthetan.databinding.ActivityLessonBinding;
import hcmute.edu.vn.nguyenthetan.domain.model.lesson.SentenceStatus;
import hcmute.edu.vn.nguyenthetan.domain.model.comment.Comment;


public class LessonActivity extends AppCompatActivity implements TranscriptAdapter.Listener {

    private static final String EXTRA_LESSON_ID = "extra_lesson_id";

    private ActivityLessonBinding binding;
    private LessonViewModel viewModel;
    private TranscriptAdapter transcriptAdapter;
    private CommentsAdapter commentsAdapter;

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

        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(getString(R.string.lesson_dictation)));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(getString(R.string.lesson_transcript)));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText(getString(R.string.lesson_speaking)));

        transcriptAdapter = new TranscriptAdapter(this);
        commentsAdapter = new CommentsAdapter();
        binding.recyclerTranscript.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerTranscript.setAdapter(transcriptAdapter);
        binding.recyclerComments.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerComments.setAdapter(commentsAdapter);

        long lessonId = getIntent().getLongExtra(EXTRA_LESSON_ID, 0L);
        TungTungApplication application = (TungTungApplication) getApplication();
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
        binding.buttonPlay.setOnClickListener(v -> viewModel.togglePlayback());
        binding.buttonReplay.setOnClickListener(v -> viewModel.replay());
        binding.buttonCheck.setOnClickListener(v -> viewModel.checkAnswer());
        binding.buttonSkip.setOnClickListener(v -> viewModel.skip());
        binding.buttonNext.setOnClickListener(v -> viewModel.nextSentence());
        binding.buttonTranscriptPrev.setOnClickListener(v -> viewModel.previousSentence());
        binding.buttonTranscriptPlay.setOnClickListener(v -> viewModel.togglePlayback());
        binding.buttonTranscriptNext.setOnClickListener(v -> viewModel.nextSentence());
        binding.buttonRecord.setOnClickListener(v -> viewModel.toggleRecording());
        binding.buttonTryAgain.setOnClickListener(v -> viewModel.retrySpeaking());
        binding.buttonSpeakingNext.setOnClickListener(v -> viewModel.nextSentence());
        binding.buttonWriteComment.setOnClickListener(v -> Toast.makeText(this, "Comment composer will be connected after backend wiring.", Toast.LENGTH_SHORT).show());

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
                viewModel.selectTab(tab == null ? 0 : tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        viewModel.getUiState().observe(this, this::render);
        viewModel.load();
    }

    private void render(@NonNull LessonViewModel.UiState state) {
        binding.textLessonTitle.setText(state.title);
        binding.textLessonSubtitle.setText(state.subtitle);
        binding.chipSentenceCounter.setText(state.sentenceCounter);
        binding.progressLesson.setProgressCompat(state.progressPercent, true);
        applyStatusChip(state.currentStatus);

        binding.textHint.setText(state.hint);
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
        binding.buttonNext.setVisibility(state.showNextButton && state.selectedTab == 0 ? View.VISIBLE : View.GONE);

        binding.textTranscriptSentence.setText(state.transcriptSentence);
        binding.textPlaybackTime.setText(state.playbackTime);
        binding.textTranscriptPlaybackTime.setText(state.playbackTime);
        binding.progressPlayback.setProgressCompat(state.playbackPercent, true);
        binding.progressTranscript.setProgressCompat(state.playbackPercent, true);
        binding.buttonPlay.setImageResource(state.playing ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
        binding.buttonTranscriptPlay.setImageResource(state.playing ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
        transcriptAdapter.submitList(state.transcriptRows);

        binding.textSpeakingReference.setText(state.speakingReference);
        binding.textRecordingState.setText(state.recordingLabel);
        binding.buttonRecord.setImageResource(state.recording ? android.R.drawable.ic_media_pause : android.R.drawable.ic_btn_speak_now);
        binding.textBestScore.setText(state.bestScore);
        binding.textBestTranscript.setText(state.bestTranscript);
        binding.textCurrentScore.setText(state.currentScore);
        binding.textCurrentTranscript.setText(state.currentTranscript);
        binding.buttonSpeakingNext.setEnabled(state.speakingNextEnabled);
        commentsAdapter.submitList(state.comments);

        binding.dictationContainer.setVisibility(state.selectedTab == 0 ? View.VISIBLE : View.GONE);
        binding.transcriptContainer.setVisibility(state.selectedTab == 1 ? View.VISIBLE : View.GONE);
        binding.speakingContainer.setVisibility(state.selectedTab == 2 ? View.VISIBLE : View.GONE);
        binding.commentsCard.setVisibility(state.selectedTab == 1 ? View.GONE : View.VISIBLE);
    }

    private void applyStatusChip(SentenceStatus status) {
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
        viewModel.selectSentence(position);
    }

    @Override
    public void onPlayRow(int position) {
        viewModel.playTranscriptRow(position);
    }
}
