package com.fivlo.fivlo_backend.domain.pomodoro.repository;

import com.fivlo.fivlo_backend.domain.pomodoro.entity.ConcentrationGoal;
import com.fivlo.fivlo_backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ConcentrationGoalRepository extends JpaRepository<ConcentrationGoal, Long> {

    /**
     * 특정 사용자의 모든 집중 목표 조회 (최신순)
     */
    @Query("SELECT c FROM ConcentrationGoal c WHERE c.user = :user ORDER BY c.createdAt DESC")
    List<ConcentrationGoal> findByUserOrderByCreatedAtDesc(@Param("user") User user);

    /**
     * 특정 사용자의 활성 집중 목표 조회 (현재 진행 중인 목표)
     */
    @Query("SELECT c FROM ConcentrationGoal c WHERE c.user = :user " +
           "AND :currentDate >= c.startDate AND :currentDate <= c.endDate " +
           "ORDER BY c.createdAt DESC")
    List<ConcentrationGoal> findActiveGoalsByUser(@Param("user") User user, @Param("currentDate") LocalDate currentDate);

    /**
     * 특정 사용자의 특정 집중 목표 조회
     */
    @Query("SELECT c FROM ConcentrationGoal c WHERE c.user = :user AND c.id = :goalId")
    Optional<ConcentrationGoal> findByUserAndId(@Param("user") User user, @Param("goalId") Long goalId);

    /**
     * 특정 사용자의 완료된 집중 목표 조회
     */
    @Query("SELECT c FROM ConcentrationGoal c WHERE c.user = :user " +
           "AND c.endDate < :currentDate " +
           "ORDER BY c.endDate DESC")
    List<ConcentrationGoal> findCompletedGoalsByUser(@Param("user") User user, @Param("currentDate") LocalDate currentDate);
}
