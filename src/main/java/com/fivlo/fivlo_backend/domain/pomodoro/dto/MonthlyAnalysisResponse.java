package com.fivlo.fivlo_backend.domain.pomodoro.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * API 32: 월간 집중도 분석 응답 DTO
 */
@Getter
@Builder
public class MonthlyAnalysisResponse {

    private MonthlySummary summary;
    private List<DailyBreakdown> dailyBreakdown;

    @Getter
    @Builder
    public static class MonthlySummary {
        private Integer totalFocusTime;        // 월간 총 집중 시간 (초)
        private Integer totalRestTime;         // 월간 총 휴식 시간 (초) 
        private Integer averageDailyFocusTime; // 월간 평균 집중 시간 (초)
    }

    @Getter
    @Builder
    public static class DailyBreakdown {
        private String date;               // 날짜 (YYYY-MM-DD)
        private Integer durationInSeconds; // 해당 날짜의 총 집중 시간 (초)
    }

    /**
     * 비어있는 월간 분석 결과 생성
     */
    public static MonthlyAnalysisResponse empty() {
        return MonthlyAnalysisResponse.builder()
                .summary(MonthlySummary.builder()
                        .totalFocusTime(0)
                        .totalRestTime(0)
                        .averageDailyFocusTime(0)
                        .build())
                .dailyBreakdown(List.of())
                .build();
    }
}
