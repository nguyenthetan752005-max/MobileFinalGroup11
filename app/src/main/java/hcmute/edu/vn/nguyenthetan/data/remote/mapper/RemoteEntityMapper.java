package hcmute.edu.vn.nguyenthetan.data.remote.mapper;

import android.net.Uri;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.nio.charset.StandardCharsets;

import hcmute.edu.vn.nguyenthetan.BuildConfig;
import hcmute.edu.vn.nguyenthetan.data.local.entity.catalog.CategoryEntity;
import hcmute.edu.vn.nguyenthetan.data.local.entity.community.CommentEntity;
import hcmute.edu.vn.nguyenthetan.data.local.entity.community.LeaderboardEntryEntity;
import hcmute.edu.vn.nguyenthetan.data.local.entity.community.LeaderboardMetaEntity;
import hcmute.edu.vn.nguyenthetan.data.local.entity.lesson.LessonEntity;
import hcmute.edu.vn.nguyenthetan.data.local.entity.catalog.SectionEntity;
import hcmute.edu.vn.nguyenthetan.data.local.entity.lesson.SentenceEntity;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.MobileBootstrapDto;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.MobileBootstrapCommentDto;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.MobileCategoryCollectionSectionDto;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.MobileCategoryDto;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.MobileLeaderboardEntryDto;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.MobileLessonDetailDto;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.MobileLessonDto;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.MobileSectionDto;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.MobileSentenceDto;

public final class RemoteEntityMapper {

    private RemoteEntityMapper() {
    }

    public static List<CategoryEntity> toCategoryEntities(List<MobileCategoryDto> dtos) {
        List<CategoryEntity> entities = new ArrayList<>();
        if (dtos == null) {
            return entities;
        }
        for (MobileCategoryDto dto : dtos) {
            long categoryId = resolveId(dto.id);
            entities.add(new CategoryEntity(
                    categoryId,
                    resolveSlug(dto),
                    safeString(dto.name, "Category " + categoryId),
                    dto.imageUrl,
                    dto.levelRange,
                    normalizeContentType(dto.contentType),
                    dto.practiceType,
                    dto.totalLessons,
                    dto.description,
                    dto.orderIndex
            ));
        }
        return entities;
    }

    public static List<SectionEntity> toSectionEntities(List<MobileSectionDto> dtos) {
        List<SectionEntity> entities = new ArrayList<>();
        if (dtos == null) {
            return entities;
        }
        for (MobileSectionDto dto : dtos) {
            entities.add(new SectionEntity(
                    resolveId(dto.id),
                    resolveId(dto.categoryId),
                    dto.name,
                    dto.description,
                    dto.orderIndex
            ));
        }
        return entities;
    }

    public static List<SectionEntity> toSectionEntities(String categoryId, List<MobileCategoryCollectionSectionDto> dtos) {
        List<SectionEntity> entities = new ArrayList<>();
        if (dtos == null) {
            return entities;
        }
        long resolvedCategoryId = resolveId(categoryId);
        for (MobileCategoryCollectionSectionDto dto : dtos) {
            entities.add(new SectionEntity(
                    resolveId(dto.id),
                    resolvedCategoryId,
                    dto.name,
                    dto.description,
                    dto.orderIndex
            ));
        }
        return entities;
    }

    public static List<LessonEntity> toLessonEntities(List<MobileLessonDto> dtos) {
        List<LessonEntity> entities = new ArrayList<>();
        if (dtos == null) {
            return entities;
        }
        for (MobileLessonDto dto : dtos) {
            entities.add(new LessonEntity(
                    resolveId(dto.id),
                    resolveId(dto.sectionId),
                    dto.title,
                    dto.level,
                    resolveLessonPracticeType(dto),
                    normalizeContentType(dto.contentType),
                    dto.totalSentences,
                    dto.passThreshold > 0 ? dto.passThreshold : 70,
                    dto.youtubeVideoId,
                    dto.orderIndex
            ));
        }
        return entities;
    }

