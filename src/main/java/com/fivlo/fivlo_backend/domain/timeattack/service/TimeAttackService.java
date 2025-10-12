package com.fivlo.fivlo_backend.domain.timeattack.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fivlo.fivlo_backend.common.ai.GeminiService;
import com.fivlo.fivlo_backend.common.ai.dto.AITimeAttackResponse;
import com.fivlo.fivlo_backend.domain.timeattack.dto.TimeAttackAIDto;
import com.fivlo.fivlo_backend.domain.timeattack.dto.TimeAttackGoalDto;
import com.fivlo.fivlo_backend.domain.timeattack.dto.TimeAttackSessionDto;
import com.fivlo.fivlo_backend.domain.timeattack.dto.TimeAttackStepDto;
import com.fivlo.fivlo_backend.domain.timeattack.entity.TimeAttackGoal;
import com.fivlo.fivlo_backend.domain.timeattack.entity.TimeAttackSession;
import com.fivlo.fivlo_backend.domain.timeattack.entity.TimeAttackStep;
import com.fivlo.fivlo_backend.domain.timeattack.constants.PredefinedTimeAttackGoals;
import com.fivlo.fivlo_backend.domain.timeattack.repository.TimeAttackGoalRepository;
import com.fivlo.fivlo_backend.domain.timeattack.repository.TimeAttackSessionRepository;
import com.fivlo.fivlo_backend.domain.timeattack.repository.TimeAttackStepRepository;
import com.fivlo.fivlo_backend.domain.user.entity.User;
import com.fivlo.fivlo_backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TimeAttackService {

    private final TimeAttackGoalRepository timeAttackGoalRepository;
    private final TimeAttackSessionRepository timeAttackSessionRepository;
    private final TimeAttackStepRepository timeAttackStepRepository;
    private final UserRepository userRepository;
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;
    private final TimeAttackGoalInitService goalInitService;

    // AI 응답 캐싱은 GeminiService의 Redis 캐시를 사용

    // ==================== 목적 관리 (i18n 지원) ====================

    /**
     * 사용자의 모든 타임어택 목적 조회
     */
    public TimeAttackGoalDto.GoalListResponse getAllGoals(Long userId) {
        log.debug("Getting all time attack goals for user: {}", userId);
        User user = validateUser(userId);

        // 미리 정의된 목적들이 있는지 확인하고 없으면 생성
        goalInitService.ensureUserHasPredefinedGoals(user);

        List<TimeAttackGoal> goals = timeAttackGoalRepository.findByUser_IdOrderByCreatedAtDesc(userId);
        long totalCount = goals.size();

        List<TimeAttackGoalDto.GoalResponse> goalResponses = goals.stream()
                .map(this::convertToGoalResponse)
                .toList();

        return new TimeAttackGoalDto.GoalListResponse(goalResponses, totalCount);
    }

    /**
     * 새로운 타임어택 목적 생성 (i18n 키 또는 사용자 텍스트)
     */
    @Transactional
    public TimeAttackGoalDto.GoalResponse createGoal(Long userId, TimeAttackGoalDto.GoalRequest request) {
        log.debug("Creating time attack goal for user: {}, name: {}, isPredefined: {}", 
                 userId, request.getName(), request.getIsPredefined());

        User user = validateUser(userId);

        // 미리 정의된 목적 검증
        if (request.getIsPredefined() && !goalInitService.isValidPredefinedGoal(request.getName())) {
            log.warn("Invalid predefined goal attempted: {} for user: {}", request.getName(), userId);
            throw new IllegalArgumentException("유효하지 않은 미리 정의된 목적입니다: " + request.getName());
        }

        // 타입별 중복 체크
        validateGoalDuplication(userId, request.getName(), request.getIsPredefined(), null);

        TimeAttackGoal goal;
        
        if (request.getIsPredefined()) {
            // 미리 정의된 목적 (i18n 키)
            goal = TimeAttackGoal.builder()
                    .user(user)
                    .nameKey(request.getName())
                    .customName(null)
                    .isPredefined(true)
                    .build();
        } else {
            // 사용자 추가 목적
            goal = TimeAttackGoal.builder()
                    .user(user)
                    .nameKey(request.getName())
                    .customName(request.getName())
                    .isPredefined(false)
                    .build();
        }

        TimeAttackGoal savedGoal = timeAttackGoalRepository.save(goal);
        log.info("Created time attack goal: {} for user: {} (isPredefined: {})", 
                savedGoal.getId(), userId, savedGoal.getIsPredefined());

        return convertToGoalResponse(savedGoal);
    }

    /**
     * 타임어택 목적 수정
     */
    @Transactional
    public TimeAttackGoalDto.GoalResponse updateGoal(Long userId, Long goalId, TimeAttackGoalDto.GoalRequest request) {
        log.debug("Updating time attack goal: {} for user: {}, new name: {}", goalId, userId, request.getName());

        validateUser(userId);
        TimeAttackGoal goal = findGoalByIdAndUserId(goalId, userId);

        if (goal.getIsPredefined()) {
            log.warn("Attempted to update predefined goal: {} by user: {}", goalId, userId);
            throw new IllegalArgumentException("미리 정의된 목적은 수정할 수 없습니다.");
        }

        // 사용자 추가 목적만 수정 가능 (customName 업데이트)
        validateGoalDuplication(userId, request.getName(), false, goalId);

        goal.updateCustomName(request.getName());
        
        log.info("Updated time attack goal: {} for user: {}", goalId, userId);
        return convertToGoalResponse(goal);
    }

    /**
     * 타임어택 목적 삭제
     */
    @Transactional
    public void deleteGoal(Long userId, Long goalId) {
        log.debug("Deleting time attack goal: {} for user: {}", goalId, userId);

        validateUser(userId);
        TimeAttackGoal goal = findGoalByIdAndUserId(goalId, userId);

        if (goal.getIsPredefined()) {
            log.warn("Attempted to delete predefined goal: {} by user: {}", goalId, userId);
            throw new IllegalArgumentException("미리 정의된 목적은 삭제할 수 없습니다.");
        }

        timeAttackGoalRepository.delete(goal);
        log.info("Deleted time attack goal: {} for user: {}", goalId, userId);
    }

    // ==================== AI 추천 (캐싱 지원) ====================

    /**
     * AI 기반 단계 추천 (GeminiService Redis 캐싱 활용)
     */
    public TimeAttackAIDto.RecommendStepsResponse recommendSteps(Long userId, TimeAttackAIDto.RecommendStepsRequest request) {
        long methodStartTime = System.currentTimeMillis();
        log.info("⏱️ 타임어택 단계 추천 시작 - goalId: {}, duration: {}s", 
                 request.getGoalId(), request.getTotalDurationInSeconds());
    
        try {
            // 1. goalId로 목적 조회 및 사용자 권한 확인
            validateUser(userId);
            TimeAttackGoal goal = findGoalByIdAndUserId(request.getGoalId(), userId);
            String goalName = goal.getDisplayName();
            
            log.debug("Found goal: {} (ID: {}) for user: {}", goalName, request.getGoalId(), userId);
    
            // 2. AI 호출 (GeminiService에서 자동으로 캐싱 처리)
            long aiStartTime = System.currentTimeMillis();
            String jsonResponse = geminiService.recommendTimeAttackSteps(
                goalName,  // ← AI에게는 실제 활동 이름 전달
                request.getTotalDurationInSeconds(),
                request.getLanguageCode()
            );
            long aiCallTime = System.currentTimeMillis() - aiStartTime;
            log.info("🤖 AI 호출 완료 - 소요 시간: {}ms", aiCallTime);
    
            // 3. JSON 파싱
            AITimeAttackResponse aiResponse = objectMapper.readValue(jsonResponse, AITimeAttackResponse.class);
    
            List<TimeAttackAIDto.RecommendedStep> steps = aiResponse.getRecommendedSteps().stream()
                    .map(step -> new TimeAttackAIDto.RecommendedStep(
                            step.getContent(),
                            step.getDurationInSeconds(),
                            0  // recommendedOrder는 나중에 설정
                    ))
                    .toList();
    
            TimeAttackAIDto.RecommendStepsResponse response = new TimeAttackAIDto.RecommendStepsResponse(
                    steps, 
                    steps.size(), 
                    steps.stream().mapToInt(TimeAttackAIDto.RecommendedStep::getDurationInSeconds).sum(),
                    "AI 단계 추천이 완료되었습니다."
            );
    
            long totalTime = System.currentTimeMillis() - methodStartTime;
            log.info("✅ 타임어택 단계 추천 완료 - 총 소요 시간: {}ms (AI: {}ms, 파싱: {}ms)", 
                     totalTime, aiCallTime, totalTime - aiCallTime);
    
            return response;
    
        } catch (Exception e) {
            long totalTime = System.currentTimeMillis() - methodStartTime;
            log.error("❌ AI 추천 실패 - goalId: {}, 소요 시간: {}ms", request.getGoalId(), totalTime, e);
            throw new RuntimeException("AI 추천을 가져오는데 실패했습니다: " + e.getMessage());
        }
    }
        

    // ==================== 세션 관리 ====================

    // ==================== 유틸리티 메서드 ====================

    /**
     * 목적 중복 검증 (타입별 분리)
     */
    private void validateGoalDuplication(Long userId, String name, Boolean isPredefined, Long excludeId) {
        boolean isDuplicated;
        
        if (isPredefined) {
            // 미리 정의된 목적: nameKey 중복 체크
            if (excludeId != null) {
                isDuplicated = timeAttackGoalRepository.existsByUser_IdAndNameKeyAndIsPredefinedAndIdNot(
                    userId, name, true, excludeId);
            } else {
                isDuplicated = timeAttackGoalRepository.existsByUser_IdAndNameKeyAndIsPredefined(
                    userId, name, true);
            }
        } else {
            // 사용자 추가 목적: customName 중복 체크 (대소문자 무시)
            if (excludeId != null) {
                isDuplicated = timeAttackGoalRepository.existsByUser_IdAndCustomNameIgnoreCaseAndIsPredefinedAndIdNot(
                    userId, name, false, excludeId);
            } else {
                isDuplicated = timeAttackGoalRepository.existsByUser_IdAndCustomNameIgnoreCaseAndIsPredefined(
                    userId, name, false);
            }
        }

        if (isDuplicated) {
            String goalType = isPredefined ? "미리 정의된 목적" : "사용자 추가 목적";
            log.warn("Duplicate goal name attempted: {} for user: {} ({})", name, userId, goalType);
            throw new IllegalArgumentException("이미 동일한 이름의 목적이 존재합니다: " + name);
        }
    }

    /**
     * 엔티티를 응답 DTO로 변환 (i18n 지원)
     */
    private TimeAttackGoalDto.GoalResponse convertToGoalResponse(TimeAttackGoal goal) {
        return new TimeAttackGoalDto.GoalResponse(
                goal.getId(),
                goal.getDisplayName(),  // nameKey 또는 customName 반환
                goal.getIsPredefined(),
                goal.getCreatedAt(),
                goal.getUpdatedAt()
        );
    }

    /**
     * goalName과 displayName을 정규화하여 일관된 캐시 키 생성
     */
    private String generateCacheKeyForGoalName(String goalName, String languageCode) {
        // goalName을 displayName으로 변환 (미리 정의된 목적의 경우)
        String normalizedGoalName = normalizeGoalName(goalName);
        return TimeAttackAIDto.CacheEntry.generateCacheKey(
            normalizedGoalName.hashCode() + 0L, 
            languageCode
        );
    }

    /**
     * goalName을 displayName으로 정규화
     */
    private String normalizeGoalName(String goalName) {
        // 미리 정의된 목적인지 확인하고 nameKey로 변환
        for (PredefinedTimeAttackGoals.Goal predefinedGoal : PredefinedTimeAttackGoals.getAllPredefinedGoals()) {
            if (goalName.equals(predefinedGoal.getKoreanName()) || goalName.equals(predefinedGoal.getEnglishName())) {
                return predefinedGoal.getNameKey(); // displayName을 nameKey로 변환
            }
        }
        return goalName; // 사용자 커스텀 목적인 경우 그대로 반환
    }

    /**
     * 사용자 유효성 검증
     */
    private User validateUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found: {}", userId);
                    return new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId);
                });
    }

    /**
     * 목적 조회 및 권한 검증
     */
    private TimeAttackGoal findGoalByIdAndUserId(Long goalId, Long userId) {
        return timeAttackGoalRepository.findByIdAndUser_Id(goalId, userId)
                .orElseThrow(() -> {
                    log.warn("Goal not found or access denied: goalId={}, userId={}", goalId, userId);
                    return new IllegalArgumentException("목적을 찾을 수 없거나 접근 권한이 없습니다.");
                });
    }

    // ==================== 세션 관리 (기존 로직 유지) ====================

    /**
     * 타임어택 세션 시작
     */
    @Transactional
    public TimeAttackSessionDto.SessionResponse startSession(Long userId, TimeAttackSessionDto.SessionStartRequest request) {
        log.debug("Starting time attack session for user: {}, goal: {}", userId, request.getGoalId());

        User user = validateUser(userId);
        TimeAttackGoal goal = findGoalByIdAndUserId(request.getGoalId(), userId);

        // 단계별 시간 총합 검증
        int totalStepDuration = request.getSteps().stream()
                .mapToInt(TimeAttackStepDto.StepRequest::getDurationInSeconds)
                .sum();

        if (totalStepDuration != request.getTotalDurationInSeconds()) {
            log.warn("Time mismatch: expected={}, actual={} for user: {}", 
                    request.getTotalDurationInSeconds(), totalStepDuration, userId);
            throw new IllegalArgumentException("단계별 시간의 총합이 설정한 목표 시간과 일치하지 않습니다.");
        }

        // 세션 생성
        TimeAttackSession session = TimeAttackSession.builder()
                .user(user)
                .timeAttackGoal(goal)
                .totalDurationInSeconds(request.getTotalDurationInSeconds())
                .isCompleted(false)
                .build();

        TimeAttackSession savedSession = timeAttackSessionRepository.save(session);

        // 단계 생성
        List<TimeAttackStep> steps = IntStream.range(0, request.getSteps().size())
                .mapToObj(i -> {
                    TimeAttackStepDto.StepRequest stepRequest = request.getSteps().get(i);
                    return TimeAttackStep.builder()
                            .timeAttackSession(savedSession)
                            .stepOrder(i + 1)
                            .content(stepRequest.getContent())
                            .durationInSeconds(stepRequest.getDurationInSeconds())
                            .build();
                })
                .toList();

        List<TimeAttackStep> savedSteps = timeAttackStepRepository.saveAll(steps);

        log.info("Created time attack session: {} with {} steps for user: {}", 
                savedSession.getId(), steps.size(), userId);

        // SessionResponse 생성 - 실제 생성자에 맞게 수정
        return new TimeAttackSessionDto.SessionResponse(
                savedSession.getId(),
                goal.getDisplayName(),
                savedSession.getTotalDurationInSeconds(),
                savedSession.getIsCompleted(),
                convertToStepResponses(savedSteps),
                savedSession.getCreatedAt()
        );
    }

    /**
     * 타임어택 세션 기록 조회
     */
    public TimeAttackSessionDto.SessionListResponse getSessionHistory(Long userId, Pageable pageable) {
        log.debug("Getting session history for user: {}", userId);
        
        validateUser(userId);
        Page<TimeAttackSession> sessionPage = timeAttackSessionRepository.findByUser_Id(userId, pageable);

        List<TimeAttackSessionDto.SessionResponse> sessionResponses = sessionPage.getContent().stream()
                .map(session -> new TimeAttackSessionDto.SessionResponse(
                        session.getId(),
                        session.getTimeAttackGoal().getDisplayName(),  // i18n 키 또는 커스텀 이름
                        session.getTotalDurationInSeconds(),
                        session.getIsCompleted(),
                        convertToStepResponses(session.getSteps() != null ? session.getSteps() : List.of()), // 단계 포함 (UX 개선)
                        session.getCreatedAt()
                ))
                .toList();

        long completedCount = timeAttackSessionRepository.countByUser_IdAndIsCompleted(userId, true);

        return new TimeAttackSessionDto.SessionListResponse(
                sessionResponses,
                sessionPage.getTotalElements(),
                completedCount
        );
    }

    // ==================== 유틸리티 메서드 ====================

    /**
     * TimeAttackStep 리스트를 StepResponse 리스트로 변환
     */
    private List<TimeAttackStepDto.StepResponse> convertToStepResponses(List<TimeAttackStep> steps) {
        return steps.stream()
                .map(step -> new TimeAttackStepDto.StepResponse(
                        step.getId(),
                        step.getContent(),
                        step.getDurationInSeconds(),
                        step.getStepOrder()
                ))
                .toList();
    }
}
