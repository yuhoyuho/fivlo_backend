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

import java.time.LocalDateTime;

/**
 * 포모도로 목표 엔티티
 * 사용자가 설정한 포모도로 집중 목표와 색상을 저장
 */
@Entity
@Table(name = "pomodoro_goals")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class PomodoroGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "name", length = 255, nullable = false)
    private String name;

    @Column(name = "color", length = 20, nullable = false)
    private String color;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ==================== 생성자 ====================
    
    @Builder
    public PomodoroGoal(User user, String name, String color) {
        this.user = user;
        this.name = name;
        this.color = color;
    }

    // ==================== 비즈니스 메서드 ====================
    
    /**
     * 목표 정보 수정
     */
    public void update(String name, String color) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name;
        }
        if (color != null && !color.trim().isEmpty()) {
            this.color = color;
        }
    }
}
