package com.fivlo.fivlo_backend.domain.timeattack.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 타임어택 세션 관련 DTO
 * 타임어택 세션의 요청/응답 데이터 전송을 위한 클래스들
 */
public class TimeAttackSessionDto {

    /**
     * 타임어택 세션 시작 요청 DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionStartRequest {
        
        /**
         * 타임어택 목적 ID
         */
        @NotNull(message = "목적 ID는 필수입니다")
        private Long goalId;
        
        /**
         * 총 목표 시간 (초 단위)
         */
        @NotNull(message = "총 목표 시간은 필수입니다")
        @Min(value = 1, message = "총 목표 시간은 최소 1초 이상이어야 합니다")
        private Integer totalDurationInSeconds;
        
        /**
         * 단계 목록
         */
        @NotEmpty(message = "최소 1개 이상의 단계가 필요합니다")
        @Valid
        private List<TimeAttackStepDto.StepRequest> steps;
    }

    /**
     * 타임어택 세션 응답 DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionResponse {
        
        /**
         * 세션 ID
         */
        private Long id;
        
        /**
         * 목적 이름
         */
        private String goalName;
        
        /**
         * 총 목표 시간 (초 단위)
         */
        private Integer totalDurationInSeconds;
        
        /**
         * 완료 여부
         */
        private Boolean isCompleted;
        
        /**
         * 단계 목록
         */
        private List<TimeAttackStepDto.StepResponse> steps;
        
        /**
         * 생성일시
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdAt;
        
        /**
         * 총 목표 시간 (분 단위) - 편의를 위한 계산 필드
         */
        public Double getTotalDurationInMinutes() {
            return totalDurationInSeconds != null ? totalDurationInSeconds / 60.0 : 0.0;
        }
        
        /**
         * 총 목표 시간 (시간 단위) - 편의를 위한 계산 필드
         */
        public Double getTotalDurationInHours() {
            return totalDurationInSeconds != null ? totalDurationInSeconds / 3600.0 : 0.0;
        }
        
        /**
         * 총 단계 개수 - 편의를 위한 계산 필드
         */
        public Integer getTotalSteps() {
            return steps != null ? steps.size() : 0;
        }
    }

    /**
     * 타임어택 세션 목록 응답 DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionListResponse {
        
        /**
         * 세션 목록
         */
        private List<SessionResponse> sessions;
        
        /**
         * 총 세션 개수
         */
        private Long totalCount;
        
        /**
         * 완료된 세션 개수
         */
        private Long completedCount;
        
        /**
         * 완료율 (%) - 편의를 위한 계산 필드
         */
        public Double getCompletionRate() {
            if (totalCount == null || totalCount == 0) {
                return 0.0;
            }
            return completedCount != null ? (completedCount.doubleValue() / totalCount.doubleValue()) * 100.0 : 0.0;
        }
    }

    /**
     * 타임어택 세션 완료 요청 DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionCompleteRequest {
        
        /**
         * 완료 여부
         */
        @NotNull(message = "완료 여부는 필수입니다")
        private Boolean isCompleted;
    }
}
