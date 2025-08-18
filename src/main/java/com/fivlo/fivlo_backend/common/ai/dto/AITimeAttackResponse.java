package com.fivlo.fivlo_backend.common.ai.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * AI 타임어택 단계 추천 결과 DTO
 * Gemini AI가 생성한 타임어택 단계 추천 결과를 담는 클래스
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AITimeAttackResponse {

    /**
     * AI가 추천한 단계 목록
     */
    @JsonAlias({"recommended_steps", "steps", "items"})
    private List<RecommendedStep> recommendedSteps;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RecommendedStep {
        /**
         * 단계 내용
         */
        @JsonAlias({"content", "title", "name"})
        private String content;
        
        /**
         * 할당된 시간 (초 단위)
         */
        @JsonAlias({"duration_in_seconds", "seconds", "duration", "time_seconds"})
        private Integer durationInSeconds;
    }
}
