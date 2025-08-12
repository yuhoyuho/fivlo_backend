package com.fivlo.fivlo_backend.domain.reminder.entity;

import com.fivlo.fivlo_backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 망각방지 알림 엔티티
 * 사용자가 설정한 개별 망각방지 알림 항목의 정보를 저장하는 테이블
 */
@Entity
@Table(name = "forgetting_prevention_reminders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class ForgettingPreventionReminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "title", length = 255, nullable = false)
    private String title;

    @Column(name = "alarm_time", nullable = false)
    private LocalTime alarmTime;

    @Column(name = "repetition_days", length = 7, nullable = false)
    private String repetitionDays;

    @Column(name = "location_name", length = 255)
    private String locationName;

    @Column(name = "location_address", length = 255)
    private String locationAddress;

    @Column(name = "location_latitude", precision = 10, scale = 8)
    private BigDecimal locationLatitude;

    @Column(name = "location_longitude", precision = 11, scale = 8)
    private BigDecimal locationLongitude;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ==================== 생성자 ====================
    
    @Builder
    public ForgettingPreventionReminder(User user, String title, LocalTime alarmTime, String repetitionDays,
                                      String locationName, String locationAddress, 
                                      BigDecimal locationLatitude, BigDecimal locationLongitude) {
        this.user = user;
        this.title = title;
        this.alarmTime = alarmTime;
        this.repetitionDays = repetitionDays != null ? repetitionDays : "";
        this.locationName = locationName;
        this.locationAddress = locationAddress;
        this.locationLatitude = locationLatitude;
        this.locationLongitude = locationLongitude;
    }

    // ==================== 비즈니스 메서드 ====================
    
    /**
     * 알림 기본 정보 수정
     */
    public void updateBasicInfo(String title, LocalTime alarmTime, String repetitionDays) {
        if (title != null && !title.trim().isEmpty()) {
            this.title = title;
        }
        if (alarmTime != null) {
            this.alarmTime = alarmTime;
        }
        if (repetitionDays != null) {
            this.repetitionDays = repetitionDays;
        }
    }

    /**
     * 위치 정보 수정 (프리미엄 기능)
     */
    public void updateLocationInfo(String locationName, String locationAddress, 
                                 BigDecimal locationLatitude, BigDecimal locationLongitude) {
        this.locationName = locationName;
        this.locationAddress = locationAddress;
        this.locationLatitude = locationLatitude;
        this.locationLongitude = locationLongitude;
    }

    /**
     * 위치 정보 제거
     */
    public void removeLocationInfo() {
        this.locationName = null;
        this.locationAddress = null;
        this.locationLatitude = null;
        this.locationLongitude = null;
    }

    /**
     * 위치 설정 여부 확인
     */
    public boolean hasLocationSet() {
        return locationLatitude != null && locationLongitude != null;
    }

    /**
     * 특정 요일에 활성화되는지 확인
     * @param dayOfWeek 요일 (MON, TUE, WED, THU, FRI, SAT, SUN)
     */
    public boolean isActiveOnDay(String dayOfWeek) {
        return repetitionDays != null && repetitionDays.contains(dayOfWeek);
    }

    /**
     * 반복 요일 배열 반환
     */
    public String[] getRepetitionDaysArray() {
        if (repetitionDays == null || repetitionDays.isEmpty()) {
            return new String[0];
        }
        // 예: "MON,WED,FRI" -> ["MON", "WED", "FRI"]
        return repetitionDays.split(",");
    }
}
