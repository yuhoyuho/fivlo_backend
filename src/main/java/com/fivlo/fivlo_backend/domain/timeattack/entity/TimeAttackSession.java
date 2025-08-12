package com.fivlo.fivlo_backend.domain.timeattack.entity;

import com.fivlo.fivlo_backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 타임어택 세션 엔티티
 * 사용자가 실행한 타임어택 세션의 기록을 저장
 */
@Entity
@Table(name = "time_attack_sessions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class TimeAttackSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_attack_goal_id", nullable = false)
    private TimeAttackGoal timeAttackGoal;

    @Column(name = "total_duration_in_seconds", nullable = false)
    private Integer totalDurationInSeconds;

    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted = false;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ==================== 생성자 ====================
    
    @Builder
    public TimeAttackSession(User user, TimeAttackGoal timeAttackGoal, Integer totalDurationInSeconds, Boolean isCompleted) {
        this.user = user;
        this.timeAttackGoal = timeAttackGoal;
        this.totalDurationInSeconds = totalDurationInSeconds;
        this.isCompleted = isCompleted != null ? isCompleted : false;
    }

    // ==================== 비즈니스 메서드 ====================
    
    /**
     * 세션 완료 처리
     */
    public void complete() {
        this.isCompleted = true;
    }

    /**
     * 세션 완료 상태 업데이트
     */
    public void updateCompletionStatus(Boolean isCompleted) {
        this.isCompleted = isCompleted != null ? isCompleted : false;
    }

    /**
     * 총 시간을 분 단위로 반환
     */
    public double getTotalDurationInMinutes() {
        return totalDurationInSeconds / 60.0;
    }

    /**
     * 총 시간을 시간 단위로 반환
     */
    public double getTotalDurationInHours() {
        return totalDurationInSeconds / 3600.0;
    }

    /**
     * 세션이 완료되었는지 확인
     */
    public boolean isSessionCompleted() {
        return isCompleted;
    }
}
