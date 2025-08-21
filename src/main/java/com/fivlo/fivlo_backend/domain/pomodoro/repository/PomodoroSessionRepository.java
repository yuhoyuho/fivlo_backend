package com.fivlo.fivlo_backend.domain.pomodoro.repository;

import com.fivlo.fivlo_backend.domain.pomodoro.entity.PomodoroSession;
import com.fivlo.fivlo_backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface PomodoroSessionRepository extends JpaRepository<PomodoroSession, Long> {

    /**
     * 특정 사용자의 일간 포모도로 세션 조회 (포모도로 목표 포함)
     */
    @Query("SELECT s FROM PomodoroSession s JOIN FETCH s.pomodoroGoal " +
           "WHERE s.user = :user AND s.createdAt >= :startOfDay AND s.createdAt < :nextDay " +
           "ORDER BY s.createdAt")
    List<PomodoroSession> findByUserAndDateWithGoal(@Param("user") User user, 
                                                    @Param("startOfDay") LocalDateTime startOfDay,
                                                    @Param("nextDay") LocalDateTime nextDay);

    /**
     * 특정 사용자의 주간 포모도로 세션 조회 (포모도로 목표 포함)
     */
    @Query("SELECT s FROM PomodoroSession s JOIN FETCH s.pomodoroGoal " +
           "WHERE s.user = :user AND s.createdAt >= :startDateTime AND s.createdAt < :endDateTime " +
           "ORDER BY s.createdAt")
    List<PomodoroSession> findByUserAndWeekWithGoal(@Param("user") User user, 
                                                    @Param("startDateTime") LocalDateTime startDateTime, 
                                                    @Param("endDateTime") LocalDateTime endDateTime);

    /**
     * 특정 사용자의 월간 포모도로 세션 조회 (포모도로 목표 포함)
     */
    @Query("SELECT s FROM PomodoroSession s JOIN FETCH s.pomodoroGoal " +
           "WHERE s.user = :user AND s.createdAt >= :startDateTime AND s.createdAt < :endDateTime " +
           "ORDER BY s.createdAt")
    List<PomodoroSession> findByUserAndMonthWithGoal(@Param("user") User user, 
                                                     @Param("startDateTime") LocalDateTime startDateTime, 
                                                     @Param("endDateTime") LocalDateTime endDateTime);

    /**
     * 특정 사용자의 기간별 포모도로 세션 조회 (D-Day 분석용)
     */
    @Query("SELECT s FROM PomodoroSession s JOIN FETCH s.pomodoroGoal " +
           "WHERE s.user = :user AND s.createdAt >= :startDateTime AND s.createdAt < :endDateTime " +
           "ORDER BY s.createdAt")
    List<PomodoroSession> findByUserAndDateRangeWithGoal(@Param("user") User user, 
                                                         @Param("startDateTime") LocalDateTime startDateTime, 
                                                         @Param("endDateTime") LocalDateTime endDateTime);

    /**
     * 특정 사용자의 완료된 포모도로 세션 개수 조회 (코인 지급용)
     */
    @Query("SELECT COUNT(s) FROM PomodoroSession s " +
           "WHERE s.user = :user AND s.createdAt >= :startOfDay AND s.createdAt < :nextDay AND s.isCycleCompleted = true")
    long countCompletedSessionsByUserAndDate(@Param("user") User user, 
                                            @Param("startOfDay") LocalDateTime startOfDay,
                                            @Param("nextDay") LocalDateTime nextDay);

    /**
     * 특정 사용자의 월간 활동별 집중시간 통계 (활동명으로 그룹핑)
     */
    @Query("SELECT s.pomodoroGoal.name, SUM(s.durationInSeconds) FROM PomodoroSession s " +
           "WHERE s.user = :user AND s.createdAt >= :startDateTime AND s.createdAt < :endDateTime " +
           "GROUP BY s.pomodoroGoal.id, s.pomodoroGoal.name " +
           "ORDER BY SUM(s.durationInSeconds) DESC")
    List<Object[]> findMonthlyActivityStatsByUser(@Param("user") User user, 
                                                  @Param("startDateTime") LocalDateTime startDateTime, 
                                                  @Param("endDateTime") LocalDateTime endDateTime);
}
