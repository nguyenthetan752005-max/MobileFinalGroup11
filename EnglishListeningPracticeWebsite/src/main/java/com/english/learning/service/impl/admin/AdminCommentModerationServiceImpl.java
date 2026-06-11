package com.english.learning.service.impl.admin;

import com.english.learning.entity.Comment;
import com.english.learning.entity.Sentence;
import com.english.learning.entity.User;
import com.english.learning.enums.ContentStatus;
import com.english.learning.exception.ResourceNotFoundException;
import com.english.learning.repository.CommentRepository;
import com.english.learning.repository.CommentVoteRepository;
import com.english.learning.repository.SentenceRepository;
import com.english.learning.repository.UserRepository;
import com.english.learning.service.admin.AdminCommentModerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminCommentModerationServiceImpl implements AdminCommentModerationService {

    private final CommentRepository commentRepository;
    private final CommentVoteRepository commentVoteRepository;
    private final SentenceRepository sentenceRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Comment toggleHideComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment khong ton tai"));
        comment.setIsHidden(!Boolean.TRUE.equals(comment.getIsHidden()));
        return commentRepository.save(comment);
    }

    @Override
    @Transactional
    public void softDeleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment khong ton tai"));
        comment.setIsDeleted(true);
        comment.setIsHidden(true);
        commentRepository.save(comment);
    }

    @Override
    @Transactional
    public Comment restoreComment(Long commentId) {
        Comment comment = commentRepository.findAnyCommentById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment khong ton tai"));

        Long sentenceId = comment.getSentence() != null ? comment.getSentence().getId() : null;
        Long userId = comment.getUser() != null ? comment.getUser().getId() : null;
        Long parentId = comment.getParent() != null ? comment.getParent().getId() : null;

        Sentence sentence = sentenceId != null ? sentenceRepository.findAnySentenceById(sentenceId).orElse(null) : null;
        User user = userId != null ? userRepository.findAnyUserById(userId).orElse(null) : null;
        Comment parent = parentId != null ? commentRepository.findAnyCommentById(parentId).orElse(null) : null;

        if (sentence == null || Boolean.TRUE.equals(sentence.getIsDeleted())) {
            throw new IllegalStateException("Khong the khoi phuc Comment khi Sentence cha dang bi xoa.");
        }
        validateSentenceHierarchy(sentence);
        if (user == null || Boolean.TRUE.equals(user.getIsDeleted())) {
            throw new IllegalStateException("Khong the khoi phuc Comment khi User da bi xoa.");
        }
        if (parentId != null && parent == null) {
            throw new IllegalStateException("Khong the khoi phuc Comment vi comment cha khong con ton tai.");
        }
        if (parent != null && Boolean.TRUE.equals(parent.getIsDeleted())) {
            throw new IllegalStateException("Khong the khoi phuc Comment khi comment cha van nam trong thung rac.");
        }

        comment.setSentence(sentence);
        comment.setUser(user);
        comment.setParent(parent);
        comment.setIsDeleted(false);
        comment.setIsHidden(false);
        return commentRepository.save(comment);
    }

    @Override
    @Transactional
    public void hardDeleteComment(Long commentId) {
        hardDeleteCommentTree(commentId);
    }

    private void hardDeleteCommentTree(Long commentId) {
        Comment comment = commentRepository.findAnyCommentById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment khong ton tai"));

        for (Comment child : commentRepository.findAnyByParentId(commentId)) {
            hardDeleteCommentTree(child.getId());
        }

        commentVoteRepository.deleteByCommentId(comment.getId());
        commentRepository.deleteById(comment.getId());
    }

    private void validateSentenceHierarchy(Sentence sentence) {
        if (sentence.getStatus() == ContentStatus.ARCHIVED) {
            throw new IllegalStateException("Khong the khoi phuc Comment khi Sentence cha dang o trang thai ARCHIVED. Hay khoi phuc Sentence truoc.");
        }
        if (sentence.getLesson() == null || Boolean.TRUE.equals(sentence.getLesson().getIsDeleted())) {
            throw new IllegalStateException("Khong the khoi phuc Comment khi Lesson cha dang bi xoa.");
        }
        if (sentence.getLesson().getStatus() == ContentStatus.ARCHIVED) {
            throw new IllegalStateException("Khong the khoi phuc Comment khi Lesson cha dang o trang thai ARCHIVED. Hay khoi phuc Lesson truoc.");
        }
        if (sentence.getLesson().getSection() == null || Boolean.TRUE.equals(sentence.getLesson().getSection().getIsDeleted())) {
            throw new IllegalStateException("Khong the khoi phuc Comment khi Section cha dang bi xoa.");
        }
        if (sentence.getLesson().getSection().getStatus() == ContentStatus.ARCHIVED) {
            throw new IllegalStateException("Khong the khoi phuc Comment khi Section cha dang o trang thai ARCHIVED. Hay khoi phuc Section truoc.");
        }
        if (sentence.getLesson().getSection().getCategory() == null
                || Boolean.TRUE.equals(sentence.getLesson().getSection().getCategory().getIsDeleted())) {
            throw new IllegalStateException("Khong the khoi phuc Comment khi Category cha dang bi xoa.");
        }
        if (sentence.getLesson().getSection().getCategory().getStatus() == ContentStatus.ARCHIVED) {
            throw new IllegalStateException("Khong the khoi phuc Comment khi Category cha dang o trang thai ARCHIVED. Hay khoi phuc Category truoc.");
        }
    }
}

