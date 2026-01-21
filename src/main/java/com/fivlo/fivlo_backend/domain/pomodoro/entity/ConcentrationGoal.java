package com.fivlo.fivlo_backend.domain.pomodoro.entity;

import com.fivlo.fivlo_backend.domain.user.entity.User;
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
import java.time.temporal.ChronoUnit;

/**
 * 집중도 목표 엔티티 (D-Day 목표)
 * D-Day 탭에서 사용하는 프리미엄 전용 하루 단위 목표를 저장
 * 포모도로의 일반 목표(PomodoroGoal)와는 별개로, 특정 날짜에 달성할 집중 시간 목표를 관리
 */
@Entity
@Table(name = "concentration_goals")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class ConcentrationGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "target_date", nullable = false)
    private LocalDate targetDate;

    @Column(name = "target_focus_time_in_seconds", nullable = false)
    @Builder.Default
    private Integer targetFocusTimeInSeconds = 7200; // 기본값: 2시간

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ==================== 생성자 ====================

    @Builder
    public ConcentrationGoal(User user, String name, LocalDate targetDate, Integer targetFocusTimeInSeconds) {
        this.user = user;
        this.name = name;
        this.targetDate = targetDate;
        this.targetFocusTimeInSeconds = targetFocusTimeInSeconds != null ? targetFocusTimeInSeconds : 7200;
    }

    // ==================== 비즈니스 메서드 ====================

    /**
     * 목표 정보 수정
     */
    public void update(String name, LocalDate targetDate, Integer targetFocusTimeInSeconds) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name;
        }
        if (targetDate != null) {
            this.targetDate = targetDate;
        }
        if (targetFocusTimeInSeconds != null && targetFocusTimeInSeconds > 0) {
            this.targetFocusTimeInSeconds = targetFocusTimeInSeconds;
        }
    }

    /**
     * 목표가 오늘인지 확인
     */
    public boolean isToday() {
        return LocalDate.now().equals(targetDate);
    }

    /**
     * 목표가 활성 상태인지 확인 (오늘이 목표 날짜인지)
     */
    public boolean isActive() {
        return isToday();
    }

    /**
     * 목표가 완료되었는지 확인 (목표 날짜가 지났는지)
     */
    public boolean isCompleted() {
        return LocalDate.now().isAfter(targetDate);
    }

    /**
     * 목표까지 남은 일수 계산 (D-Day)
     */
    public long getDaysUntilTarget() {
        LocalDate today = LocalDate.now();
        if (today.isAfter(targetDate)) {
            return 0;
        }
        return ChronoUnit.DAYS.between(today, targetDate);
    }

    /**
     * 목표 집중 시간을 분 단위로 반환
     */
    public int getTargetFocusTimeInMinutes() {
        return targetFocusTimeInSeconds / 60;
    }

    /**
     * 목표 집중 시간을 시간 단위로 반환
     */
    public double getTargetFocusTimeInHours() {
        return targetFocusTimeInSeconds / 3600.0;
    }
}