    public static List<LessonEntity> toLessonEntities(List<MobileLessonDto> dtos, List<SectionEntity> sections, List<CategoryEntity> categories) {
        List<LessonEntity> entities = new ArrayList<>();
        if (dtos == null) {
            return entities;
        }

        Map<Long, Long> sectionToCategory = new HashMap<>();
        for (SectionEntity section : sections) {
            sectionToCategory.put(section.id, section.categoryId);
        }

        Map<Long, String> categoryContentTypes = new HashMap<>();
        for (CategoryEntity category : categories) {
            categoryContentTypes.put(category.id, category.contentType);
        }

        for (MobileLessonDto dto : dtos) {
            long sectionId = resolveId(dto.sectionId);
            String contentType = normalizeContentType(dto.contentType);
            if (contentType == null) {
                Long categoryId = sectionToCategory.get(sectionId);
                contentType = categoryId == null ? null : categoryContentTypes.get(categoryId);
            }

            entities.add(new LessonEntity(
                    resolveId(dto.id),
                    sectionId,
                    dto.title,
                    dto.level,
                    resolveLessonPracticeType(dto),
                    contentType,
                    dto.totalSentences,
                    dto.passThreshold > 0 ? dto.passThreshold : 70,
                    dto.youtubeVideoId,
                    dto.orderIndex
            ));
        }
        return entities;
    }

    public static LessonEntity toLessonEntity(MobileLessonDetailDto dto) {
        if (dto == null) {
            return null;
        }
        return new LessonEntity(
                resolveId(dto.id),
                resolveId(dto.sectionId),
                dto.title,
                dto.level,
                resolveLessonPracticeType(dto.displayType, dto.contentType),
                normalizeContentType(dto.contentType),
                dto.totalSentences,
                dto.passThreshold > 0 ? dto.passThreshold : 70,
                dto.youtubeVideoId,
                dto.orderIndex
        );
    }

    public static List<SentenceEntity> toSentenceEntities(List<MobileSentenceDto> dtos) {
        List<SentenceEntity> entities = new ArrayList<>();
        if (dtos == null) {
            return entities;
        }
        for (MobileSentenceDto dto : dtos) {
            entities.add(new SentenceEntity(
                    resolveId(dto.id),
                    resolveId(dto.lessonId),
                    normalizeMediaUrl(dto.audioUrl),
                    null,
                    dto.content,
                    safeString(dto.hintText, ""),
                    resolveDurationMillis(dto),
                    dto.startTime,
                    dto.endTime,
                    dto.orderIndex
            ));
        }
        return entities;
    }

    public static List<CommentEntity> toCommentEntities(List<MobileBootstrapCommentDto> dtos) {
        List<CommentEntity> entities = new ArrayList<>();
        if (dtos == null) {
            return entities;
        }
        for (MobileBootstrapCommentDto dto : dtos) {
            entities.add(new CommentEntity(
                    resolveId(dto.id),
                    resolveId(dto.sentenceId),
                    resolveNullableId(dto.parentCommentId),
                    dto.author,
                    dto.avatarLabel,
                    dto.timeAgo,
                    dto.content,
                    dto.likeCount,
                    dto.dislikeCount,
                    dto.orderIndex
            ));
        }
        return entities;
    }

    public static List<LeaderboardEntryEntity> toLeaderboardEntries(MobileBootstrapDto bootstrapDto) {
        List<LeaderboardEntryEntity> entries = new ArrayList<>();
        if (bootstrapDto == null || bootstrapDto.leaderboard == null) {
            return entries;
        }

        appendEntries(entries, "WEEKLY", bootstrapDto.leaderboard.weeklyEntries);
        appendEntries(entries, "MONTHLY", bootstrapDto.leaderboard.monthlyEntries);
        return entries;
    }

