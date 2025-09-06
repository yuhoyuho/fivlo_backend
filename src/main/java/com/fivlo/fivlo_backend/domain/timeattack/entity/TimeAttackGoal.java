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
 * 
 * 다국어 지원:
 * - 미리 정의된 목적: nameKey를 사용하여 프론트엔드 i18n으로 처리
 * - 사용자 추가 목적: customName을 사용하여 사용자 입력 텍스트 그대로 저장
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

    /**
     * 미리 정의된 목적의 i18n 키 (예: "timeAttack.goal.outingPrep")
     * isPredefined가 true일 때만 사용
     */
    @Column(name = "name_key", length = 100)
    private String nameKey;

    /**
     * 사용자가 직접 추가한 목적의 이름
     * isPredefined가 false일 때만 사용
     */
    @Column(name = "custom_name", length = 255)
    private String customName;

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
    public TimeAttackGoal(User user, String nameKey, String customName, Boolean isPredefined) {
        this.user = user;
        this.nameKey = nameKey;
        this.customName = customName;
        this.isPredefined = isPredefined != null ? isPredefined : false;
        
        // 데이터 무결성 검증
        if (this.isPredefined && (nameKey == null || nameKey.trim().isEmpty())) {
            throw new IllegalArgumentException("미리 정의된 목적은 nameKey가 필수입니다.");
        }
        if (!this.isPredefined && (customName == null || customName.trim().isEmpty())) {
            throw new IllegalArgumentException("사용자 추가 목적은 customName이 필수입니다.");
        }
    }

    // ==================== 비즈니스 메서드 ====================
    
    /**
     * 표시할 이름 조회
     * 미리 정의된 목적은 nameKey 반환 (프론트에서 i18n 처리)
     * 사용자 추가 목적은 customName 반환
     */
    public String getDisplayName() {
        if (isPredefined) {
            return nameKey;
        } else {
            return customName;
        }
    }

    /**
     * 사용자 추가 목적의 이름 수정
     */
    public void updateCustomName(String customName) {
        if (!isPredefined && customName != null && !customName.trim().isEmpty()) {
            this.customName = customName;
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

    /**
     * i18n 키를 사용하는 목적인지 확인
     */
    public boolean usesI18nKey() {
        return isPredefined && nameKey != null;
    }

    /**
     * 커스텀 이름을 사용하는 목적인지 확인
     */
    public boolean usesCustomName() {
        return !isPredefined && customName != null;
    }
}
