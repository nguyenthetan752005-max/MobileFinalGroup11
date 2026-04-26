package hcmute.edu.vn.nguyenthetan.data.repository;

import android.net.Uri;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;

import hcmute.edu.vn.nguyenthetan.core.UserSessionStore;
import hcmute.edu.vn.nguyenthetan.data.local.dao.user.AppSettingsDao;
import hcmute.edu.vn.nguyenthetan.data.local.dao.catalog.CategoryDao;
import hcmute.edu.vn.nguyenthetan.data.local.dao.lesson.GuestProgressDao;
import hcmute.edu.vn.nguyenthetan.data.local.dao.lesson.LessonDao;
import hcmute.edu.vn.nguyenthetan.data.local.dao.catalog.SectionDao;
import hcmute.edu.vn.nguyenthetan.data.local.dao.lesson.SentenceDao;
import hcmute.edu.vn.nguyenthetan.data.local.entity.user.AppSettingsEntity;
import hcmute.edu.vn.nguyenthetan.data.local.entity.catalog.CategoryEntity;
import hcmute.edu.vn.nguyenthetan.data.local.entity.lesson.GuestSentenceProgressEntity;
import hcmute.edu.vn.nguyenthetan.data.local.entity.lesson.LessonEntity;
import hcmute.edu.vn.nguyenthetan.data.local.entity.catalog.SectionEntity;
import hcmute.edu.vn.nguyenthetan.data.local.entity.lesson.SentenceEntity;
import hcmute.edu.vn.nguyenthetan.data.remote.sync.RemoteLessonSyncManager;
import hcmute.edu.vn.nguyenthetan.data.remote.sync.RemoteProgressSyncManager;
import hcmute.edu.vn.nguyenthetan.data.repository.support.RepositoryFormatters;
import hcmute.edu.vn.nguyenthetan.domain.model.lesson.LessonProgress;
import hcmute.edu.vn.nguyenthetan.domain.model.lesson.LessonSession;
import hcmute.edu.vn.nguyenthetan.domain.model.lesson.Sentence;
import hcmute.edu.vn.nguyenthetan.domain.model.lesson.SentenceStatus;
import hcmute.edu.vn.nguyenthetan.domain.model.lesson.SpeakingAttempt;
import hcmute.edu.vn.nguyenthetan.domain.repository.LessonRepository;

public class RoomLessonRepository implements LessonRepository {

    private final LessonDao lessonDao;
    private final SentenceDao sentenceDao;
    private final GuestProgressDao guestProgressDao;
    private final AppSettingsDao appSettingsDao;
    private final SectionDao sectionDao;
    private final CategoryDao categoryDao;
    private final RemoteLessonSyncManager remoteLessonSyncManager;
    private final RemoteProgressSyncManager remoteProgressSyncManager;
    private final UserSessionStore userSessionStore;

    public RoomLessonRepository(
            LessonDao lessonDao,
            SentenceDao sentenceDao,
            GuestProgressDao guestProgressDao,
            AppSettingsDao appSettingsDao,
            SectionDao sectionDao,
            CategoryDao categoryDao,
            RemoteLessonSyncManager remoteLessonSyncManager,
            RemoteProgressSyncManager remoteProgressSyncManager,
            UserSessionStore userSessionStore
    ) {
        this.lessonDao = lessonDao;
        this.sentenceDao = sentenceDao;
        this.guestProgressDao = guestProgressDao;
        this.appSettingsDao = appSettingsDao;
        this.sectionDao = sectionDao;
        this.categoryDao = categoryDao;
        this.remoteLessonSyncManager = remoteLessonSyncManager;
        this.remoteProgressSyncManager = remoteProgressSyncManager;
        this.userSessionStore = userSessionStore;
    }

    @Override
    public void syncLessonContent(long lessonId) {
        if (remoteLessonSyncManager != null) {
            remoteLessonSyncManager.syncLesson(lessonId);
        }
    }

