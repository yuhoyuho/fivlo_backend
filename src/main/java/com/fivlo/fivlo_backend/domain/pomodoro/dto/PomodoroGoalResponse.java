package com.fivlo.fivlo_backend.domain.pomodoro.dto;

import com.fivlo.fivlo_backend.domain.pomodoro.entity.PomodoroGoal;

import java.util.List;

public record PomodoroGoalResponse(
        List<PomodoroGoal> goals) {
}
