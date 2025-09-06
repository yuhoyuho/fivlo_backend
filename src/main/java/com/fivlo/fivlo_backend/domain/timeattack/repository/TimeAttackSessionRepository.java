package com.fivlo.fivlo_backend.domain.timeattack.repository;

import com.fivlo.fivlo_backend.domain.timeattack.entity.TimeAttackSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TimeAttackSessionRepository extends JpaRepository<TimeAttackSession, Long> {

    // 페이지네이션/정렬은 Pageable로 받기 (컨트롤러 @PageableDefault 사용)
    @EntityGraph(attributePaths = {"steps", "timeAttackGoal"})
    Page<TimeAttackSession> findByUser_Id(Long userId, Pageable pageable);

    // 리스트 용 정렬 메서드가 따로 필요하면 유지
    List<TimeAttackSession> findByUser_IdOrderByCreatedAtDesc(Long userId);

    List<TimeAttackSession> findByUser_IdAndIsCompleted(Long userId, Boolean isCompleted);

    List<TimeAttackSession> findByTimeAttackGoal_IdOrderByCreatedAtDesc(Long timeAttackGoalId);

    List<TimeAttackSession> findByUser_IdAndCreatedAtBetween(Long userId, LocalDateTime startDate, LocalDateTime endDate);

    long countByUser_IdAndIsCompleted(Long userId, Boolean isCompleted);

    // 목표에 연결된 세션 존재 여부 (삭제 방지 체크)
    boolean existsByTimeAttackGoal_Id(Long timeAttackGoalId);
    
    // 특정 목표의 미완료 세션 찾기 (임시 세션 체크)
    java.util.Optional<TimeAttackSession> findByTimeAttackGoal_IdAndIsCompletedFalse(Long timeAttackGoalId);
}