    @Override
    public void syncLessonProgress(long lessonId) {
        if (shouldPersistProgress() && remoteProgressSyncManager != null) {
            remoteProgressSyncManager.syncLessonProgress(lessonId);
        }
    }

    @Override
    public boolean hasLessonContent(long lessonId) {
        return sentenceDao.countByLessonId(lessonId) > 0;
    }

    @Override
    public LessonSession getLessonSession(long lessonId) {
        LessonEntity lesson = lessonDao.getById(lessonId);
        if (lesson == null) {
            return new LessonSession(
                    lessonId,
                    "",
                    "",
                    "",
                    "",
                    "",
                    70,
                    null,
                    new ArrayList<>(),
                    RepositoryFormatters.defaultBestAttempt()
            );
        }
        List<SentenceEntity> sentenceEntities = sentenceDao.getByLessonId(lessonId);
        List<Sentence> sentences = new ArrayList<>();
        for (SentenceEntity entity : sentenceEntities) {
            sentences.add(new Sentence(
                    entity.id,
                    entity.orderIndex,
                    resolveAudioSource(entity),
                    entity.localAudioPath,
                    entity.content,
                    entity.hintText == null ? "" : entity.hintText,
                    entity.durationMillis,
                    entity.startTime,
                    entity.endTime,
                    entity.properNouns
            ));
        }

        SpeakingAttempt bestAttempt = RepositoryFormatters.defaultBestAttempt();
        if (shouldPersistProgress()) {
            for (GuestSentenceProgressEntity progress : guestProgressDao.getByLessonId(lessonId)) {
                if (progress.bestSpeakingScore != null && progress.bestSpeakingScore > bestAttempt.getScore()) {
                    bestAttempt = new SpeakingAttempt(
                            progress.bestSpeakingScore,
                            progress.bestSpeakingTranscript == null ? "" : progress.bestSpeakingTranscript,
                            progress.bestSpeakingFeedback == null ? "" : progress.bestSpeakingFeedback,
                            progress.bestSpeakingAudioUrl == null ? "" : progress.bestSpeakingAudioUrl
                    );
                }
            }
        }

        SectionEntity section = findSection(lesson.sectionId);
        CategoryEntity category = section == null ? null : categoryDao.getBySlug(resolveCategorySlug(section.categoryId));

        return new LessonSession(
                lesson.id,
                lesson.title,
                category == null ? "" : category.name,
                category == null ? "" : category.practiceType,
                lesson.level,
                lesson.contentType,
                lesson.passThreshold,
                normalizeYoutubeVideoId(lesson.youtubeVideoId),
                sentences,
                bestAttempt
        );
    }

    @Override
    public LessonProgress getLessonProgress(long lessonId) {
        List<SentenceEntity> sentences = sentenceDao.getByLessonId(lessonId);
        List<GuestSentenceProgressEntity> progressEntries = shouldPersistProgress()
                ? guestProgressDao.getByLessonId(lessonId)
                : new ArrayList<>();

        Map<Long, SentenceStatus> statuses = new LinkedHashMap<>();
        for (SentenceEntity sentence : sentences) {
            statuses.put(sentence.id, SentenceStatus.NOT_STARTED);
        }
        for (GuestSentenceProgressEntity progress : progressEntries) {
            statuses.put(progress.sentenceId, progress.status);
        }

        int currentSentenceIndex = resolveCurrentSentenceIndex(sentences, statuses);
        SpeakingAttempt currentAttempt = resolveCurrentAttempt(sentences, progressEntries, currentSentenceIndex);

        return new LessonProgress(statuses, currentSentenceIndex, currentAttempt);
    }

