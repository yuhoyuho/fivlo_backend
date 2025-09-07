package com.fivlo.fivlo_backend.domain.pomodoro.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Gemini AI 월간 분석 응답 DTO
 * GeminiService의 JSON 응답 구조와 정확히 일치
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIResponseDto {

    @JsonProperty("optimal_start_time_info")
    private OptimalStartTimeInfoDto optimalStartTimeInfo;

    @JsonProperty("optimal_day_info")
    private List<OptimalDayInfoDto> optimalDayInfo;

    @JsonProperty("low_concentration_time_info")
    private LowConcentrationTimeInfoDto lowConcentrationTimeInfo;

    @JsonProperty("activity_suggestions")
    private ActivitySuggestionsDto activitySuggestions;

    @JsonProperty("overall_comment")
    private String overallComment;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptimalStartTimeInfoDto {
        @JsonProperty("time")
        private String time;

        @JsonProperty("pomodoro_set_count")
        private Integer pomodoroSetCount;

        @JsonProperty("interruption_rate")
        private Double interruptionRate;

        @JsonProperty("average_focus_time_in_minutes")
        private Integer averageFocusTimeInMinutes;

        @JsonProperty("ai_comment")
        private String aiComment;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptimalDayInfoDto {
        @JsonProperty("day")
        private String day;

        @JsonProperty("average_set_count")
        private Double averageSetCount;

        @JsonProperty("success_rate")
        private Double successRate;

        @JsonProperty("average_focus_time_in_minutes")
        private Integer averageFocusTimeInMinutes;

        @JsonProperty("ai_comment")
        private String aiComment;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LowConcentrationTimeInfoDto {
        @JsonProperty("time_range")
        private String timeRange;

        @JsonProperty("interruption_rate")
        private Double interruptionRate;

        @JsonProperty("average_focus_time_in_minutes")
        private Integer averageFocusTimeInMinutes;

        @JsonProperty("set_success_rate")
        private Double setSuccessRate;

        @JsonProperty("ai_comment")
        private String aiComment;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivitySuggestionsDto {
        @JsonProperty("suggestions")
        private List<ActivitySuggestionDto> suggestions;

        @JsonProperty("ai_comment")
        private String aiComment;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivitySuggestionDto {
        @JsonProperty("activity_name")
        private String activityName;

        @JsonProperty("time_range")
        private String timeRange;
    }
}
