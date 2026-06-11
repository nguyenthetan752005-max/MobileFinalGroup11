package com.english.learning.entity;

import org.hibernate.annotations.SQLRestriction;
import com.english.learning.enums.ContentStatus;
import jakarta.persistence.Index;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "sections", indexes = {
    @Index(name = "idx_section_status", columnList = "status"),
    @Index(name = "idx_section_category", columnList = "category_id")
})
@SQLRestriction("is_deleted = false")
public class Section {

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContentStatus status = ContentStatus.PUBLISHED;

    @Column(name = "order_index")
    private Integer orderIndex = 0;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    private String name;
    private String description;
    private String folderName;
}