    @Override
    public void saveSentenceStatus(long lessonId, long sentenceId, SentenceStatus status) {
        if (!shouldPersistProgress()) {
            return;
        }
        GuestSentenceProgressEntity existing = guestProgressDao.getBySentenceId(sentenceId);
        long now = System.currentTimeMillis();
        SentenceStatus resolvedStatus = resolvePersistedStatus(existing == null ? null : existing.status, status);
        GuestSentenceProgressEntity updated = new GuestSentenceProgressEntity(
                sentenceId,
                lessonId,
                resolvedStatus,
                existing == null ? null : existing.bestSpeakingScore,
                existing == null ? null : existing.bestSpeakingTranscript,
                existing == null ? null : existing.bestSpeakingFeedback,
                existing == null ? null : existing.bestSpeakingAudioUrl,
                existing == null ? null : existing.currentSpeakingScore,
                existing == null ? null : existing.currentSpeakingTranscript,
                existing == null ? null : existing.currentSpeakingFeedback,
                existing == null ? null : existing.currentSpeakingAudioUrl,
                now
        );
        guestProgressDao.upsert(updated);
        saveLastOpened(lessonId, sentenceId);
        if (remoteProgressSyncManager != null && resolvedStatus != (existing == null ? null : existing.status)) {
            remoteProgressSyncManager.submitProgressAsync(lessonId, sentenceId, resolvedStatus);
        }
    }

    @Override
    public void saveSpeakingAttempt(long lessonId, long sentenceId, SpeakingAttempt attempt, int passThreshold) {
        if (!shouldPersistProgress()) {
            return;
        }
        GuestSentenceProgressEntity existing = guestProgressDao.getBySentenceId(sentenceId);
        int bestScore = existing != null && existing.bestSpeakingScore != null ? existing.bestSpeakingScore : Integer.MIN_VALUE;
        boolean updateBest = attempt.getScore() >= bestScore;
        SentenceStatus nextStatus = attempt.getScore() >= passThreshold ? SentenceStatus.COMPLETED : SentenceStatus.IN_PROGRESS;
        SentenceStatus resolvedStatus = resolvePersistedStatus(existing == null ? null : existing.status, nextStatus);

        GuestSentenceProgressEntity updated = new GuestSentenceProgressEntity(
                sentenceId,
                lessonId,
                resolvedStatus,
                updateBest ? attempt.getScore() : (existing == null ? null : existing.bestSpeakingScore),
                updateBest ? attempt.getTranscript() : (existing == null ? null : existing.bestSpeakingTranscript),
                updateBest ? attempt.getFeedback() : (existing == null ? null : existing.bestSpeakingFeedback),
                updateBest ? attempt.getAudioUrl() : (existing == null ? null : existing.bestSpeakingAudioUrl),
                attempt.getScore(),
                attempt.getTranscript(),
                attempt.getFeedback(),
                attempt.getAudioUrl(),
                System.currentTimeMillis()
        );
        guestProgressDao.upsert(updated);
        saveLastOpened(lessonId, sentenceId);
        if (remoteProgressSyncManager != null && resolvedStatus != (existing == null ? null : existing.status)) {
            remoteProgressSyncManager.submitProgressAsync(lessonId, sentenceId, resolvedStatus);
        }
    }

    private int resolveCurrentSentenceIndex(List<SentenceEntity> sentences, Map<Long, SentenceStatus> statuses) {
        if (!shouldPersistProgress()) {
            return 0;
        }
        
        AppSettingsEntity settings = appSettingsDao.getSettings();
        if (settings != null && settings.lastOpenedSentenceId != null) {
            for (int index = 0; index < sentences.size(); index++) {
                if (sentences.get(index).id == settings.lastOpenedSentenceId) {
                    return index;
                }
            }
        }

        for (int index = 0; index < sentences.size(); index++) {
            SentenceStatus status = statuses.get(sentences.get(index).id);
            if (status == SentenceStatus.IN_PROGRESS) {
                return index;
            }
        }
        for (int index = 0; index < sentences.size(); index++) {
            SentenceStatus status = statuses.get(sentences.get(index).id);
            if (status != SentenceStatus.COMPLETED) {
                return index;
            }
        }
        return 0;
    }

