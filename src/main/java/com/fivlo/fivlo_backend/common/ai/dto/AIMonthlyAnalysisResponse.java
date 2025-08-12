package com.fivlo.fivlo_backend.common.ai.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * AI 월간 분석 제안 결과 DTO
 * Gemini AI가 생성한 집중도 분석 및 제안사항을 담는 클래스
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIMonthlyAnalysisResponse {

    /**
     * 최적의 집중 시작 시간 정보
     */
    private OptimalStartTimeInfo optimalStartTimeInfo;
    
    /**
     * 최적의 집중 요일 정보
     */
    private List<OptimalDayInfo> optimalDayInfo;
    
    /**
     * 집중도가 낮은 시간 정보
     */
    private LowConcentrationTimeInfo lowConcentrationTimeInfo;
    
    /**
     * 활동 시간 제안
     */
    private ActivitySuggestions activitySuggestions;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptimalStartTimeInfo {
        /**
         * 최적 시간 (예: "AM 09:00")
         */
        private String time;
        
        /**
         * AI 코멘트
         */
        private String aiComment;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OptimalDayInfo {
        /**
         * 요일 (예: "수요일")
         */
        private String day;
        
        /**
         * AI 코멘트
         */
        private String aiComment;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LowConcentrationTimeInfo {
        /**
         * 시간 범위 (예: "PM 13:00 ~ 14:00")
         */
        private String timeRange;
        
        /**
         * AI 코멘트
         */
        private String aiComment;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivitySuggestions {
        /**
         * 활동별 제안 목록
         */
        private List<ActivitySuggestion> suggestions;
        
        /**
         * AI 코멘트
         */
        private String aiComment;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivitySuggestion {
        /**
         * 활동명 (예: "국제무역사 공부")
         */
        private String activityName;
        
        /**
         * 추천 시간대 (예: "AM 9시 ~ 11시")
         */
        private String timeRange;
    }
}
