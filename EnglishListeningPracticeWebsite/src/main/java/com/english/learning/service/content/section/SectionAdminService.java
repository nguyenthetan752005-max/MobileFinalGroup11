package com.english.learning.service.content.section;

import com.english.learning.dto.AdminSectionRequest;
import com.english.learning.entity.Section;

public interface SectionAdminService {
    Section createSection(AdminSectionRequest request);

    Section updateSection(Long id, AdminSectionRequest request);

    void deleteSection(Long id);

    void restoreSection(Long id);

    void hardDeleteSection(Long id);
}

