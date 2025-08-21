package com.fivlo.fivlo_backend.domain.pomodoro.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * API 30: 일간 집중도 분석 응답 DTO
 */
@Getter
@Builder
public class DailyAnalysisResponse {

    private DailySummary summary;
    private List<HourlyBreakdown> hourlyBreakdown;

    @Getter
    @Builder
    public static class DailySummary {
        private Integer totalFocusTime;    // 총 집중 시간 (초)
        private Double focusRatio;         // 집중 시간 비율 (%)
    }

    @Getter
    @Builder
    public static class HourlyBreakdown {
        private Integer hour;              // 시간대 (0~23)
        private Integer durationInSeconds; // 해당 시간대 집중 시간 (초)
        private Long pomodoroGoalId;       // 포모도로 목표 ID
        private String goalName;           // 활동명
        private String goalColor;          // 활동 색상
    }

    /**
     * 비어있는 일간 분석 결과 생성 (포모도로 데이터가 없을 때)
     */
    public static DailyAnalysisResponse empty() {
        return DailyAnalysisResponse.builder()
                .summary(DailySummary.builder()
                        .totalFocusTime(0)
                        .focusRatio(0.0)
                        .build())
                .hourlyBreakdown(List.of())
                .build();
    }
}
