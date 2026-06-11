package com.english.learning.service.impl.comment;

import com.english.learning.repository.CommentRepository;
import com.english.learning.repository.CommentVoteRepository;
import com.english.learning.repository.SentenceRepository;
import com.english.learning.repository.UserRepository;
import com.english.learning.entity.Comment;
import com.english.learning.entity.CommentVote;
import com.english.learning.entity.Sentence;
import com.english.learning.entity.User;
import com.english.learning.service.comment.CommentService;
import com.english.learning.service.notification.MobileNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final CommentVoteRepository commentVoteRepository;
    private final UserRepository userRepository;
    private final SentenceRepository sentenceRepository;
    private final MobileNotificationService mobileNotificationService;

    @Override
    public List<Comment> getCommentsBySentenceId(Long sentenceId) {
        return commentRepository.findBySentence_IdAndIsHiddenFalseAndParentIsNullOrderByCreatedAtDesc(sentenceId);
    }

    @Override
    public List<Comment> getTopLevelCommentsWithVotes(Long sentenceId) {
        List<Comment> comments = commentRepository
                .findBySentence_IdAndIsHiddenFalseAndParentIsNullOrderByCreatedAtDesc(sentenceId);
        comments.forEach(this::populateVoteCounts);
        return comments;
    }

    @Override
    public List<Comment> getRepliesWithVotes(Long parentId) {
        List<Comment> replies = commentRepository.findByParent_IdAndIsHiddenFalseOrderByCreatedAtAsc(parentId);
        replies.forEach(this::populateVoteCounts);
        return replies;
    }

    @Override
    public List<Comment> getCommentsByUserId(Long userId) {
        List<Comment> comments = commentRepository.findByUser_IdAndIsHiddenFalseOrderByCreatedAtDesc(userId);
        comments.forEach(this::populateVoteCounts);
        return comments;
    }

    private void populateVoteCounts(Comment comment) {
        comment.setLikeCount(commentVoteRepository.countVotes(comment.getId(), true));
        comment.setDislikeCount(commentVoteRepository.countVotes(comment.getId(), false));
    }

    @Override
    public Comment addComment(Long sentenceId, Long userId, String content, Long parentId) {
        Comment comment = new Comment();
        comment.setContent(content);

        Sentence sentence = sentenceRepository.findPublishedById(sentenceId, com.english.learning.enums.ContentStatus.PUBLISHED)
                .orElseThrow(() -> new RuntimeException("Sentence khÃ´ng tá»“n táº¡i!"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User khÃ´ng tá»“n táº¡i!"));

        comment.setSentence(sentence);
        comment.setUser(user);

        if (parentId != null) {
            Comment parent = commentRepository.findById(parentId)
                    .orElseThrow(() -> new RuntimeException("Comment gá»‘c khÃ´ng tá»“n táº¡i!"));
            if (parent.getSentence() == null || !parent.getSentence().getId().equals(sentenceId)) {
                throw new RuntimeException("Reply phai thuoc cung mot sentence voi comment goc.");
            }
            comment.setParent(parent);
        }

        Comment saved = commentRepository.save(comment);
        if (comment.getParent() != null) {
            mobileNotificationService.createReplyNotification(comment.getParent(), saved);
        }
        populateVoteCounts(saved);
        return saved;
    }

    @Override
    public CommentVote voteComment(Long commentId, Long userId, Boolean isLike) {
        Optional<CommentVote> existingVote = commentVoteRepository.findByComment_IdAndUser_Id(commentId, userId);

        if (existingVote.isPresent()) {
            CommentVote vote = existingVote.get();
            if (vote.getIsLike().equals(isLike)) {
                // CÃ¹ng loáº¡i â†’ xÃ³a vote (toggle off)
                commentVoteRepository.deleteById(vote.getId());
                return null;
            } else {
                // KhÃ¡c loáº¡i â†’ Ä‘á»•i is_like
                vote.setIsLike(isLike);
                return commentVoteRepository.save(vote);
            }
        } else {
            // ChÆ°a vote â†’ táº¡o má»›i
            CommentVote vote = new CommentVote();
            Comment comment = commentRepository.findById(commentId)
                    .orElseThrow(() -> new RuntimeException("Comment khÃ´ng tá»“n táº¡i!"));
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User khÃ´ng tá»“n táº¡i!"));
            vote.setComment(comment);
            vote.setUser(user);
            vote.setIsLike(isLike);
            return commentVoteRepository.save(vote);
        }
    }

    @Override
    public Comment editComment(Long commentId, Long userId, String newContent) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment khÃ´ng tá»“n táº¡i!"));
        if (!comment.getUser().getId().equals(userId)) {
            throw new RuntimeException("Báº¡n khÃ´ng cÃ³ quyá»n chá»‰nh sá»­a comment nÃ y!");
        }
        comment.setContent(newContent + " [author edited comment]");
        Comment saved = commentRepository.save(comment);
        populateVoteCounts(saved);
        return saved;
    }

    @Override
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment khÃ´ng tá»“n táº¡i!"));
        if (!comment.getUser().getId().equals(userId)) {
            throw new RuntimeException("Báº¡n khÃ´ng cÃ³ quyá»n xÃ³a comment nÃ y!");
        }
        // Náº¿u cÃ³ reply â†’ soft delete (giá»¯ comment, Ä‘á»•i ná»™i dung)
        List<Comment> replies = commentRepository.findByParent_IdOrderByCreatedAtAsc(commentId);
        if (replies != null && !replies.isEmpty()) {
            comment.setContent("comment has been deleted by author");
            commentRepository.save(comment);
        } else {
            commentRepository.deleteById(commentId);
        }
    }
}

