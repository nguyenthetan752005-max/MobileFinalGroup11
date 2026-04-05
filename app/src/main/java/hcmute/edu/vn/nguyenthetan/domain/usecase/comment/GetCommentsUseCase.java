package hcmute.edu.vn.nguyenthetan.domain.usecase.comment;

import java.util.List;

import hcmute.edu.vn.nguyenthetan.domain.model.comment.Comment;
import hcmute.edu.vn.nguyenthetan.domain.repository.CommentRepository;

public class GetCommentsUseCase {

    private final CommentRepository repository;

    public GetCommentsUseCase(CommentRepository repository) {
        this.repository = repository;
    }

    public List<Comment> execute(long sentenceId) {
        return repository.getComments(sentenceId);
    }
}
