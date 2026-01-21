package com.fivlo.fivlo_backend.domain.pomodoro.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.time.LocalDate;

/**
 * API 34: D-Day 목표 설정 요청 DTO (하루 단위 목표)
 */
@Builder
public record ConcentrationGoalRequest(
        @NotBlank(message = "목표 이름은 필수입니다.")
        String name,

        @NotNull(message = "목표 날짜는 필수입니다.")
        LocalDate targetDate,

        @NotNull(message = "목표 집중 시간은 필수입니다.")
        @Min(value = 60, message = "목표 집중 시간은 최소 60초(1분) 이상이어야 합니다.")
        Integer targetFocusTimeInSeconds
) {
    /**
     * 기본 목표 시간(2시간)으로 생성하는 헬퍼 메서드
     */
    public static ConcentrationGoalRequest withDefaultTime(String name, LocalDate targetDate) {
        return new ConcentrationGoalRequest(name, targetDate, 7200); // 2시간
    }

    /**
     * 목표 시간을 분 단위로 반환
     */
    public int getTargetFocusTimeInMinutes() {
        return targetFocusTimeInSeconds / 60;
    }

    /**
     * 목표 시간을 시간 단위로 반환
     */
    public double getTargetFocusTimeInHours() {
        return targetFocusTimeInSeconds / 3600.0;
    }

    /**
     * 목표 날짜가 과거인지 확인
     */
    public boolean isPastDate() {
        return targetDate != null && targetDate.isBefore(LocalDate.now());
    }

    /**
     * 목표 날짜가 오늘인지 확인
     */
    public boolean isToday() {
        return targetDate != null && targetDate.equals(LocalDate.now());
    }
}
