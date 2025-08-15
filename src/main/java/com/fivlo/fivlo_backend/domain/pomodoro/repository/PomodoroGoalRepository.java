package com.fivlo.fivlo_backend.domain.pomodoro.repository;

import com.fivlo.fivlo_backend.domain.pomodoro.entity.PomodoroGoal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PomodoroGoalRepository extends JpaRepository<PomodoroGoal, Long> {

    List<PomodoroGoal> findByUserId(Long userId);
}
