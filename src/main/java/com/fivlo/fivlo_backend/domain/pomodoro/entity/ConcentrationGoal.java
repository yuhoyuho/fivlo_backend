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
 * D-Day 탭에서 사용하는 프리미엄 전용 목표를 저장
 * 포모도로의 일반 목표(PomodoroGoal)와는 별개로, 기간이 명확한 목표를 관리
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

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ==================== 생성자 ====================
    
    @Builder
    public ConcentrationGoal(User user, String name, LocalDate startDate, LocalDate endDate) {
        this.user = user;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // ==================== 비즈니스 메서드 ====================
    
    /**
     * 목표 정보 수정
     */
    public void update(String name, LocalDate startDate, LocalDate endDate) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name;
        }
        if (startDate != null) {
            this.startDate = startDate;
        }
        if (endDate != null) {
            this.endDate = endDate;
        }
    }

    /**
     * 총 목표 기간 계산 (일 수)
     */
    public long getTotalDays() {
        return ChronoUnit.DAYS.between(startDate, endDate) + 1; // +1은 시작일 포함
    }

    /**
     * 현재까지 경과 일수 계산
     */
    public long getElapsedDays() {
        LocalDate today = LocalDate.now();
        if (today.isBefore(startDate)) {
            return 0;
        }
        if (today.isAfter(endDate)) {
            return getTotalDays();
        }
        return ChronoUnit.DAYS.between(startDate, today) + 1;
    }

    /**
     * 남은 일수 계산
     */
    public long getRemainingDays() {
        LocalDate today = LocalDate.now();
        if (today.isAfter(endDate)) {
            return 0;
        }
        if (today.isBefore(startDate)) {
            return ChronoUnit.DAYS.between(today, endDate);
        }
        return ChronoUnit.DAYS.between(today, endDate);
    }

    /**
     * 목표가 활성 상태인지 확인 (현재 날짜가 목표 기간 내)
     */
    public boolean isActive() {
        LocalDate today = LocalDate.now();
        return !today.isBefore(startDate) && !today.isAfter(endDate);
    }

    /**
     * 목표가 완료되었는지 확인 (종료일이 지났는지)
     */
    public boolean isCompleted() {
        return LocalDate.now().isAfter(endDate);
    }
}
