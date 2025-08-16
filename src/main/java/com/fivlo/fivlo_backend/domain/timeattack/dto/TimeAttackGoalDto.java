package com.fivlo.fivlo_backend.domain.timeattack.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 타임어택 목적 관련 DTO
 * 타임어택 목적의 요청/응답 데이터 전송을 위한 클래스들
 */
public class TimeAttackGoalDto {

    /**
     * 타임어택 목적 생성/수정 요청 DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GoalRequest {
        
        /**
         * 목적 이름
         */
        @NotBlank(message = "목적 이름은 필수입니다")
        @Size(min = 1, max = 255, message = "목적 이름은 1자 이상 255자 이하여야 합니다")
        private String name;
    }

    /**
     * 타임어택 목적 응답 DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GoalResponse {
        
        /**
         * 목적 ID
         */
        private Long id;
        
        /**
         * 목적 이름
         */
        private String name;
        
        /**
         * 미리 정의된 목적 여부
         */
        private Boolean isPredefined;
        
        /**
         * 생성일시
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdAt;
        
        /**
         * 수정일시
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime updatedAt;
    }

    /**
     * 타임어택 목적 목록 응답 DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GoalListResponse {
        
        /**
         * 목적 목록
         */
        private java.util.List<GoalResponse> goals;
        
        /**
         * 총 목적 개수
         */
        private Long totalCount;
    }
}
