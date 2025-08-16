package com.fivlo.fivlo_backend.domain.timeattack.repository;

import com.fivlo.fivlo_backend.domain.timeattack.entity.TimeAttackGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 타임어택 목적 리포지토리
 */
@Repository
public interface TimeAttackGoalRepository extends JpaRepository<TimeAttackGoal, Long> {

    List<TimeAttackGoal> findByUser_IdOrderByCreatedAtDesc(Long userId);

    List<TimeAttackGoal> findByUser_IdAndIsPredefined(Long userId, Boolean isPredefined);

    long countByUser_IdAndIsPredefined(Long userId, Boolean isPredefined);

    // ▼ 정확 일치 중복 체크(대소문자 무시)
    boolean existsByUser_IdAndNameIgnoreCase(Long userId, String name);
    boolean existsByUser_IdAndNameIgnoreCaseAndIdNot(Long userId, String name, Long id);

    // 필요 시: 미리정의 목적 존재 여부
    boolean existsByUser_IdAndIsPredefined(Long userId, Boolean isPredefined);

    // (참고) 부분일치 검색이 꼭 필요하면 유지
    List<TimeAttackGoal> findByUser_IdAndNameContainingIgnoreCase(Long userId, String name);
}
