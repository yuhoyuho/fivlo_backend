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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    // ==================== 목적 관리 ====================

    public TimeAttackGoalDto.GoalListResponse getAllGoals(Long userId) {
        log.debug("Getting all time attack goals for user: {}", userId);
        validateUser(userId);

        List<TimeAttackGoal> goals = timeAttackGoalRepository.findByUser_IdOrderByCreatedAtDesc(userId);
        long totalCount = goals.size();

        List<TimeAttackGoalDto.GoalResponse> goalResponses = goals.stream()
                .map(this::convertToGoalResponse)
                .toList();

        return new TimeAttackGoalDto.GoalListResponse(goalResponses, totalCount);
    }

    @Transactional
    public TimeAttackGoalDto.GoalResponse createGoal(Long userId, TimeAttackGoalDto.GoalRequest request) {
        log.debug("Creating time attack goal for user: {}, name: {}", userId, request.getName());

        User user = validateUser(userId);

        // 정확 일치 중복 체크
        if (timeAttackGoalRepository.existsByUser_IdAndNameIgnoreCase(userId, request.getName())) {
            log.warn("Duplicate goal name attempted: {} for user: {}", request.getName(), userId);
            throw new IllegalArgumentException("이미 동일한 이름의 목적이 존재합니다: " + request.getName());
        }

        TimeAttackGoal goal = TimeAttackGoal.builder()
                .user(user)
                .name(request.getName())
                .isPredefined(false)
                .build();

        TimeAttackGoal savedGoal = timeAttackGoalRepository.save(goal);
        log.info("Created time attack goal: {} for user: {}", savedGoal.getId(), userId);

        return convertToGoalResponse(savedGoal);
    }

    @Transactional
    public TimeAttackGoalDto.GoalResponse updateGoal(Long userId, Long goalId, TimeAttackGoalDto.GoalRequest request) {
        log.debug("Updating time attack goal: {} for user: {}, new name: {}", goalId, userId, request.getName());

        validateUser(userId);
        TimeAttackGoal goal = findGoalByIdAndUserId(goalId, userId);

        if (goal.getIsPredefined()) {
            log.warn("Attempted to update predefined goal: {} by user: {}", goalId, userId);
            throw new IllegalArgumentException("미리 정의된 목적은 수정할 수 없습니다.");
        }

        // 정확 일치 중복 체크 (자기 자신 제외)
        if (timeAttackGoalRepository.existsByUser_IdAndNameIgnoreCaseAndIdNot(userId, request.getName(), goalId)) {
            log.warn("Duplicate goal name attempted: {} for user: {}", request.getName(), userId);
            throw new IllegalArgumentException("이미 동일한 이름의 목적이 존재합니다: " + request.getName());
        }

        goal.updateName(request.getName());
        TimeAttackGoal updatedGoal = timeAttackGoalRepository.save(goal);
        log.info("Updated time attack goal: {} for user: {}", goalId, userId);

        return convertToGoalResponse(updatedGoal);
    }

    @Transactional
    public void deleteGoal(Long userId, Long goalId) {
        log.debug("Deleting time attack goal: {} for user: {}", goalId, userId);

        validateUser(userId);
        TimeAttackGoal goal = findGoalByIdAndUserId(goalId, userId);

        if (goal.getIsPredefined()) {
            log.warn("Attempted to delete predefined goal: {} by user: {}", goalId, userId);
            throw new IllegalArgumentException("미리 정의된 목적은 삭제할 수 없습니다.");
        }

        // 세션 존재 여부 빠른 체크
        if (timeAttackSessionRepository.existsByTimeAttackGoal_Id(goalId)) {
            log.warn("Attempted to delete goal with existing sessions: {} by user: {}", goalId, userId);
            throw new IllegalArgumentException("해당 목적을 사용하는 세션이 존재하여 삭제할 수 없습니다.");
        }

        timeAttackGoalRepository.delete(goal);
        log.info("Deleted time attack goal: {} for user: {}", goalId, userId);
    }

    // ==================== AI 추천 ====================

    public TimeAttackAIDto.RecommendStepsResponse recommendSteps(TimeAttackAIDto.RecommendStepsRequest request) {
        log.debug("Requesting AI step recommendation for goal: {}, duration: {}s",
                request.getGoalName(), request.getTotalDurationInSeconds());

        try {
            String aiResponseJson = geminiService.recommendTimeAttackSteps(
                    request.getGoalName(),
                    request.getTotalDurationInSeconds()
            );

            log.debug("Received AI response: {}", aiResponseJson);

            AITimeAttackResponse aiResponse = objectMapper.readValue(aiResponseJson, AITimeAttackResponse.class);

            TimeAttackAIDto.RecommendStepsResponse response =
                    TimeAttackAIDto.AIResponseConverter.convertFromAIResponse(aiResponse, request.getGoalName());

            log.info("Successfully generated {} AI-recommended steps for goal: {}",
                    response.getTotalSteps(), request.getGoalName());

            return response;

        } catch (JsonProcessingException e) {
            log.error("Failed to parse AI response JSON for goal: {}", request.getGoalName(), e);
            throw new RuntimeException("AI 응답 처리 중 오류가 발생했습니다: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Failed to get AI recommendation for goal: {}", request.getGoalName(), e);
            throw new RuntimeException("AI 단계 추천 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    // ==================== 세션 관리 ====================

    @Transactional
    public TimeAttackSessionDto.SessionResponse startSession(Long userId, TimeAttackSessionDto.SessionStartRequest request) {
        log.debug("Starting time attack session for user: {}, goal: {}", userId, request.getGoalId());

        User user = validateUser(userId);
        TimeAttackGoal goal = findGoalByIdAndUserId(request.getGoalId(), userId);

        // 단계별 시간 합계 검증 (요청 기준)
        int totalStepDuration = request.getSteps().stream()
                .mapToInt(step -> step.getDurationInSeconds() != null ? step.getDurationInSeconds() : 0)
                .sum();

        if (Math.abs(totalStepDuration - request.getTotalDurationInSeconds()) > 60) { // 1분 오차 허용
            log.warn("Step duration mismatch: total={}, steps={} for user: {}",
                    request.getTotalDurationInSeconds(), totalStepDuration, userId);
            throw new IllegalArgumentException("단계별 시간의 합계가 총 목표 시간과 일치하지 않습니다.");
        }

        // 세션 생성
        TimeAttackSession session = TimeAttackSession.builder()
                .user(user)
                .timeAttackGoal(goal)
                .totalDurationInSeconds(request.getTotalDurationInSeconds())
                .isCompleted(false)
                .build();

        TimeAttackSession savedSession = timeAttackSessionRepository.save(session);
        log.debug("Created session: {} for user: {}", savedSession.getId(), userId);

        // 단계들 생성 (요청 순서대로 1..N)
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
        log.debug("Created {} steps for session: {}", savedSteps.size(), savedSession.getId());

        log.info("Successfully started time attack session: {} for user: {}", savedSession.getId(), userId);

        return convertToSessionResponse(savedSession, savedSteps);
    }

    public TimeAttackSessionDto.SessionListResponse getSessionHistory(Long userId, Pageable pageable) {
        log.debug("Getting session history for user: {}", userId);
        validateUser(userId);

        // ✅ 레포 시그니처 교체: 정렬/페이징은 Pageable로
        Page<TimeAttackSession> sessionPage =
                timeAttackSessionRepository.findByUser_Id(userId, pageable);

        long completedCount = timeAttackSessionRepository.countByUser_IdAndIsCompleted(userId, true);

        List<Long> sessionIds = sessionPage.getContent().stream()
                .map(TimeAttackSession::getId)
                .toList();

        // ✅ 레포 시그니처 교체 (N+1 완화용 일괄 로드)
        final Map<Long, List<TimeAttackStep>> stepsBySession =
                sessionIds.isEmpty()
                        ? Map.of()
                        : timeAttackStepRepository
                        .findByTimeAttackSession_IdInOrderByTimeAttackSession_IdAscStepOrderAsc(sessionIds)
                        .stream()
                        .collect(Collectors.groupingBy(s -> s.getTimeAttackSession().getId()));

        List<TimeAttackSessionDto.SessionResponse> sessionResponses = sessionPage.getContent().stream()
                .map(session -> convertToSessionResponse(session, stepsBySession.getOrDefault(session.getId(), List.of())))
                .toList();

        return new TimeAttackSessionDto.SessionListResponse(
                sessionResponses,
                sessionPage.getTotalElements(),
                completedCount
        );
    }

    @Transactional
    public TimeAttackSessionDto.SessionResponse completeSession(
            Long userId, Long sessionId, TimeAttackSessionDto.SessionCompleteRequest request) {

        log.debug("Completing session: {} for user: {}", sessionId, userId);
        validateUser(userId);

        TimeAttackSession session = findSessionByIdAndUserId(sessionId, userId);
        session.updateCompletionStatus(request.getIsCompleted());
        TimeAttackSession updatedSession = timeAttackSessionRepository.save(session);

        // ✅ 레포 시그니처 교체
        List<TimeAttackStep> steps =
                timeAttackStepRepository.findByTimeAttackSession_IdOrderByStepOrderAsc(sessionId);

        log.info("Updated session completion status: {} for session: {}", request.getIsCompleted(), sessionId);

        return convertToSessionResponse(updatedSession, steps);
    }

    // ==================== 단계 관리 ====================

    @Transactional
    public TimeAttackStepDto.StepResponse addStepToGoal(Long userId, TimeAttackStepDto.AddStepRequest request) {
        log.debug("Adding step to goal: {} for user: {}", request.getGoalId(), userId);
    
        validateUser(userId);
        TimeAttackGoal goal = findGoalByIdAndUserId(request.getGoalId(), userId);
    
        // 임시 세션을 생성하여 단계를 저장 (실제 세션 시작 전 준비 단계)
        TimeAttackSession tempSession = getOrCreateTempSession(goal);
    
        // ✅ MAX 대체: Top+OrderBy로 현재 최대 stepOrder를 가진 행을 가져와 +1
        int nextStepOrder = timeAttackStepRepository
                .findTopByTimeAttackSession_IdOrderByStepOrderDesc(tempSession.getId())
                .map(s -> s.getStepOrder() + 1)
                .orElse(1); // 스텝이 없으면 1부터 시작
    
        TimeAttackStep newStep = TimeAttackStep.builder()
                .timeAttackSession(tempSession)
                .stepOrder(nextStepOrder)
                .content(request.getContent())
                .durationInSeconds(request.getDurationInSeconds())
                .build();
    
        TimeAttackStep savedStep = timeAttackStepRepository.save(newStep);
        log.info("Added new step: {} to goal: {}", savedStep.getId(), request.getGoalId());
    
        return new TimeAttackStepDto.StepResponse(
                savedStep.getId(),
                savedStep.getContent(),
                savedStep.getDurationInSeconds(),
                savedStep.getStepOrder()
        );
    }
    
    private TimeAttackSession getOrCreateTempSession(TimeAttackGoal goal) {
        // 임시 세션이 이미 있는지 확인 (is_completed = false인 세션)
        return timeAttackSessionRepository
                .findByTimeAttackGoal_IdAndIsCompletedFalse(goal.getId())
                .orElseGet(() -> {
                    // 임시 세션 생성
                    TimeAttackSession tempSession = TimeAttackSession.builder()
                            .timeAttackGoal(goal)
                            .user(goal.getUser())  // ✅ user 설정 추가!
                            .totalDurationInSeconds(0) // 나중에 계산됨
                            .isCompleted(false)
                            .build();
                    return timeAttackSessionRepository.save(tempSession);
                });
    }
    
    @Transactional
    public void updateStep(Long userId, Long stepId, TimeAttackStepDto.UpdateStepRequest request) {
        log.debug("Updating step: {} for user: {}", stepId, userId);
        validateUser(userId);

        TimeAttackStep step = timeAttackStepRepository.findById(stepId)
                .orElseThrow(() -> {
                    log.warn("Step not found: {}", stepId);
                    return new IllegalArgumentException("존재하지 않는 단계입니다: " + stepId);
                });

        if (!step.getTimeAttackSession().getUser().getId().equals(userId)) {
            log.warn("Unauthorized step access: stepId={}, userId={}", stepId, userId);
            throw new IllegalArgumentException("접근 권한이 없는 단계입니다.");
        }

        if (step.getTimeAttackSession().getIsCompleted()) {
            log.warn("Attempted to update step in completed session: {} by user: {}",
                    step.getTimeAttackSession().getId(), userId);
            throw new IllegalArgumentException("완료된 세션의 단계는 수정할 수 없습니다.");
        }

        // 내용/시간 업데이트
        if (request.getContent() != null && !request.getContent().trim().isEmpty()) {
            step.updateContent(request.getContent());
        }
        if (request.getDurationInSeconds() != null) {
            step.updateDuration(request.getDurationInSeconds());
        }

        // 순서 변경이 있는 경우: 동일 세션 내 전체 재정렬
        if (request.getStepOrder() != null) {
            Long sessionId = step.getTimeAttackSession().getId();
            List<TimeAttackStep> steps =
                    new ArrayList<>(timeAttackStepRepository.findByTimeAttackSession_IdOrderByStepOrderAsc(sessionId));

            // 현재 스텝 제거
            steps.removeIf(s -> Objects.equals(s.getId(), step.getId()));

            // 삽입 위치 보정 (1..N+1)
            int newOrder = Math.max(1, Math.min(request.getStepOrder(), steps.size() + 1));
            steps.add(newOrder - 1, step);

            // 1..N 재부여
            for (int i = 0; i < steps.size(); i++) {
                steps.get(i).updateStepOrder(i + 1);
            }

            // 전체 저장
            timeAttackStepRepository.saveAll(steps);
            log.info("Reordered steps in session: {} (moved stepId={} to order={})",
                    sessionId, stepId, newOrder);
        } else {
            // 순서 변경 없으면 해당 스텝만 저장
            timeAttackStepRepository.save(step);
        }

        log.info("Updated step: {} for user: {}", stepId, userId);
    }

    @Transactional
    public void deleteStepById(Long userId, Long stepId) {
        log.debug("Deleting step: {} for user: {}", stepId, userId);
        validateUser(userId);

        TimeAttackStep step = timeAttackStepRepository.findById(stepId)
                .orElseThrow(() -> {
                    log.warn("Step not found: {}", stepId);
                    return new IllegalArgumentException("존재하지 않는 단계입니다: " + stepId);
                });

        if (!step.getTimeAttackSession().getUser().getId().equals(userId)) {
            log.warn("Unauthorized step access: stepId={}, userId={}", stepId, userId);
            throw new IllegalArgumentException("접근 권한이 없는 단계입니다.");
        }

        if (step.getTimeAttackSession().getIsCompleted()) {
            log.warn("Attempted to delete step from completed session: {} by user: {}",
                    step.getTimeAttackSession().getId(), userId);
            throw new IllegalArgumentException("완료된 세션의 단계는 삭제할 수 없습니다.");
        }

        Long sessionId = step.getTimeAttackSession().getId();

        // 삭제
        timeAttackStepRepository.delete(step);

        // 잔여 스텝 재정렬
        List<TimeAttackStep> remainingSteps =
                timeAttackStepRepository.findByTimeAttackSession_IdOrderByStepOrderAsc(sessionId);
        for (int i = 0; i < remainingSteps.size(); i++) {
            remainingSteps.get(i).updateStepOrder(i + 1);
        }
        timeAttackStepRepository.saveAll(remainingSteps);

        log.info("Deleted step: {} and reordered remaining steps in session: {}", stepId, sessionId);
    }

    // ==================== 유틸리티 ====================

    private User validateUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found: {}", userId);
                    return new IllegalArgumentException("존재하지 않는 사용자입니다: " + userId);
                });
    }

    private TimeAttackGoal findGoalByIdAndUserId(Long goalId, Long userId) {
        return timeAttackGoalRepository.findById(goalId)
                .filter(goal -> goal.getUser().getId().equals(userId))
                .orElseThrow(() -> {
                    log.warn("Goal not found or access denied: goalId={}, userId={}", goalId, userId);
                    return new IllegalArgumentException("존재하지 않거나 접근 권한이 없는 목적입니다: " + goalId);
                });
    }

    private TimeAttackSession findSessionByIdAndUserId(Long sessionId, Long userId) {
        return timeAttackSessionRepository.findById(sessionId)
                .filter(session -> session.getUser().getId().equals(userId))
                .orElseThrow(() -> {
                    log.warn("Session not found or access denied: sessionId={}, userId={}", sessionId, userId);
                    return new IllegalArgumentException("존재하지 않거나 접근 권한이 없는 세션입니다: " + sessionId);
                });
    }

    private TimeAttackGoalDto.GoalResponse convertToGoalResponse(TimeAttackGoal goal) {
        return new TimeAttackGoalDto.GoalResponse(
                goal.getId(),
                goal.getName(),
                goal.getIsPredefined(),
                goal.getCreatedAt(),
                goal.getUpdatedAt()
        );
    }

    private TimeAttackSessionDto.SessionResponse convertToSessionResponse(
            TimeAttackSession session, List<TimeAttackStep> steps) {

        List<TimeAttackStepDto.StepResponse> stepResponses = steps.stream()
                .map(step -> new TimeAttackStepDto.StepResponse(
                        step.getId(),
                        step.getContent(),
                        step.getDurationInSeconds(),
                        step.getStepOrder()
                ))
                .toList();

        return new TimeAttackSessionDto.SessionResponse(
                session.getId(),
                session.getTimeAttackGoal().getName(),
                session.getTotalDurationInSeconds(),
                session.getIsCompleted(),
                stepResponses,
                session.getCreatedAt()
        );
    }
}
