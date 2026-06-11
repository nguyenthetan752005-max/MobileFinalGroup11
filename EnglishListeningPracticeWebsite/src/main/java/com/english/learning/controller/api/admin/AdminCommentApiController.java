package com.english.learning.controller.api.admin;

import com.english.learning.service.admin.AdminCommentModerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminCommentApiController {

    private final AdminCommentModerationService adminCommentModerationService;

    @PatchMapping("/comments/{id}/toggle-hide")
    public ResponseEntity<Map<String, Object>> toggleHideComment(@PathVariable Long id) {
        try {
            boolean hidden = adminCommentModerationService.toggleHideComment(id).getIsHidden();
            Map<String, Object> response = AdminApiResponses.successBody();
            response.put("hidden", hidden);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return AdminApiResponses.badRequest(e.getMessage());
        }
    }

    @DeleteMapping("/comments/{id}")
    public ResponseEntity<Map<String, Object>> softDeleteComment(@PathVariable Long id) {
        return AdminApiResponses.action(() -> adminCommentModerationService.softDeleteComment(id), "Da xoa mem comment.");
    }

    @PostMapping("/comments/{id}/restore")
    public ResponseEntity<Map<String, Object>> restoreComment(@PathVariable Long id) {
        return AdminApiResponses.action(() -> adminCommentModerationService.restoreComment(id), "Da khoi phuc comment.");
    }

    @DeleteMapping("/trash/comments/{id}")
    public ResponseEntity<Map<String, Object>> hardDeleteComment(@PathVariable Long id) {
        return AdminApiResponses.action(() -> adminCommentModerationService.hardDeleteComment(id), "Da xoa vinh vien comment.");
    }
}