    public static LeaderboardMetaEntity toLeaderboardMeta(MobileBootstrapDto bootstrapDto) {
        if (bootstrapDto == null || bootstrapDto.leaderboard == null) {
            return new LeaderboardMetaEntity(1L, 0, "Guest mode");
        }
        return new LeaderboardMetaEntity(
                1L,
                bootstrapDto.leaderboard.currentUserRank,
                bootstrapDto.leaderboard.currentUserTime
        );
    }

    private static void appendEntries(List<LeaderboardEntryEntity> entries, String period, List<MobileLeaderboardEntryDto> dtos) {
        if (dtos == null) {
            return;
        }
        for (MobileLeaderboardEntryDto dto : dtos) {
            entries.add(new LeaderboardEntryEntity(period, dto.rank, dto.name, dto.avatarLabel, dto.activeTime, dto.currentUser));
        }
    }

    private static String resolveSlug(MobileCategoryDto dto) {
        if (dto.slug != null && !dto.slug.trim().isEmpty()) {
            return dto.slug.trim();
        }
        String base = safeString(dto.name, "category-" + resolveId(dto.id)).trim().toLowerCase(Locale.US);
        base = base.replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
        return base.isEmpty() ? "category-" + resolveId(dto.id) : base;
    }

    private static String resolveLessonPracticeType(MobileLessonDto dto) {
        return resolveLessonPracticeType(dto.displayType, dto.contentType);
    }

    private static String resolveLessonPracticeType(String displayType, String contentTypeValue) {
        if (displayType != null && !displayType.trim().isEmpty()) {
            return displayType;
        }
        String contentType = normalizeContentType(contentTypeValue);
        if ("VIDEO".equals(contentType)) {
            return "Video";
        }
        if ("AUDIO".equals(contentType)) {
            return "Audio";
        }
        return "Audio";
    }

    private static long resolveDurationMillis(MobileSentenceDto dto) {
        if (dto.durationMillis > 0) {
            return dto.durationMillis;
        }
        if (dto.startTime != null && dto.endTime != null && dto.endTime > dto.startTime) {
            return Math.round((dto.endTime - dto.startTime) * 1000d);
        }
        return 0L;
    }

    private static String normalizeContentType(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim().toUpperCase(Locale.US);
    }

    private static String safeString(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    private static String normalizeMediaUrl(String rawUrl) {
        if (rawUrl == null || rawUrl.trim().isEmpty()) {
            return rawUrl;
        }
        try {
            Uri mediaUri = Uri.parse(rawUrl.trim());
            String host = mediaUri.getHost();
            if (host == null) {
                return rawUrl;
            }
            String normalizedHost = host.toLowerCase(Locale.US);
            if (!"localhost".equals(normalizedHost) && !"127.0.0.1".equals(normalizedHost)) {
                return rawUrl;
            }

            Uri baseUri = Uri.parse(BuildConfig.TUNGTUNG_API_BASE_URL);
            String baseScheme = baseUri.getScheme();
            String baseHost = baseUri.getHost();
            if (baseScheme == null || baseHost == null) {
                return rawUrl;
            }

            Uri.Builder builder = mediaUri.buildUpon()
                    .scheme(baseScheme)
                    .encodedAuthority(baseHost + (baseUri.getPort() != -1 ? ":" + baseUri.getPort() : ""));
            return builder.build().toString();
        } catch (Exception ignored) {
            return rawUrl;
        }
    }

    private static long resolveId(String rawId) {
        if (rawId == null || rawId.trim().isEmpty()) {
            return 0L;
        }

        String value = rawId.trim();
        if (value.matches("-?\\d+")) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException ignored) {
            }
        }

        UUID uuid = UUID.nameUUIDFromBytes(value.getBytes(StandardCharsets.UTF_8));
        return uuid.getMostSignificantBits() & Long.MAX_VALUE;
    }

    private static Long resolveNullableId(String rawId) {
        if (rawId == null || rawId.trim().isEmpty()) {
            return null;
        }
        return resolveId(rawId);
    }
}
