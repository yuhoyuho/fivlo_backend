package com.fivlo.fivlo_backend.domain.pomodoro.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * API 33: 월간 AI 분석 제안서 응답 DTO
 */
@Getter
@Builder
public class AIAnalysisResponse {

    private OptimalStartTimeInfo optimalStartTimeInfo;
    private List<OptimalDayInfo> optimalDayInfo;
    private LowConcentrationTimeInfo lowConcentrationTimeInfo;
    private List<ActivitySuggestion> activitySuggestions;
    private String overallComment; // 전체 월간 분석 종합 코멘트

    @Getter
    @Builder
    public static class OptimalStartTimeInfo {
        private String time;                    // 최적 시작 시간 (예: 'AM 09:00')
        private Integer pomodoroSetCount;       // 포모도로 세트 수
        private Double interruptionRate;       // 중단율 (%)
        private Integer averageFocusTimeInMinutes; // 평균 집중시간 (분)
        private String aiComment;               // AI 조언
    }

    @Getter
    @Builder
    public static class OptimalDayInfo {
        private String day;                     // 요일 (예: '수요일')
        private Double averageSetCount;         // 평균 세트 수
        private Double successRate;             // 성공률 (%)
        private Integer averageFocusTimeInMinutes; // 평균 집중시간 (분)
        private String aiComment;               // AI 조언
    }

    @Getter
    @Builder
    public static class LowConcentrationTimeInfo {
        private String timeRange;               // 시간대 (예: 'PM 13:00 ~ 14:00')
        private Double interruptionRate;       // 중단율 (%)
        private Integer averageFocusTimeInMinutes; // 평균 집중시간 (분)
        private Double setSuccessRate;          // 세트 성공률 (%)
        private String aiComment;               // AI 조언
    }

    @Getter
    @Builder
    public static class ActivitySuggestion {
        private String activityName;           // 활동명 (예: '국제무역사 공부')
        private String timeRange;              // 권장 시간 (예: 'AM 9시 ~ 11시')
    }

    /**
     * 비어있는 AI 분석 결과 생성 (데이터 부족 시)
     */
    public static AIAnalysisResponse empty() {
        return AIAnalysisResponse.builder()
                .optimalStartTimeInfo(OptimalStartTimeInfo.builder()
                        .time("데이터 부족")
                        .pomodoroSetCount(0)
                        .interruptionRate(0.0)
                        .averageFocusTimeInMinutes(0)
                        .aiComment("충분한 포모도로 데이터를 수집한 후 분석이 가능합니다.")
                        .build())
                .optimalDayInfo(List.of())
                .lowConcentrationTimeInfo(LowConcentrationTimeInfo.builder()
                        .timeRange("데이터 부족")
                        .interruptionRate(0.0)
                        .averageFocusTimeInMinutes(0)
                        .setSuccessRate(0.0)
                        .aiComment("더 많은 포모도로 세션을 진행해주세요.")
                        .build())
                .activitySuggestions(List.of())
                .overallComment("충분한 포모도로 데이터를 수집한 후 종합 분석이 가능합니다.")
                .build();
    }
}
