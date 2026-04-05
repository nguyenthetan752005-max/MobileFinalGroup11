package hcmute.edu.vn.nguyenthetan.domain.repository;

import java.util.List;

import hcmute.edu.vn.nguyenthetan.domain.model.comment.Comment;

public interface CommentRepository {

    List<Comment> getComments(long sentenceId);
}
