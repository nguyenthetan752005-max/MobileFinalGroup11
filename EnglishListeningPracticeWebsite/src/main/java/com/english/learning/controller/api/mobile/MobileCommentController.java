package com.english.learning.controller.api.mobile;

import com.english.learning.dto.mobile.MobileCommentResponse;
import com.english.learning.entity.Comment;
import com.english.learning.entity.CommentVote;
import com.english.learning.mapper.mobile.MobileResponseMapper;
import com.english.learning.service.comment.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST Controller: Mobile Comment API.
 * Provides JSON endpoints for Android comment features (stateless, userId in body).
 */
@RestController
@RequestMapping("/api/mobile")
@RequiredArgsConstructor
public class MobileCommentController {

    private final CommentService commentService;
    private final MobileResponseMapper mapper;

    /**
     * GET /api/mobile/sentences/{sentenceId}/comments
     * Returns top-level comments for a sentence in mobile DTO format.
     */
    @GetMapping("/sentences/{sentenceId}/comments")
    public ResponseEntity<List<MobileCommentResponse>> getComments(@PathVariable Long sentenceId) {
        List<MobileCommentResponse> comments = commentService
                .getTopLevelCommentsWithVotes(sentenceId)
                .stream()
                .map(mapper::toMobileComment)
                .collect(Collectors.toList());
        return ResponseEntity.ok(comments);
    }

    /**
     * GET /api/mobile/comments/{commentId}/replies
     * Returns replies for a comment in mobile DTO format.
     */
    @GetMapping("/comments/{commentId}/replies")
    public ResponseEntity<List<MobileCommentResponse>> getReplies(@PathVariable Long commentId) {
        List<MobileCommentResponse> replies = commentService
                .getRepliesWithVotes(commentId)
                .stream()
                .map(mapper::toMobileComment)
                .collect(Collectors.toList());
        return ResponseEntity.ok(replies);
    }

    /**
     * POST /api/mobile/comments
     * Body: { "sentenceId": 100, "userId": 1, "content": "...", "parentId": null }
     */
    @PostMapping("/comments")
    public ResponseEntity<?> addComment(@RequestBody Map<String, Object> payload) {
        Long sentenceId = payload.get("sentenceId") != null
                ? Long.valueOf(payload.get("sentenceId").toString()) : null;
        Long userId = payload.get("userId") != null
                ? Long.valueOf(payload.get("userId").toString()) : null;
        String content = (String) payload.get("content");
        Long parentId = payload.get("parentId") != null
                ? Long.valueOf(payload.get("parentId").toString()) : null;

        if (sentenceId == null || userId == null || content == null || content.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "sentenceId, userId, and content required"));
        }

        Comment saved = commentService.addComment(sentenceId, userId, content, parentId);
        MobileCommentResponse response = mapper.toMobileComment(saved);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/mobile/comments/{commentId}/vote
     * Body: { "userId": 1, "isLike": true }
     */
    @PostMapping("/comments/{commentId}/vote")
    public ResponseEntity<?> voteComment(
            @PathVariable Long commentId,
            @RequestBody Map<String, Object> payload) {
        Long userId = payload.get("userId") != null
                ? Long.valueOf(payload.get("userId").toString()) : null;
        Boolean isLike = (Boolean) payload.get("isLike");

        if (userId == null || isLike == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "userId and isLike required"));
        }

        CommentVote vote = commentService.voteComment(commentId, userId, isLike);
        if (vote != null) {
            return ResponseEntity.ok(Map.of("success", true, "isLike", vote.getIsLike()));
        }
        return ResponseEntity.ok(Map.of("success", true, "removed", true));
    }

    /**
     * DELETE /api/mobile/comments/{commentId}?userId={userId}
     */
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable Long commentId,
            @RequestParam Long userId) {
        commentService.deleteComment(commentId, userId);
        return ResponseEntity.ok(Map.of("success", true));
    }

    /**
     * GET /api/mobile/profile/{userId}/comments
     * Returns all comments created by a specific user.
     */
    @GetMapping("/profile/{userId}/comments")
    public ResponseEntity<List<MobileCommentResponse>> getUserComments(@PathVariable Long userId) {
        List<MobileCommentResponse> comments = commentService
                .getCommentsByUserId(userId)
                .stream()
                .map(mapper::toMobileComment)
                .collect(Collectors.toList());
        return ResponseEntity.ok(comments);
    }
}
