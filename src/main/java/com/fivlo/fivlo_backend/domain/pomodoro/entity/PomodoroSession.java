package com.fivlo.fivlo_backend.domain.pomodoro.entity;

import com.fivlo.fivlo_backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 포모도로 세션 엔티티
 * 포모도로 세션의 기록을 저장하여 '집중도 분석' 기능을 지원
 */
@Entity
@Table(name = "pomodoro_sessions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Builder
public class PomodoroSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pomodoro_goal_id", nullable = false)
    private PomodoroGoal pomodoroGoal;

    @Column(name = "duration_in_seconds", nullable = false)
    @Builder.Default
    private Integer durationInSeconds = 0;

    @Column(name = "is_cycle_completed", nullable = false)
    @Builder.Default
    private Boolean isCycleCompleted = false;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ==================== 생성자 ====================
    
    @Builder
    public PomodoroSession(User user, PomodoroGoal pomodoroGoal, Integer durationInSeconds, Boolean isCycleCompleted) {
        this.user = user;
        this.pomodoroGoal = pomodoroGoal;
        this.durationInSeconds = durationInSeconds;
        this.isCycleCompleted = isCycleCompleted != null ? isCycleCompleted : false;
    }

    // ==================== 비즈니스 메서드 ====================

    /**
     * 세션 지속시간 수정
     */
    public void updateDurationInSeconds(Integer durationInSeconds) {
        this.durationInSeconds = durationInSeconds;
    }

    /**
     * 30분(25분 집중+5분 휴식) 1사이클 완료 여부 확인
     */
    public boolean isFullCycleCompleted() {
        return isCycleCompleted;
    }

    /**
     * 집중 시간을 분 단위로 반환
     */
    public double getDurationInMinutes() {
        return durationInSeconds / 60.0;
    }

    /**
     * 집중 시간을 시간 단위로 반환
     */
    public double getDurationInHours() {
        return durationInSeconds / 3600.0;
    }

    /**
     * 사이클 완료 여부 업데이트
     */
    public void updateCycleCompletedStatus(Boolean isCycleCompleted) {
        this.isCycleCompleted = isCycleCompleted;
    }
}
