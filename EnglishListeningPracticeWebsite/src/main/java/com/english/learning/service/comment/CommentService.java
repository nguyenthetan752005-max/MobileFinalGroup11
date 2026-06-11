package com.english.learning.service.comment;

import com.english.learning.entity.Comment;
import com.english.learning.entity.CommentVote;

import java.util.List;

public interface CommentService {
    List<Comment> getCommentsBySentenceId(Long sentenceId);

    List<Comment> getTopLevelCommentsWithVotes(Long sentenceId);

    List<Comment> getRepliesWithVotes(Long parentId);

    List<Comment> getCommentsByUserId(Long userId);

    Comment addComment(Long sentenceId, Long userId, String content, Long parentId);

    Comment editComment(Long commentId, Long userId, String newContent);

    CommentVote voteComment(Long commentId, Long userId, Boolean isLike);

    void deleteComment(Long commentId, Long userId);
}

