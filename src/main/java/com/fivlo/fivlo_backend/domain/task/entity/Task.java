package com.fivlo.fivlo_backend.domain.task.entity;

import com.fivlo.fivlo_backend.domain.user.entity.User;
import com.fivlo.fivlo_backend.domain.category.entity.Category;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Task 엔티티
 * 사용자가 설정한 일정을 저장하는 테이블
 */
@Entity
@Table(name = "tasks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "repeat_type", nullable = false)
    private RepeatType repeatType = RepeatType.NONE;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "is_linked_to_growth_album", nullable = false)
    private Boolean isLinkedToGrowthAlbum = false;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ==================== 생성자 ====================
    
    @Builder
    public Task(User user, Category category, String content, LocalDate dueDate,
                Boolean isCompleted, RepeatType repeatType, LocalDate endDate,
                Boolean isLinkedToGrowthAlbum) {
        this.user = user;
        this.category = category;
        this.content = content;
        this.dueDate = dueDate;
        this.isCompleted = isCompleted != null ? isCompleted : false;
        this.repeatType = repeatType != null ? repeatType : RepeatType.NONE;
        this.endDate = endDate;
        this.isLinkedToGrowthAlbum = isLinkedToGrowthAlbum != null ? isLinkedToGrowthAlbum : false;
    }

    // ==================== 비즈니스 메서드 ====================
    
    /**
     * Task 내용 수정
     */
    public void updateContent(String content) {
        if (content != null && !content.trim().isEmpty()) {
            this.content = content;
        }
    }

    /**
     * Task 카테고리 변경
     */
    public void updateCategory(Category category) {
        this.category = category;
    }

    /**
     * Task 완료 상태 변경
     */
    public void updateCompletionStatus(Boolean isCompleted) {
        this.isCompleted = isCompleted != null ? isCompleted : false;
    }

    /**
     * Task 반복 설정 변경
     */
    public void updateRepeatSettings(RepeatType repeatType, LocalDate endDate) {
        this.repeatType = repeatType != null ? repeatType : RepeatType.NONE;
        this.endDate = endDate;
    }

    /**
     * 성장앨범 연동 설정 변경
     */
    public void updateGrowthAlbumLink(Boolean isLinkedToGrowthAlbum) {
        this.isLinkedToGrowthAlbum = isLinkedToGrowthAlbum != null ? isLinkedToGrowthAlbum : false;
    }

    /**
     * Task 완료 처리
     */
    public void complete() {
        this.isCompleted = true;
    }

    /**
     * Task 미완료 처리
     */
    public void incomplete() {
        this.isCompleted = false;
    }

    // ==================== Enum 클래스 ====================
    
    /**
     * Task 반복 유형
     */
    public enum RepeatType {
        DAILY("매일 반복"),
        NONE("반복 없음");

        private final String description;

        RepeatType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
