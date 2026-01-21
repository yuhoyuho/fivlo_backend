package com.fivlo.fivlo_backend.domain.pomodoro.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * API 35: D-Day 목표 분석 응답 DTO (프리미엄 전용, 하루 단위 목표)
 */
@Getter
@Builder
public class ConcentrationGoalAnalysisResponse {

    private GoalInfo goalInfo;
    private MonthlyStats monthlyStats;        // 한 달 집중 통계 추가
    private List<DailyCalendar> dailyCalendar;

    @Getter
    @Builder
    public static class GoalInfo {
        private String name;                    // 목표 이름
        private String targetDate;              // 목표 날짜 (YYYY-MM-DD)
        private Integer targetFocusTime;        // 목표 집중 시간 (초)
        private Integer actualFocusTime;        // 실제 집중 시간 (초)
        private Double achievementRate;         // 목표 달성률 (%) = (실제/목표)*100
        private Long daysUntilTarget;           // 목표까지 남은 일수 (D-Day)
        private Boolean isCompleted;            // 목표 달성 여부 (달성률 >= 100%)
    }

    @Getter
    @Builder
    public static class MonthlyStats {
        private Integer totalFocusDays;         // 한 달 동안 집중한 일수
        private Integer totalFocusTime;         // 한 달 총 집중 시간 (초)
        private Double averageDailyFocusTime;   // 한 달 평균 집중 시간 (초)
    }

    @Getter
    @Builder
    public static class DailyCalendar {
        private String date;               // 날짜 (YYYY-MM-DD)
        private Integer durationInSeconds; // 해당 날짜의 총 집중 시간 (초)
        private Integer targetFocusTime;        // 목표 집중 시간 (초)
        private Double achievementRate;         // 해당 날짜 달성률 (%)
        private String obooniImageType;         // 오분이 이미지 타입 (달성률 기반)
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
    public static ConcentrationGoalAnalysisResponse empty(String goalName, String targetDate, Integer targetFocusTime) {
        return ConcentrationGoalAnalysisResponse.builder()
                .goalInfo(GoalInfo.builder()
                        .name(goalName)
                        .targetDate(targetDate)
                        .targetFocusTime(targetFocusTime)
                        .actualFocusTime(0)
                        .achievementRate(0.0)
                        .daysUntilTarget(0L)
                        .isCompleted(false)
                        .build())
                        .monthlyStats(MonthlyStats.builder()
                        .totalFocusDays(0)
                        .totalFocusTime(0)
                        .averageDailyFocusTime(0.0)
                        .build())
                .dailyCalendar(List.of())
                .build();
    }

    /**
     * 오분이 이미지 타입 결정 (달성률 기준)
     *
     * @param achievementRate 목표 달성률 (0~100+)
     * @return 오분이 이미지 타입 (목표 없는 날은 null)
     */
    public static String determineObooniImageType(double achievementRate) {
        if (achievementRate == 0.0) {
            return null;                    // 목표 없는 날: 이미지 없음
        } else if (achievementRate < 30.0) {
            return "sad";                   // 0~30%: 슬픈 오분이
        } else if (achievementRate < 70.0) {
            return "default";               // 30~70%: 기본 오분이
        } else {
            return "happy";                 // 70%+: 행복한 오분이
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
     * D-Day 목표 목록 아이템 DTO (하루 단위)
     */
    @Getter
    @Builder
    public static class ConcentrationGoalItem {
        private Long id;                        // 목표 ID
        private String name;                    // 목표 이름
        private String targetDate;              // 목표 날짜 (YYYY-MM-DD)
        private Integer targetFocusTime;        // 목표 집중 시간 (초)
        private Long daysUntilTarget;           // 목표까지 남은 일수 (D-Day)
        private Boolean isActive;               // 활성 상태 (오늘이 목표 날짜인지)
        private Boolean isCompleted;            // 완료 상태 (목표 날짜가 지났는지)
    }
}
