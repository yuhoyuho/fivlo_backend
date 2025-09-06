package com.fivlo.fivlo_backend.domain.timeattack.service;

import com.fivlo.fivlo_backend.domain.timeattack.constants.PredefinedTimeAttackGoals;
import com.fivlo.fivlo_backend.domain.timeattack.entity.TimeAttackGoal;
import com.fivlo.fivlo_backend.domain.timeattack.repository.TimeAttackGoalRepository;
import com.fivlo.fivlo_backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Set;
/**
 * 타임어택 목적 초기화 서비스
 * 사용자에게 미리 정의된 목적들을 자동으로 생성하는 역할
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TimeAttackGoalInitService {

    private final TimeAttackGoalRepository timeAttackGoalRepository;

    /**
     * 사용자에게 미리 정의된 목적들이 있는지 확인하고, 없으면 자동 생성
     * 
     * @param user 목적을 생성할 사용자
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void ensureUserHasPredefinedGoals(User user) {
        log.debug("Checking predefined goals for user: {}", user.getId());

        // 현재 사용자가 가진 미리 정의된 목적들의 nameKey 조회
        Set<String> existingNameKeys = timeAttackGoalRepository
                .findByUser_IdAndIsPredefined(user.getId(), true)
                .stream()
                .map(TimeAttackGoal::getNameKey)
                .collect(Collectors.toSet());

        // 없는 미리 정의된 목적들을 찾아서 생성
        List<TimeAttackGoal> missingGoals = PredefinedTimeAttackGoals.getAllPredefinedGoals()
                .stream()
                .filter(predefinedGoal -> !existingNameKeys.contains(predefinedGoal.getNameKey()))
                .map(predefinedGoal -> createPredefinedGoal(user, predefinedGoal))
                .toList();

        if (!missingGoals.isEmpty()) {
            timeAttackGoalRepository.saveAll(missingGoals);
            log.info("Created {} predefined goals for user: {}", missingGoals.size(), user.getId());
            
            // 생성된 목적들 로깅
            missingGoals.forEach(goal -> 
                log.debug("Created predefined goal: {} (nameKey: {}) for user: {}", 
                         goal.getId(), goal.getNameKey(), user.getId())
            );
        } else {
            log.debug("User {} already has all predefined goals", user.getId());
        }
    }

    /**
     * 특정 nameKey가 유효한 미리 정의된 목적인지 검증
     */
    public boolean isValidPredefinedGoal(String nameKey) {
        return PredefinedTimeAttackGoals.isPredefinedGoal(nameKey);
    }

    /**
     * 미리 정의된 목적 엔티티 생성
     */
    private TimeAttackGoal createPredefinedGoal(User user, PredefinedTimeAttackGoals.Goal predefinedGoal) {
        return TimeAttackGoal.builder()
                .user(user)
                .nameKey(predefinedGoal.getNameKey())
                .customName(null)
                .isPredefined(true)
                .build();
    }
}

