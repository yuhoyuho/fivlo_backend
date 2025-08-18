package com.fivlo.fivlo_backend.domain.timeattack.controller;

import com.fivlo.fivlo_backend.common.Routes;
import com.fivlo.fivlo_backend.domain.timeattack.dto.TimeAttackAIDto;
import com.fivlo.fivlo_backend.domain.timeattack.dto.TimeAttackGoalDto;
import com.fivlo.fivlo_backend.domain.timeattack.dto.TimeAttackSessionDto;
import com.fivlo.fivlo_backend.domain.timeattack.dto.TimeAttackStepDto;
import com.fivlo.fivlo_backend.domain.timeattack.service.TimeAttackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import com.fivlo.fivlo_backend.security.CustomUserDetails;
import jakarta.validation.Valid;
import java.util.Map;

/**
 * 타임어택 컨트롤러
 * 타임어택 기능의 REST API 엔드포인트를 제공
 */
@Slf4j
@RestController
@RequestMapping(Routes.TIME_ATTACK_BASE)
@RequiredArgsConstructor
public class TimeAttackController {

    private final TimeAttackService timeAttackService;

    // ==================== 목적 관리 API ====================

    /**
     * API : 타임어택 목적 목록 조회
     * GET /api/v1/time-attack/goals
     */
    @GetMapping("/goals")
    public ResponseEntity<TimeAttackGoalDto.GoalListResponse> getGoals(
            @AuthenticationPrincipal UserDetails userDetails) {

        // CustomUserDetails에서 직접 userId를 가져오도록 수정
        CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
        Long userId = customUserDetails.getUser().getId(); // ✅ 수정된 코드
        log.debug("Getting time attack goals for user: {}", userId);
        
        TimeAttackGoalDto.GoalListResponse response = timeAttackService.getAllGoals(userId);
        
        return ResponseEntity.ok(response);
    }

