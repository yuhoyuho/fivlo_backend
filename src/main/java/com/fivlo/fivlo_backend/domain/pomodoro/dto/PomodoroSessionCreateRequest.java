package com.fivlo.fivlo_backend.domain.pomodoro.dto;

/**
 * 포모도로 세션 생성 요청 DTO
 * D-Day 목표와 연결 가능
 */
public record PomodoroSessionCreateRequest(
        Long id,                        // 포모도로 목표 ID (필수)
        Long concentrationGoalId        // D-Day 목표 ID (선택, NULL이면 일반 포모도로)
) {
}
