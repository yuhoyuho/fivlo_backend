package com.fivlo.fivlo_backend.domain.pomodoro.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDate;

/**
 * API 34: D-Day 목표 설정 요청 DTO
 */
@Builder
public record ConcentrationGoalRequest(
        @NotBlank(message = "목표 이름은 필수입니다.")
        String name,

        @NotNull(message = "시작일은 필수입니다.")
        LocalDate startDate,

        @NotNull(message = "종료일은 필수입니다.")
        LocalDate endDate
) {
    /**
     * 목표 기간 유효성 검증
     */
    public boolean isValidDateRange() {
        return startDate != null && endDate != null && !endDate.isBefore(startDate);
    }

    /**
     * 목표 기간이 너무 짧은지 확인 (최소 1일, 하루 목표는 허용)
     */
    public boolean isTooShort() {
        // 하루 목표도 유효하므로 실제로는 사용하지 않음
        // 향후 필요시 최소 기간을 정의할 수 있음
        return false;
    }

    /**
     * 목표 기간이 너무 긴지 확인 (최대 1년)
     */
    public boolean isTooLong() {
        return startDate != null && endDate != null && 
               startDate.plusYears(1).isBefore(endDate);
    }
}
