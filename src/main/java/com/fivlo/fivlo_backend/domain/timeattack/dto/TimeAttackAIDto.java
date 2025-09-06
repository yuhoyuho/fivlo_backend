package com.fivlo.fivlo_backend.domain.timeattack.dto;

import com.fivlo.fivlo_backend.common.ai.dto.AITimeAttackResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.IntStream;

/**
 * 타임어택 AI 추천 관련 DTO
 * AI 단계 추천 요청/응답 데이터 전송을 위한 클래스들
 */
public class TimeAttackAIDto {

    /**
     * AI 단계 추천 요청 DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendStepsRequest {
        /** 목적 ID */
        @NotNull(message = "목적 ID는 필수입니다")
        @Min(value = 1, message = "목적 ID는 1 이상이어야 합니다")
        private Long goalId;

        /** 총 목표 시간 (초 단위) */
        @NotNull(message = "총 목표 시간은 필수입니다")
        @Min(value = 60, message = "총 목표 시간은 최소 1분(60초) 이상이어야 합니다")
        private Integer totalDurationInSeconds;

        /** 언어 코드 (AI 추천 언어) */
        @NotBlank(message = "언어 코드는 필수입니다")
        private String languageCode = "ko";

        /** 총 목표 시간 (분 단위) - 편의를 위한 계산 필드 */
        public Double getTotalDurationInMinutes() {
            return totalDurationInSeconds != null ? totalDurationInSeconds / 60.0 : 0.0;
        }
    }

    /**
     * AI 단계 추천 응답 DTO
     * 기존 AITimeAttackResponse를 확장하여 사용
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendStepsResponse {
        /** AI가 추천한 단계 목록 */
        private List<RecommendedStep> recommendedSteps;
        /** 총 추천 단계 개수 */
        private Integer totalSteps;
        /** 총 할당 시간 (초 단위) */
        private Integer totalAllocatedDuration;
        /** AI 추천 메시지 */
        private String aiMessage;

        /** 총 할당 시간 (분 단위) - 편의를 위한 계산 필드 */
        public Double getTotalAllocatedDurationInMinutes() {
            return totalAllocatedDuration != null ? totalAllocatedDuration / 60.0 : 0.0;
        }
    }

    /**
     * AI 추천 단계 DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendedStep {
        /** 단계 내용 */
        private String content;
        /** 할당된 시간 (초 단위) */
        private Integer durationInSeconds;
        /** 추천 순서 (AI가 제안한 순서) */
        private Integer recommendedOrder;

        /** 할당된 시간 (분 단위) - 편의를 위한 계산 필드 */
        public Double getDurationInMinutes() {
            return durationInSeconds != null ? durationInSeconds / 60.0 : 0.0;
        }

        /** AITimeAttackResponse.RecommendedStep에서 변환 */
        public static RecommendedStep fromAIResponse(AITimeAttackResponse.RecommendedStep aiStep, Integer order) {
            return new RecommendedStep(
                    aiStep.getContent(),
                    aiStep.getDurationInSeconds(),
                    order
            );
        }

        /** TimeAttackStepDto.StepRequest로 변환 */
        public TimeAttackStepDto.StepRequest toStepRequest() {
            return new TimeAttackStepDto.StepRequest(
                    this.content,
                    this.durationInSeconds,
                    this.recommendedOrder
            );
        }
    }

    /**
     * 캐시된 AI 추천 단계 응답 DTO (API 48)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CachedStepsResponse {
        /** 목적 ID */
        private Long goalId;
        /** 목적 이름 (표시용) */
        private String goalName;
        /** 캐시된 추천 단계 목록 */
        private List<RecommendedStep> recommendedSteps;
        /** 총 추천 단계 개수 */
        private Integer totalSteps;
        /** 총 할당 시간 (초 단위) */
        private Integer totalAllocatedDuration;
        /** 언어 코드 */
        private String languageCode;
        /** 캐시 생성 시간 */
        private java.time.LocalDateTime cachedAt;
        /** 캐시 만료 여부 */
        private Boolean isExpired = false;
    }

    /**
     * AI 추천 캐시 엔트리 (내부 사용)
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CacheEntry {
        /** 캐시 키 */
        private String cacheKey;
        /** 목적 이름 */
        private String goalName;
        /** 추천 단계 목록 */
        private List<RecommendedStep> recommendedSteps;
        /** 언어 코드 */
        private String languageCode;
        /** 총 시간 */
        private Integer totalDurationInSeconds;
        /** 생성 시간 */
        private java.time.LocalDateTime createdAt;
        /** 만료 시간 (TTL) */
        private java.time.LocalDateTime expiresAt;

        /** 만료 여부 확인 */
        public boolean isExpired() {
            return java.time.LocalDateTime.now().isAfter(expiresAt);
        }

        /** 캐시 키 생성 */
        public static String generateCacheKey(Long goalId, String languageCode) {
            return String.format("time_attack:goal:%d:lang:%s", goalId, languageCode);
        }
    }
    public static class AIResponseConverter {
        /** AITimeAttackResponse를 RecommendStepsResponse로 변환 */
        public static RecommendStepsResponse convertFromAIResponse(AITimeAttackResponse aiResponse, String goalName) {
            if (aiResponse == null || aiResponse.getRecommendedSteps() == null) {
                return new RecommendStepsResponse(List.of(), 0, 0, "AI 추천 결과가 없습니다.");
            }

            List<RecommendedStep> recommendedSteps =
                    IntStream.range(0, aiResponse.getRecommendedSteps().size())
                            .mapToObj(i -> RecommendedStep.fromAIResponse(aiResponse.getRecommendedSteps().get(i), i + 1))
                            .toList();

            int totalDuration = recommendedSteps.stream()
                    .mapToInt(step -> step.getDurationInSeconds() != null ? step.getDurationInSeconds() : 0)
                    .sum();

            String aiMessage = String.format("%s 활동을 위한 %d단계 일정을 추천했습니다.",
                    goalName, recommendedSteps.size());

            return new RecommendStepsResponse(
                    recommendedSteps,
                    recommendedSteps.size(),
                    totalDuration,
                    aiMessage
            );
        }
    }
}
