package hcmute.edu.vn.nguyenthetan.data.repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;

import hcmute.edu.vn.nguyenthetan.data.local.dao.community.CommentDao;
import hcmute.edu.vn.nguyenthetan.data.local.dao.user.ProfileDao;
import hcmute.edu.vn.nguyenthetan.data.local.entity.community.CommentEntity;
import hcmute.edu.vn.nguyenthetan.data.local.entity.user.ProfileEntity;
import hcmute.edu.vn.nguyenthetan.core.UserSessionStore;
import hcmute.edu.vn.nguyenthetan.domain.model.comment.Comment;
import hcmute.edu.vn.nguyenthetan.domain.repository.CommentRepository;

public class RoomCommentRepository implements CommentRepository {

    private final CommentDao commentDao;
    private final ProfileDao profileDao;
    private final UserSessionStore userSessionStore;

    public RoomCommentRepository(CommentDao commentDao, ProfileDao profileDao, UserSessionStore userSessionStore) {
        this.commentDao = commentDao;
        this.profileDao = profileDao;
        this.userSessionStore = userSessionStore;
    }

    @Override
    public List<Comment> getComments(long sentenceId) {
        List<CommentEntity> entities = commentDao.getBySentenceId(sentenceId);
        Map<Long, List<Comment>> repliesByParent = new LinkedHashMap<>();
        List<CommentEntity> rootEntities = new ArrayList<>();

        for (CommentEntity entity : entities) {
            if (entity.parentCommentId == null) {
                rootEntities.add(entity);
            }
        }

        for (CommentEntity entity : entities) {
            if (entity.parentCommentId != null) {
                repliesByParent.computeIfAbsent(entity.parentCommentId, ignored -> new ArrayList<>()).add(toDomain(entity, new ArrayList<>()));
            }
        }

        List<Comment> comments = new ArrayList<>();
        for (CommentEntity entity : rootEntities) {
            comments.add(toDomain(entity, repliesByParent.getOrDefault(entity.id, new ArrayList<>())));
        }
        return comments;
    }

    private Comment toDomain(CommentEntity entity, List<Comment> replies) {
        ProfileEntity currentProfile = profileDao == null ? null : profileDao.getProfile();
        long currentUserId = userSessionStore == null ? 0L : userSessionStore.getUserId();
        boolean ownComment = currentUserId > 0L && entity.authorId == currentUserId;
        String resolvedAuthor = safeText(entity.author, currentProfile != null ? currentProfile.name : "");
        String resolvedAvatarUrl = ownComment
                ? firstNonBlank(entity.avatarUrl, currentProfile == null ? null : currentProfile.avatarUrl)
                : entity.avatarUrl;
        String resolvedAvatarLabel = ownComment
                ? firstNonBlank(entity.avatarLabel, buildAvatarLabel(currentProfile != null ? currentProfile.name : resolvedAuthor))
                : firstNonBlank(entity.avatarLabel, buildAvatarLabel(resolvedAuthor));
        return new Comment(
                entity.id,
                entity.authorId,
                entity.sentenceId,
                resolvedAuthor,
                resolvedAvatarLabel,
                resolvedAvatarUrl,
                entity.timeAgo,
                entity.content,
                entity.likeCount,
                entity.dislikeCount,
                replies
        );
    }

    private String safeText(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private String firstNonBlank(String primary, String fallback) {
        return primary == null || primary.trim().isEmpty() ? fallback : primary.trim();
    }

    private String buildAvatarLabel(String authorName) {
        if (authorName == null || authorName.trim().isEmpty()) {
            return "TT";
        }
        String[] parts = authorName.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase(Locale.US);
        }
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase(Locale.US);
    }
}
