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
    
    // ==================== 성능 측정용 통계 쿼리 ====================
    
    /**
     * 전체 완료율 조회 (자소서 검증용)
     */
    @org.springframework.data.jpa.repository.Query(
        "SELECT " +
        "COUNT(s) as totalSessions, " +
        "SUM(CASE WHEN s.isCompleted = true THEN 1 ELSE 0 END) as completedSessions, " +
        "CAST(SUM(CASE WHEN s.isCompleted = true THEN 1.0 ELSE 0.0 END) * 100.0 / COUNT(s) AS double) as completionRate " +
        "FROM TimeAttackSession s"
    )
    java.util.Map<String, Object> getCompletionStatistics();
    
    /**
     * 사용자별 완료율 조회
     */
    @org.springframework.data.jpa.repository.Query(
        "SELECT " +
        "COUNT(s) as totalSessions, " +
        "SUM(CASE WHEN s.isCompleted = true THEN 1 ELSE 0 END) as completedSessions, " +
        "CAST(SUM(CASE WHEN s.isCompleted = true THEN 1.0 ELSE 0.0 END) * 100.0 / COUNT(s) AS double) as completionRate " +
        "FROM TimeAttackSession s " +
        "WHERE s.user.id = :userId"
    )
    java.util.Map<String, Object> getUserCompletionStatistics(@org.springframework.data.repository.query.Param("userId") Long userId);
    
    /**
     * 기간별 완료율 조회
     */
    @org.springframework.data.jpa.repository.Query(
        "SELECT " +
        "COUNT(s) as totalSessions, " +
        "SUM(CASE WHEN s.isCompleted = true THEN 1 ELSE 0 END) as completedSessions, " +
        "CAST(SUM(CASE WHEN s.isCompleted = true THEN 1.0 ELSE 0.0 END) * 100.0 / COUNT(s) AS double) as completionRate " +
        "FROM TimeAttackSession s " +
        "WHERE s.createdAt BETWEEN :startDate AND :endDate"
    )
    java.util.Map<String, Object> getCompletionStatisticsByDateRange(
        @org.springframework.data.repository.query.Param("startDate") LocalDateTime startDate,
        @org.springframework.data.repository.query.Param("endDate") LocalDateTime endDate
    );
}
