package com.fivlo.fivlo_backend.domain.timeattack.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 타임어택 목적 관련 DTO
 * i18n 키 기반 다국어 지원과 사용자 커스텀 목적을 모두 지원
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
         * 목적 이름 또는 i18n 키
         * isPredefined가 true면 i18n 키 (예: "timeAttack.goal.outingPrep")
         * isPredefined가 false면 사용자 입력 텍스트
         */
        @NotBlank(message = "목적 이름은 필수입니다")
        @Size(min = 1, max = 255, message = "목적 이름은 1자 이상 255자 이하여야 합니다")
        private String name;

        /**
         * 언어 코드 (AI 추천 요청 시 사용)
         * ko: 한국어, en: 영어
         */
        @Pattern(regexp = "^(ko|en)$", message = "언어 코드는 'ko' 또는 'en'만 허용됩니다")
        private String languageCode = "ko";  // 기본값: 한국어

        /**
         * 미리 정의된 목적 여부
         * true: nameKey 사용 (i18n 처리)
         * false: customName 사용 (사용자 입력)
         */
        private Boolean isPredefined = false;
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
         * 표시할 이름
         * isPredefined가 true면 i18n 키 반환 (프론트에서 i18n 처리)
         * isPredefined가 false면 사용자 입력 텍스트 반환
         */
        private String name;
        
        /**
         * 미리 정의된 목적 여부
         * true: 프론트에서 name을 i18n 키로 처리
         * false: name을 텍스트 그대로 표시
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
