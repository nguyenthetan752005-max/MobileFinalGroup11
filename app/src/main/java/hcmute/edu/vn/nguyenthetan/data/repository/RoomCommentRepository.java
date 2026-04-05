package hcmute.edu.vn.nguyenthetan.data.repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import hcmute.edu.vn.nguyenthetan.data.local.dao.community.CommentDao;
import hcmute.edu.vn.nguyenthetan.data.local.entity.community.CommentEntity;
import hcmute.edu.vn.nguyenthetan.domain.model.comment.Comment;
import hcmute.edu.vn.nguyenthetan.domain.repository.CommentRepository;

public class RoomCommentRepository implements CommentRepository {

    private final CommentDao commentDao;

    public RoomCommentRepository(CommentDao commentDao) {
        this.commentDao = commentDao;
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
        return new Comment(
                entity.author,
                entity.avatarLabel,
                entity.timeAgo,
                entity.content,
                entity.likeCount,
                entity.dislikeCount,
                replies
        );
    }
}
