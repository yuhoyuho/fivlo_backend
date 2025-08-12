package com.fivlo.fivlo_backend.domain.reminder.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 일일 알림 완료 엔티티
 * 매일의 망각방지 알림 항목별 완료 상태를 기록하는 테이블
 * 이는 코인 지급 로직의 판단 기준으로 사용됨
 */
@Entity
@Table(name = "daily_reminder_completions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyReminderCompletion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reminder_id", nullable = false)
    private ForgettingPreventionReminder reminder;

    @Column(name = "completion_date", nullable = false)
    private LocalDate completionDate;

    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted = false;

    // ==================== 생성자 ====================
    
    @Builder
    public DailyReminderCompletion(ForgettingPreventionReminder reminder, LocalDate completionDate, Boolean isCompleted) {
        this.reminder = reminder;
        this.completionDate = completionDate;
        this.isCompleted = isCompleted != null ? isCompleted : false;
    }

    // ==================== 비즈니스 메서드 ====================
    
    /**
     * 완료 상태 변경
     */
    public void updateCompletionStatus(Boolean isCompleted) {
        this.isCompleted = isCompleted != null ? isCompleted : false;
    }

    /**
     * 완료 처리
     */
    public void complete() {
        this.isCompleted = true;
    }

    /**
     * 미완료 처리
     */
    public void incomplete() {
        this.isCompleted = false;
    }

    /**
     * 완료 상태 토글
     */
    public void toggleCompletion() {
        this.isCompleted = !this.isCompleted;
    }

    /**
     * 완료되었는지 확인
     */
    public boolean isCompletedToday() {
        return isCompleted;
    }
}
