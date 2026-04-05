package hcmute.edu.vn.nguyenthetan.data.remote.mapper;

import java.util.ArrayList;
import java.util.List;

import hcmute.edu.vn.nguyenthetan.data.local.entity.catalog.CategoryEntity;
import hcmute.edu.vn.nguyenthetan.data.local.entity.community.CommentEntity;
import hcmute.edu.vn.nguyenthetan.data.local.entity.community.LeaderboardEntryEntity;
import hcmute.edu.vn.nguyenthetan.data.local.entity.community.LeaderboardMetaEntity;
import hcmute.edu.vn.nguyenthetan.data.local.entity.lesson.LessonEntity;
import hcmute.edu.vn.nguyenthetan.data.local.entity.catalog.SectionEntity;
import hcmute.edu.vn.nguyenthetan.data.local.entity.lesson.SentenceEntity;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.MobileBootstrapDto;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.MobileBootstrapCommentDto;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.MobileCategoryDto;
import hcmute.edu.vn.nguyenthetan.data.remote.dto.MobileLeaderboardEntryDto;
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
            entities.add(new CategoryEntity(
                    dto.id,
                    dto.slug,
                    dto.name,
                    dto.imageUrl,
                    dto.levelRange,
                    dto.contentType,
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
            entities.add(new SectionEntity(dto.id, dto.categoryId, dto.name, dto.description, dto.orderIndex));
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
                    dto.id,
                    dto.sectionId,
                    dto.title,
                    dto.level,
                    dto.displayType,
                    dto.contentType,
                    dto.totalSentences,
                    dto.passThreshold,
                    dto.youtubeVideoId,
                    dto.orderIndex
            ));
        }
        return entities;
    }

    public static List<SentenceEntity> toSentenceEntities(List<MobileSentenceDto> dtos) {
        List<SentenceEntity> entities = new ArrayList<>();
        if (dtos == null) {
            return entities;
        }
        for (MobileSentenceDto dto : dtos) {
            entities.add(new SentenceEntity(
                    dto.id,
                    dto.lessonId,
                    dto.audioUrl,
                    dto.content,
                    dto.hintText,
                    dto.durationMillis,
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
                    dto.id,
                    dto.sentenceId,
                    dto.parentCommentId,
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
}
