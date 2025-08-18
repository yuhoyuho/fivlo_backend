package com.fivlo.fivlo_backend.domain.pomodoro.dto;

public record PomodoroSessionEndRequest(
        Long pomodoroSessionId,
        Integer durationInSeconds,
        Boolean isCycleCompleted
) {
}
