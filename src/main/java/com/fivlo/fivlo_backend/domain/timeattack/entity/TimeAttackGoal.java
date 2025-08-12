package com.fivlo.fivlo_backend.domain.timeattack.entity;

import com.fivlo.fivlo_backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 타임어택 목표 엔티티
 * 타임어택 기능에서 사용되는 목적을 저장
 * 미리 정의된 목적과 사용자가 추가한 목적을 모두 관리
 */
@Entity
@Table(name = "time_attack_goals")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class TimeAttackGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "is_predefined", nullable = false)
    private Boolean isPredefined = false;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ==================== 생성자 ====================
    
    @Builder
    public TimeAttackGoal(User user, String name, Boolean isPredefined) {
        this.user = user;
        this.name = name;
        this.isPredefined = isPredefined != null ? isPredefined : false;
    }

    // ==================== 비즈니스 메서드 ====================
    
    /**
     * 목적 이름 수정
     */
    public void updateName(String name) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name;
        }
    }

    /**
     * 미리 정의된 목적인지 확인
     */
    public boolean isPredefinedGoal() {
        return isPredefined;
    }

    /**
     * 사용자가 직접 추가한 목적인지 확인
     */
    public boolean isUserCreatedGoal() {
        return !isPredefined;
    }
}
