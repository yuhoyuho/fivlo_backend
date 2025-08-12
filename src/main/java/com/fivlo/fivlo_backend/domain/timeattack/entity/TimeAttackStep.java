package com.fivlo.fivlo_backend.domain.timeattack.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 타임어택 단계 엔티티
 * 각 타임어택 세션에 포함된 단계별 일정과 시간을 저장
 */
@Entity
@Table(name = "time_attack_steps")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TimeAttackStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_attack_session_id", nullable = false)
    private TimeAttackSession timeAttackSession;

    @Column(name = "step_order", nullable = false)
    private Integer stepOrder;

    @Column(name = "content", length = 255, nullable = false)
    private String content;

    @Column(name = "duration_in_seconds", nullable = false)
    private Integer durationInSeconds;

    // ==================== 생성자 ====================
    
    @Builder
    public TimeAttackStep(TimeAttackSession timeAttackSession, Integer stepOrder, String content, Integer durationInSeconds) {
        this.timeAttackSession = timeAttackSession;
        this.stepOrder = stepOrder;
        this.content = content;
        this.durationInSeconds = durationInSeconds;
    }

    // ==================== 비즈니스 메서드 ====================
    
    /**
     * 단계 내용 수정
     */
    public void updateContent(String content) {
        if (content != null && !content.trim().isEmpty()) {
            this.content = content;
        }
    }

    /**
     * 단계 시간 수정
     */
    public void updateDuration(Integer durationInSeconds) {
        if (durationInSeconds != null && durationInSeconds > 0) {
            this.durationInSeconds = durationInSeconds;
        }
    }

    /**
     * 단계 순서 수정
     */
    public void updateStepOrder(Integer stepOrder) {
        if (stepOrder != null && stepOrder > 0) {
            this.stepOrder = stepOrder;
        }
    }

    /**
     * 단계 시간을 분 단위로 반환
     */
    public double getDurationInMinutes() {
        return durationInSeconds / 60.0;
    }

    /**
     * 단계 시간을 시간 단위로 반환
     */
    public double getDurationInHours() {
        return durationInSeconds / 3600.0;
    }
}
