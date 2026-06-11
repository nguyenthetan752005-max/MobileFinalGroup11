package com.english.learning.service.admin;

import com.english.learning.entity.Comment;

public interface AdminCommentModerationService {
    Comment toggleHideComment(Long commentId);

    void softDeleteComment(Long commentId);

    Comment restoreComment(Long commentId);

    void hardDeleteComment(Long commentId);
}

