package com.fivlo.fivlo_backend.common.ai.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * AI 목표 분석 결과 DTO
 * Gemini AI가 생성한 Task 추천 결과를 담는 클래스
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIGoalAnalysisResponse {

    /**
     * AI가 추천한 Task 목록
     */
    private List<RecommendedTask> recommendedTasks;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendedTask {
        /**
         * Task 내용
         */
        private String content;
        
        /**
         * Task 예정일
         */
        private LocalDate dueDate;
        
        /**
         * 반복 유형 (DAILY/NONE)
         */
        private String repeatType;
        
        /**
         * 반복 종료일
         */
        private LocalDate endDate;
        
        /**
         * 성장앨범 연동 여부 (기본값: false)
         */
        private Boolean isLinkedToGrowthAlbum = false;
    }
}
