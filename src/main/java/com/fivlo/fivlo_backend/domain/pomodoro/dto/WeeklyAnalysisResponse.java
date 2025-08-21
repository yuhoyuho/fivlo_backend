package com.fivlo.fivlo_backend.domain.pomodoro.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * API 31: 주간 집중도 분석 응답 DTO
 */
@Getter
@Builder
public class WeeklyAnalysisResponse {

    private WeeklySummary summary;
    private List<DailyBreakdown> dailyBreakdown;

    @Getter
    @Builder
    public static class WeeklySummary {
        private Integer totalFocusTime;        // 주간 총 집중 시간 (초)
        private Integer totalRestTime;         // 주간 총 휴식 시간 (초)
        private Integer averageDailyFocusTime; // 주간 평균 집중 시간 (초)
        private String mostFocusedDay;         // 가장 집중한 요일
    }

    @Getter
    @Builder
    public static class DailyBreakdown {
        private String dayOfWeek;          // 요일 (예: 'MON', 'TUE')
        private Integer durationInSeconds; // 해당 요일의 총 집중 시간 (초)
        private String date;               // 날짜 (YYYY-MM-DD)
    }

    /**
     * 비어있는 주간 분석 결과 생성
     */
    public static WeeklyAnalysisResponse empty() {
        return WeeklyAnalysisResponse.builder()
                .summary(WeeklySummary.builder()
                        .totalFocusTime(0)
                        .totalRestTime(0)
                        .averageDailyFocusTime(0)
                        .mostFocusedDay("")
                        .build())
                .dailyBreakdown(List.of())
                .build();
    }
}
