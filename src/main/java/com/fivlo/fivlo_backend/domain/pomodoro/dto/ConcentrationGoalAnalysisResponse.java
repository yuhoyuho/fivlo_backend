package com.fivlo.fivlo_backend.domain.pomodoro.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * API 35: D-Day 목표 분석 응답 DTO (프리미엄 전용)
 */
@Getter
@Builder
public class ConcentrationGoalAnalysisResponse {

    private GoalInfo goalInfo;
    private List<DailyCalendar> dailyCalendar;

    @Getter
    @Builder
    public static class GoalInfo {
        private String name;               // 목표 이름
        private Long totalDays;            // 총 기간 (일)
        private Long daysFocused;          // 집중한 일 수
        private Integer totalFocusTime;    // 총 집중 시간 (초)
        private Double achievementRate;    // 목표 달성률 (%)
        private Long remainingDays;        // 남은 일수
    }

    @Getter
    @Builder
    public static class DailyCalendar {
        private String date;               // 날짜 (YYYY-MM-DD)
        private Integer durationInSeconds; // 해당 날짜의 총 집중 시간 (초)
        private String obooniImageType;    // 오분이 이미지 타입 (GRAY_SAD, BROWN_NEUTRAL, RED_HAPPY)
    }

    /**
     * D-Day 목표 생성 응답 DTO
     */
    @Getter
    @Builder
    public static class ConcentrationGoalCreateResponse {
        private Long id;                   // 생성된 목표 ID
        private String message;            // 응답 메시지
        
        public static ConcentrationGoalCreateResponse success(Long goalId) {
            return ConcentrationGoalCreateResponse.builder()
                    .id(goalId)
                    .message("D-Day 목표가 성공적으로 생성되었습니다.")
                    .build();
        }
    }

    /**
     * 비어있는 D-Day 분석 결과 생성
     */
    public static ConcentrationGoalAnalysisResponse empty(String goalName) {
        return ConcentrationGoalAnalysisResponse.builder()
                .goalInfo(GoalInfo.builder()
                        .name(goalName)
                        .totalDays(0L)
                        .daysFocused(0L)
                        .totalFocusTime(0)
                        .achievementRate(0.0)
                        .remainingDays(0L)
                        .build())
                .dailyCalendar(List.of())
                .build();
    }

    /**
     * 오분이 이미지 타입 결정 (집중 시간에 따라)
     */
    public static String determineObooniImageType(int durationInSeconds) {
        int durationInMinutes = durationInSeconds / 60;

        if (durationInMinutes == 0) {
            return "GRAY_SAD";        // 0분: 회색 슬픔 오분이
        } else if (durationInMinutes < 60) {
            return "GRAY_SAD";        // 0~1시간: 회색 슬픔 오분이
        } else if (durationInMinutes < 120) {
            return "BROWN_NEUTRAL";   // 1~2시간: 갈색 무뚝뚝 오분이
        } else {
            return "RED_HAPPY";       // 2시간+: 빨간색 기쁨 오분이
        }
    }

    /**
     * D-Day 목표 목록 조회 응답 DTO
     */
    @Getter
    @Builder
    public static class ConcentrationGoalListResponse {
        private List<ConcentrationGoalItem> goals;
    }

    /**
     * D-Day 목표 목록 아이템 DTO
     */
    @Getter
    @Builder
    public static class ConcentrationGoalItem {
        private Long id;                   // 목표 ID
        private String name;               // 목표 이름
        private String startDate;          // 시작일 (YYYY-MM-DD)
        private String endDate;            // 종료일 (YYYY-MM-DD)
        private Long totalDays;            // 총 기간 (일)
        private Long elapsedDays;          // 경과 일수
        private Long remainingDays;        // 남은 일수
        private Boolean isActive;          // 활성 상태 (현재 진행 중인지)
        private Boolean isCompleted;       // 완료 상태 (종료일이 지났는지)
    }
}
