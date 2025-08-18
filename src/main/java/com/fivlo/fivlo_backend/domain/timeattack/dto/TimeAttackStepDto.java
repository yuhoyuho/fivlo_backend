package com.fivlo.fivlo_backend.domain.timeattack.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 타임어택 단계 관련 DTO
 * 타임어택 단계의 요청/응답 데이터 전송을 위한 클래스들
 */
public class TimeAttackStepDto {

    /**
     * 타임어택 단계 생성/수정 요청 DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StepRequest {
        
        /**
         * 단계 내용
         */
        @NotBlank(message = "단계 내용은 필수입니다")
        @Size(min = 1, max = 255, message = "단계 내용은 1자 이상 255자 이하여야 합니다")
        private String content;
        
        /**
         * 할당된 시간 (초 단위)
         */
        @NotNull(message = "할당 시간은 필수입니다")
        @Min(value = 1, message = "할당 시간은 최소 1초 이상이어야 합니다")
        private Integer durationInSeconds;
        
        /**
         * 단계 순서 (선택사항 - 서버에서 자동 할당 가능)
         */
        private Integer stepOrder;
    }

    /**
     * API 46: 타임어택 단계 추가 요청 DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddStepRequest {
        
        /**
         * 단계가 속할 타임어택 목적의 ID
         */
        @NotNull(message = "타임어택 목적 ID는 필수입니다")
        private Long goalId;
        
        /**
         * 단계 내용
         */
        @NotBlank(message = "단계 내용은 필수입니다")
        @Size(min = 1, max = 255, message = "단계 내용은 1자 이상 255자 이하여야 합니다")
        private String content;
        
        /**
         * 할당된 시간 (초 단위)
         */
        @NotNull(message = "할당 시간은 필수입니다")
        @Min(value = 1, message = "할당 시간은 최소 1초 이상이어야 합니다")
        private Integer durationInSeconds;
    }

    /**
     * API 47: 타임어택 단계 수정 요청 DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateStepRequest {
        
        /**
         * 수정할 단계 내용 (선택사항)
         */
        @Size(min = 1, max = 255, message = "단계 내용은 1자 이상 255자 이하여야 합니다")
        private String content;
        
        /**
         * 수정할 할당 시간 (초 단위, 선택사항)
         */
        @Min(value = 1, message = "할당 시간은 최소 1초 이상이어야 합니다")
        private Integer durationInSeconds;
        
        /**
         * 수정할 단계의 순서 (선택사항)
         */
        @Min(value = 1, message = "단계 순서는 최소 1 이상이어야 합니다")
        private Integer stepOrder;
    }

    /**
     * 타임어택 단계 응답 DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StepResponse {
        
        /**
         * 단계 ID
         */
        private Long id;
        
        /**
         * 단계 내용
         */
        private String content;
        
        /**
         * 할당된 시간 (초 단위)
         */
        private Integer durationInSeconds;
        
        /**
         * 단계 순서
         */
        private Integer stepOrder;
        
        /**
         * 할당된 시간 (분 단위) - 편의를 위한 계산 필드
         */
        public Double getDurationInMinutes() {
            return durationInSeconds != null ? durationInSeconds / 60.0 : 0.0;
        }
        
        /**
         * 할당된 시간 (시간 단위) - 편의를 위한 계산 필드
         */
        public Double getDurationInHours() {
            return durationInSeconds != null ? durationInSeconds / 3600.0 : 0.0;
        }
    }

    /**
     * 타임어택 단계 목록 응답 DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StepListResponse {
        
        /**
         * 단계 목록
         */
        private java.util.List<StepResponse> steps;
        
        /**
         * 총 단계 개수
         */
        private Integer totalSteps;
        
        /**
         * 총 소요 시간 (초 단위)
         */
        private Integer totalDurationInSeconds;
        
        /**
         * 총 소요 시간 (분 단위) - 편의를 위한 계산 필드
         */
        public Double getTotalDurationInMinutes() {
            return totalDurationInSeconds != null ? totalDurationInSeconds / 60.0 : 0.0;
        }
    }
}