    /**
     * API : 타임어택 목적 생성
     * POST /api/v1/time-attack/goals
     */
    @PostMapping("/goals")
    public ResponseEntity<TimeAttackGoalDto.GoalResponse> createGoal(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody TimeAttackGoalDto.GoalRequest request) {

        // CustomUserDetails에서 직접 userId를 가져오도록 수정
        CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
        Long userId = customUserDetails.getUser().getId(); // ✅ 수정된 코드
        log.debug("Creating time attack goal for user: {}, name: {}", userId, request.getName());
        
        TimeAttackGoalDto.GoalResponse response = timeAttackService.createGoal(userId, request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * API : 타임어택 목적 수정
     * PATCH /api/v1/time-attack/goals/{goalId}
     */
    @PatchMapping("/goals/{goalId}")
    public ResponseEntity<TimeAttackGoalDto.GoalResponse> updateGoal(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long goalId,
            @Valid @RequestBody TimeAttackGoalDto.GoalRequest request) {

        // CustomUserDetails에서 직접 userId를 가져오도록 수정
        CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
        Long userId = customUserDetails.getUser().getId(); // ✅ 수정된 코드
        log.debug("Updating time attack goal: {} for user: {}", goalId, userId);
        
        TimeAttackGoalDto.GoalResponse response = timeAttackService.updateGoal(userId, goalId, request);
        
        return ResponseEntity.ok(response);
    }

    /**
     * API : 타임어택 목적 삭제
     * DELETE /api/v1/time-attack/goals/{goalId}
     */
    @DeleteMapping("/goals/{goalId}")
    public ResponseEntity<Map<String, String>> deleteGoal(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long goalId) {

        // CustomUserDetails에서 직접 userId를 가져오도록 수정
        CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
        Long userId = customUserDetails.getUser().getId(); // ✅ 수정된 코드
        log.debug("Deleting time attack goal: {} for user: {}", goalId, userId);
        
        timeAttackService.deleteGoal(userId, goalId);
        
        return ResponseEntity.ok(Map.of("message", "목적이 성공적으로 삭제되었습니다."));
    }

    // ==================== AI 추천 API ====================

    /**
     * API : AI 기반 단계 추천
     * POST /api/v1/time-attack/recommend-steps
     */
    @PostMapping("/recommend-steps")
    public ResponseEntity<TimeAttackAIDto.RecommendStepsResponse> recommendSteps(
            @Valid @RequestBody TimeAttackAIDto.RecommendStepsRequest request) {
        
        log.debug("Requesting AI step recommendation for goal: {}, duration: {}s", 
                 request.getGoalName(), request.getTotalDurationInSeconds());
        
        TimeAttackAIDto.RecommendStepsResponse response = timeAttackService.recommendSteps(request);
        
        return ResponseEntity.ok(response);
    }

    // ==================== 세션 관리 API ====================

    /**
     * API : 타임어택 세션 시작 (세션 및 단계 저장)
     * POST /api/v1/time-attack/sessions
     */
    @PostMapping("/sessions")
    public ResponseEntity<TimeAttackSessionDto.SessionResponse> startSession(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody TimeAttackSessionDto.SessionStartRequest request) {

        // CustomUserDetails에서 직접 userId를 가져오도록 수정
        CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
        Long userId = customUserDetails.getUser().getId(); // ✅ 수정된 코드
        log.debug("Starting time attack session for user: {}, goal: {}", userId, request.getGoalId());
        
        TimeAttackSessionDto.SessionResponse response = timeAttackService.startSession(userId, request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * API : 타임어택 세션 기록 조회
     * GET /api/v1/time-attack/sessions
     */
    @GetMapping("/sessions")
    public ResponseEntity<TimeAttackSessionDto.SessionListResponse> getSessionHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        // CustomUserDetails에서 직접 userId를 가져오도록 수정
        CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
        Long userId = customUserDetails.getUser().getId(); // ✅ 수정된 코드
        log.debug("Getting session history for user: {}", userId);
        
        TimeAttackSessionDto.SessionListResponse response = timeAttackService.getSessionHistory(userId, pageable);
        
        return ResponseEntity.ok(response);
    }

    // ==================== 단계 관리 API ====================

    /**
     * API : 새로운 타임어택 단계 추가
     * POST /api/v1/time-attack/steps
     */
    @PostMapping("/steps")
    public ResponseEntity<Map<String, Object>> addStep(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody TimeAttackStepDto.AddStepRequest request) {

        // CustomUserDetails에서 직접 userId를 가져오도록 수정
        CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
        Long userId = customUserDetails.getUser().getId(); // ✅ 수정된 코드
        log.debug("Adding step to goal: {} for user: {}", request.getGoalId(), userId);
        
        TimeAttackStepDto.StepResponse response = timeAttackService.addStepToGoal(userId, request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
            "message", "타임어택 단계가 성공적으로 추가되었습니다.",
            "step_id", response.getId()
        ));
    }

    /**
     * API : 타임어택 단계 수정
     * PATCH /api/v1/time-attack/steps/{stepId}
     */
    @PatchMapping("/steps/{stepId}")
    public ResponseEntity<Map<String, String>> updateStep(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long stepId,
            @Valid @RequestBody TimeAttackStepDto.UpdateStepRequest request) {

        // CustomUserDetails에서 직접 userId를 가져오도록 수정
        CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
        Long userId = customUserDetails.getUser().getId(); // ✅ 수정된 코드
        log.debug("Updating step: {} for user: {}", stepId, userId);
        
        timeAttackService.updateStep(userId, stepId, request);
        
        return ResponseEntity.ok(Map.of("message", "타임어택 단계가 성공적으로 수정되었습니다."));
    }

    /**
     * API : 타임어택 단계 삭제
     * DELETE /api/v1/time-attack/steps/{stepId}
     */
    @DeleteMapping("/steps/{stepId}")
    public ResponseEntity<Map<String, String>> deleteStep(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long stepId) {

        // CustomUserDetails에서 직접 userId를 가져오도록 수정
        CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
        Long userId = customUserDetails.getUser().getId(); // ✅ 수정된 코드
        log.debug("Deleting step: {} for user: {}", stepId, userId);
        
        timeAttackService.deleteStepById(userId, stepId);
        
        return ResponseEntity.ok(Map.of("message", "타임어택 단계가 성공적으로 삭제되었습니다."));
    }

    // ==================== 예외 처리 ====================

    /**
     * IllegalArgumentException 처리
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("Validation error in TimeAttackController: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
    }

    /**
     * RuntimeException 처리 (AI 관련 오류 등)
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException e) {
        log.error("Runtime error in TimeAttackController: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "서버 내부 오류가 발생했습니다: " + e.getMessage()));
    }

    /**
     * 일반적인 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception e) {
        log.error("Unexpected error in TimeAttackController: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "예상치 못한 오류가 발생했습니다."));
    }
}
