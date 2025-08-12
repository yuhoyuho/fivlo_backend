package com.fivlo.fivlo_backend.domain.growth.entity;

import com.fivlo.fivlo_backend.domain.task.entity.Task;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 성장앨범 엔티티
 * 성장앨범에 저장될 사진과 메모 정보를 저장하는 테이블
 */
@Entity
@Table(name = "growth_albums")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class GrowthAlbum {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", unique = true, nullable = false)
    private Task task;

    @Column(name = "photo_url", length = 255, nullable = false)
    private String photoUrl;

    @Column(name = "memo", columnDefinition = "TEXT")
    private String memo;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ==================== 생성자 ====================
    
    @Builder
    public GrowthAlbum(Task task, String photoUrl, String memo) {
        this.task = task;
        this.photoUrl = photoUrl;
        this.memo = memo;
    }

    // ==================== 비즈니스 메서드 ====================
    
    /**
     * 메모 수정
     */
    public void updateMemo(String memo) {
        this.memo = memo;
    }

    /**
     * 사진 URL 수정
     */
    public void updatePhotoUrl(String photoUrl) {
        if (photoUrl != null && !photoUrl.trim().isEmpty()) {
            this.photoUrl = photoUrl;
        }
    }
}
