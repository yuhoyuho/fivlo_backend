package com.fivlo.fivlo_backend.domain.timeattack.repository;

import com.fivlo.fivlo_backend.domain.timeattack.entity.TimeAttackGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 타임어택 목적 리포지토리
 * i18n 키 기반 구조와 사용자 커스텀 이름을 모두 지원
 */
@Repository
public interface TimeAttackGoalRepository extends JpaRepository<TimeAttackGoal, Long> {

    /**
     * 사용자의 모든 목적 조회 (생성일 역순)
     */
    List<TimeAttackGoal> findByUser_IdOrderByCreatedAtDesc(Long userId);

    /**
     * 미리 정의된 목적 또는 사용자 추가 목적 조회
     */
    List<TimeAttackGoal> findByUser_IdAndIsPredefined(Long userId, Boolean isPredefined);

    /**
     * 미리 정의된 목적 또는 사용자 추가 목적 개수
     */
    long countByUser_IdAndIsPredefined(Long userId, Boolean isPredefined);

    // ==================== 미리 정의된 목적 (nameKey 기반) ====================
    
    /**
     * nameKey로 미리 정의된 목적 조회
     */
    Optional<TimeAttackGoal> findByUser_IdAndNameKeyAndIsPredefined(Long userId, String nameKey, Boolean isPredefined);

    /**
     * nameKey 중복 체크 (미리 정의된 목적)
     */
    boolean existsByUser_IdAndNameKeyAndIsPredefined(Long userId, String nameKey, Boolean isPredefined);

    /**
     * nameKey 중복 체크 (자기 자신 제외)
     */
    boolean existsByUser_IdAndNameKeyAndIsPredefinedAndIdNot(Long userId, String nameKey, Boolean isPredefined, Long id);

    // ==================== 사용자 추가 목적 (customName 기반) ====================
    
    /**
     * customName으로 사용자 추가 목적 조회
     */
    List<TimeAttackGoal> findByUser_IdAndCustomNameContainingIgnoreCaseAndIsPredefined(Long userId, String customName, Boolean isPredefined);

    /**
     * customName 중복 체크 (사용자 추가 목적, 대소문자 무시)
     */
    boolean existsByUser_IdAndCustomNameIgnoreCaseAndIsPredefined(Long userId, String customName, Boolean isPredefined);

    /**
     * customName 중복 체크 (자기 자신 제외, 대소문자 무시)
     */
    boolean existsByUser_IdAndCustomNameIgnoreCaseAndIsPredefinedAndIdNot(Long userId, String customName, Boolean isPredefined, Long id);

    // ==================== 통합 조회 메서드 ====================
    
    /**
     * 특정 사용자의 특정 목적 조회 (ID와 userId로)
     */
    Optional<TimeAttackGoal> findByIdAndUser_Id(Long id, Long userId);

    /**
     * 미리 정의된 목적 존재 여부
     */
    boolean existsByUser_IdAndIsPredefined(Long userId, Boolean isPredefined);
}