    private SpeakingAttempt resolveCurrentAttempt(
            List<SentenceEntity> sentences,
            List<GuestSentenceProgressEntity> progressEntries,
            int currentSentenceIndex
    ) {
        if (sentences.isEmpty()) {
            return RepositoryFormatters.defaultSpeakingAttempt();
        }

        long currentSentenceId = sentences.get(currentSentenceIndex).id;
        for (GuestSentenceProgressEntity progress : progressEntries) {
            if (progress.sentenceId == currentSentenceId && progress.currentSpeakingScore != null) {
                return new SpeakingAttempt(
                        progress.currentSpeakingScore,
                        progress.currentSpeakingTranscript == null ? "" : progress.currentSpeakingTranscript,
                        progress.currentSpeakingFeedback == null ? "" : progress.currentSpeakingFeedback,
                        progress.currentSpeakingAudioUrl == null ? "" : progress.currentSpeakingAudioUrl
                );
            }
        }
        return RepositoryFormatters.defaultSpeakingAttempt();
    }

    private void saveLastOpened(long lessonId, long sentenceId) {
        if (!shouldPersistProgress()) {
            return;
        }
        AppSettingsEntity settings = appSettingsDao.getSettings();
        AppSettingsEntity updated = new AppSettingsEntity(
                1L,
                settings == null ? "en" : settings.languageCode,
                settings != null && settings.onboardingCompleted,
                lessonId,
                sentenceId,
                settings == null ? null : settings.lastViewedCategorySlug
        );
        appSettingsDao.upsert(updated);
    }

    private SectionEntity findSection(long sectionId) {
        for (CategoryEntity category : categoryDao.getAllOrdered()) {
            for (SectionEntity section : sectionDao.getByCategoryId(category.id)) {
                if (section.id == sectionId) {
                    return section;
                }
            }
        }
        return null;
    }

    private String resolveCategorySlug(long categoryId) {
        for (CategoryEntity category : categoryDao.getAllOrdered()) {
            if (category.id == categoryId) {
                return category.slug;
            }
        }
        return null;
    }

    private String resolveAudioSource(SentenceEntity entity) {
        if (entity == null) {
            return null;
        }
        if (entity.localAudioPath != null && !entity.localAudioPath.trim().isEmpty()) {
            File localFile = new File(entity.localAudioPath);
            if (localFile.exists() && localFile.isFile() && localFile.length() > 0L) {
                return entity.localAudioPath;
            }
        }
        return entity.audioUrl;
    }

    private String normalizeYoutubeVideoId(String rawValue) {
        if (rawValue == null) {
            return null;
        }

        String value = rawValue.trim();
        if (value.isEmpty()) {
            return null;
        }
        if (value.matches("[A-Za-z0-9_-]{11}")) {
            return value;
        }

        try {
            Uri uri = Uri.parse(value);
            String host = uri.getHost();
            if (host == null) {
                return null;
            }
            String normalizedHost = host.toLowerCase(Locale.US);
            if (normalizedHost.contains("youtu.be")) {
                String path = uri.getPath();
                if (path != null) {
                    String candidate = path.replace("/", "").trim();
                    if (candidate.matches("[A-Za-z0-9_-]{11}")) {
                        return candidate;
                    }
                }
            }
            if (normalizedHost.contains("youtube.com")) {
                String candidate = uri.getQueryParameter("v");
                if (candidate != null && candidate.matches("[A-Za-z0-9_-]{11}")) {
                    return candidate;
                }
                List<String> segments = uri.getPathSegments();
                if (segments.size() >= 2) {
                    String second = segments.get(1);
                    if (("embed".equals(segments.get(0)) || "shorts".equals(segments.get(0)))
                            && second.matches("[A-Za-z0-9_-]{11}")) {
                        return second;
                    }
                }
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    private SentenceStatus resolvePersistedStatus(SentenceStatus existingStatus, SentenceStatus requestedStatus) {
        if (existingStatus == SentenceStatus.COMPLETED || requestedStatus == SentenceStatus.COMPLETED) {
            return SentenceStatus.COMPLETED;
        }
        return requestedStatus == null ? existingStatus : requestedStatus;
    }

    private boolean shouldPersistProgress() {
        return userSessionStore != null && userSessionStore.isLoggedIn();
    }
}
