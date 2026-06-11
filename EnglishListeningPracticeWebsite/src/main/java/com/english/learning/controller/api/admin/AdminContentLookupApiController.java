package com.english.learning.controller.api.admin;

import com.english.learning.entity.Lesson;
import com.english.learning.entity.Section;
import com.english.learning.entity.Sentence;
import com.english.learning.service.content.lesson.LessonService;
import com.english.learning.service.content.section.SectionService;
import com.english.learning.service.content.sentence.SentenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminContentLookupApiController {

    private final SectionService sectionService;
    private final LessonService lessonService;
    private final SentenceService sentenceService;

    @GetMapping("/content/categories/{categoryId}/sections")
    public ResponseEntity<List<Section>> getSectionsByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(sectionService.getSectionsByCategoryId(categoryId));
    }

    @GetMapping("/content/sections/{sectionId}/lessons")
    public ResponseEntity<List<Lesson>> getLessonsBySection(@PathVariable Long sectionId) {
        return ResponseEntity.ok(lessonService.getLessonsBySectionId(sectionId));
    }

    @GetMapping("/content/lessons/{lessonId}/sentences")
    public ResponseEntity<List<Sentence>> getSentencesByLesson(@PathVariable Long lessonId) {
        return ResponseEntity.ok(sentenceService.getSentencesByLessonId(lessonId));
    }
}
