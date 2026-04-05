package hcmute.edu.vn.nguyenthetan.data.repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
import hcmute.edu.vn.nguyenthetan.data.repository.support.RepositoryFormatters;
import hcmute.edu.vn.nguyenthetan.domain.model.LessonProgress;
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

    public RoomLessonRepository(
            LessonDao lessonDao,
            SentenceDao sentenceDao,
            GuestProgressDao guestProgressDao,
            AppSettingsDao appSettingsDao,
            SectionDao sectionDao,
            CategoryDao categoryDao
    ) {
        this.lessonDao = lessonDao;
        this.sentenceDao = sentenceDao;
        this.guestProgressDao = guestProgressDao;
        this.appSettingsDao = appSettingsDao;
        this.sectionDao = sectionDao;
        this.categoryDao = categoryDao;
    }

    @Override
    public LessonSession getLessonSession(long lessonId) {
        LessonEntity lesson = lessonDao.getById(lessonId);
        List<SentenceEntity> sentenceEntities = sentenceDao.getByLessonId(lessonId);
        List<Sentence> sentences = new ArrayList<>();
        for (SentenceEntity entity : sentenceEntities) {
            sentences.add(new Sentence(entity.id, entity.orderIndex, entity.content, entity.hintText == null ? "" : entity.hintText, entity.durationMillis));
        }

        SpeakingAttempt bestAttempt = RepositoryFormatters.defaultBestAttempt();
        for (GuestSentenceProgressEntity progress : guestProgressDao.getByLessonId(lessonId)) {
            if (progress.bestSpeakingScore != null && progress.bestSpeakingScore > bestAttempt.getScore()) {
                bestAttempt = new SpeakingAttempt(
                        progress.bestSpeakingScore,
                        progress.bestSpeakingTranscript == null ? "" : progress.bestSpeakingTranscript,
                        progress.bestSpeakingFeedback == null ? "" : progress.bestSpeakingFeedback
                );
            }
        }

        SectionEntity section = findSection(lesson.sectionId);
        CategoryEntity category = section == null ? null : categoryDao.getBySlug(resolveCategorySlug(section.categoryId));

        return new LessonSession(
                lesson.id,
                lesson.title,
                category == null ? "" : category.name,
                lesson.level,
                lesson.passThreshold,
                sentences,
                bestAttempt
        );
    }

    @Override
    public LessonProgress getLessonProgress(long lessonId) {
        List<SentenceEntity> sentences = sentenceDao.getByLessonId(lessonId);
        List<GuestSentenceProgressEntity> progressEntries = guestProgressDao.getByLessonId(lessonId);

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
        GuestSentenceProgressEntity existing = guestProgressDao.getBySentenceId(sentenceId);
        long now = System.currentTimeMillis();
        GuestSentenceProgressEntity updated = new GuestSentenceProgressEntity(
                sentenceId,
                lessonId,
                status,
                existing == null ? null : existing.bestSpeakingScore,
                existing == null ? null : existing.bestSpeakingTranscript,
                existing == null ? null : existing.bestSpeakingFeedback,
                existing == null ? null : existing.currentSpeakingScore,
                existing == null ? null : existing.currentSpeakingTranscript,
                existing == null ? null : existing.currentSpeakingFeedback,
                now
        );
        guestProgressDao.upsert(updated);
        saveLastOpened(lessonId, sentenceId);
    }

    @Override
    public void saveSpeakingAttempt(long lessonId, long sentenceId, SpeakingAttempt attempt, int passThreshold) {
        GuestSentenceProgressEntity existing = guestProgressDao.getBySentenceId(sentenceId);
        int bestScore = existing != null && existing.bestSpeakingScore != null ? existing.bestSpeakingScore : Integer.MIN_VALUE;
        boolean updateBest = attempt.getScore() >= bestScore;
        SentenceStatus nextStatus = attempt.getScore() >= passThreshold ? SentenceStatus.COMPLETED : SentenceStatus.IN_PROGRESS;

        GuestSentenceProgressEntity updated = new GuestSentenceProgressEntity(
                sentenceId,
                lessonId,
                nextStatus,
                updateBest ? attempt.getScore() : (existing == null ? null : existing.bestSpeakingScore),
                updateBest ? attempt.getTranscript() : (existing == null ? null : existing.bestSpeakingTranscript),
                updateBest ? attempt.getFeedback() : (existing == null ? null : existing.bestSpeakingFeedback),
                attempt.getScore(),
                attempt.getTranscript(),
                attempt.getFeedback(),
                System.currentTimeMillis()
        );
        guestProgressDao.upsert(updated);
        saveLastOpened(lessonId, sentenceId);
    }

    private int resolveCurrentSentenceIndex(List<SentenceEntity> sentences, Map<Long, SentenceStatus> statuses) {
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
            return RepositoryFormatters.defaultAttempt();
        }

        long currentSentenceId = sentences.get(currentSentenceIndex).id;
        for (GuestSentenceProgressEntity progress : progressEntries) {
            if (progress.sentenceId == currentSentenceId && progress.currentSpeakingScore != null) {
                return new SpeakingAttempt(
                        progress.currentSpeakingScore,
                        progress.currentSpeakingTranscript == null ? "" : progress.currentSpeakingTranscript,
                        progress.currentSpeakingFeedback == null ? "" : progress.currentSpeakingFeedback
                );
            }
        }
        return RepositoryFormatters.defaultAttempt();
    }

    private void saveLastOpened(long lessonId, long sentenceId) {
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
}
