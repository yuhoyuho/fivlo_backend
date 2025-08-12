package com.fivlo.fivlo_backend.common.ai.dto;

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
public class AITimeAttackResponse {

    /**
     * AI가 추천한 단계 목록
     */
    private List<RecommendedStep> recommendedSteps;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendedStep {
        /**
         * 단계 내용
         */
        private String content;
        
        /**
         * 할당된 시간 (초 단위)
         */
        private Integer durationInSeconds;
    }
}
