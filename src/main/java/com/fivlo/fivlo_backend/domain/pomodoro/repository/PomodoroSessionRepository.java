package com.fivlo.fivlo_backend.domain.pomodoro.repository;

import com.fivlo.fivlo_backend.domain.pomodoro.entity.PomodoroSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PomodoroSessionRepository extends JpaRepository<PomodoroSession, Long> {
}
