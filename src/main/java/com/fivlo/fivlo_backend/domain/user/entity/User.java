package com.fivlo.fivlo_backend.domain.user.entity;

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

/**
 * 사용자 엔티티
 * 앱에 가입한 사용자의 정보를 저장하는 테이블
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "email", length = 255, unique = true, nullable = false)
    private String email;

    @Column(name = "password", length = 255)
    private String password;

    @Column(name = "social_id", length = 255)
    private String socialId;

    @Enumerated(EnumType.STRING)
    @Column(name = "social_provider", length = 50)
    private SocialProvider socialProvider;

    @Column(name = "nickname", length = 100, nullable = false)
    private String nickname;

    @Column(name = "profile_image_url", length = 255)
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "onboarding_type", length = 50)
    private OnboardingType onboardingType;

    @Enumerated(EnumType.STRING)
    @Column(name = "language")
    private Language language;

    @Column(name = "alarm_status", nullable = false)
    private Boolean alarmStatus;

    @Column(name = "is_premium", nullable = false)
    private Boolean isPremium = false;

    @Column(name = "total_coins", nullable = false)
    private Integer totalCoins = 0;

    @Column(name = "last_pomodoro_coin_date")
    private LocalDate lastPomodoroCoinDate;

    @Column(name = "last_reminder_coin_date")
    private LocalDate lastReminderCoinDate;

    @Column(name = "last_attendance_coin_date")
    private LocalDate lastAttendanceCoinDate;

    @Column(name = "last_login")
    private LocalDate lastLogin;

    @Column(name = "last_task_coin_date")
    private LocalDate lastTaskCoinDate;

    @Column(name = "fcm_token", length = 255)
    private String fcmToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.ACTIVE;

    @Column(name = "deactivated_at")
    private LocalDateTime deactivatedAt; // 탈퇴 신청한 날짜

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // ==================== 생성자 ====================
    
    @Builder
    public User(String email, String password, String socialId, SocialProvider socialProvider,
                String nickname, String profileImageUrl, OnboardingType onboardingType,
                Boolean alarmStatus, Boolean isPremium, Integer totalCoins, Status status) {
        this.email = email;
        this.password = password;
        this.socialId = socialId;
        this.socialProvider = socialProvider;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.onboardingType = onboardingType;
        this.alarmStatus = alarmStatus;
        this.status = status;
        this.isPremium = isPremium != null ? isPremium : false;
        this.totalCoins = totalCoins != null ? totalCoins : 0;
    }

    // ==================== 비즈니스 메서드 ====================
    
    /**
     * 온보딩 목표 설정/수정
     */
    public void updateOnboardingType(OnboardingType onboardingType) {
        this.onboardingType = onboardingType;
    }

    /**
     * 언어 설정/수정
     */
    public void updateLanguage(Language language) {
        this.language = language;
    }

    /**
     * 알람 on/off 상태 변경
     */
    public void updateAlarmStatus() {
        this.alarmStatus = !this.alarmStatus;
    }

    /**
     * 프로필 정보 수정
     */
    public void updateProfile(String nickname, String profileImageUrl) {
        if (nickname != null && !nickname.trim().isEmpty()) {
            this.nickname = nickname;
        }
        if (profileImageUrl != null) {
            this.profileImageUrl = profileImageUrl;
        }
    }

    /**
     * 코인 지급
     */
    public void addCoins(Integer amount) {
        if (amount != null && amount > 0) {
            this.totalCoins += amount;
        }
    }

    /**
     * 코인 사용
     */
    public boolean useCoins(Integer amount) {
        if (amount != null && amount > 0 && this.totalCoins >= amount) {
            this.totalCoins -= amount;
            return true;
        }
        return false;
    }

    /**
     * 포모도로 코인 지급일 업데이트
     */
    public void updateLastPomodoroCoinDate(LocalDate date) {
        this.lastPomodoroCoinDate = date;
    }

    /**
     * 일일 완료 코인 지급일 업데이트
     */
    public void updateLastReminderCoinDate(LocalDate date) {
        this.lastReminderCoinDate = date;
    }

    /**
     * 출석 코인 지급일 업데이트
     */
    public void updateLastAttendanceCoinDate(LocalDate date) {
        this.lastAttendanceCoinDate = date;
    }

    /**
     * 마지막 로그인 날짜 업데이트
     */
    public void updateLastLogin(LocalDate date) {
        this.lastLogin = date;
    }

    /**
     * Task 코인 지급일 업데이트
     */
    public void updateLastTaskCoinDate(LocalDate date) {
        this.lastTaskCoinDate = date;
    }


    /**
     * 프리미엄 상태 변경
     */
    public void updatePremiumStatus(Boolean isPremium) {
        this.isPremium = isPremium != null ? isPremium : false;
    }

    /**
     * 회원 탈퇴 신청 (계정 비활성화 상태로 바꿈)
     */
    public void deactivate() {
        this.status = Status.DEACTIVATED;
        this.deactivatedAt = LocalDateTime.now();
    }

    /**
     * 유예기간 1주일 내로 로그인하면 다시 활성화 상태로 바꿈
     */
    public void restore() {
        this.status = Status.ACTIVE;
        this.deactivatedAt = null;
    }

    /**
     * 최종 삭제 (7일 지난 경우)
     */
    public boolean isReadyForDeletion() {
        return this.status == Status.DEACTIVATED &&
                this.deactivatedAt != null &&
                this.deactivatedAt.isBefore(LocalDateTime.now().minusDays(7));
    }

    /**
     * 사용자 status 변경
     */
    public void updateStatus(Status status) {
        this.status = status;
    }

    /**
     * 소셜 로그인 연동
     */
    public void linkSocialAccount(String socialId, SocialProvider socialProvider) {
        this.socialId = socialId;
        this.socialProvider = socialProvider;
    }

    public void updateFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    // ==================== Enum 클래스들 ====================
    
    /**
     * 소셜 로그인 제공자
     */
    public enum SocialProvider {
        GOOGLE, KAKAO
    }

    /**
     * 온보딩 목표 타입
     */
    public enum OnboardingType {
        집중력_개선("집중력 개선"),
        루틴_형성("루틴 형성"), 
        목표_관리("목표 관리");

        private final String description;

        OnboardingType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 언어 설정
     */
    public enum Language {
        한국어,
        English
    }

    /**
     * 계정 상태
     */
    public enum Status {
        ACTIVE, // 활성화
        DEACTIVATED, // 비활성화
        DELETED // 삭제 (Spring Batch로 처리)
    }
}
