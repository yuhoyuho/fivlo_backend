package com.fivlo.fivlo_backend.domain.timeattack.controller;

import com.fivlo.fivlo_backend.common.Routes;
import com.fivlo.fivlo_backend.domain.timeattack.dto.TimeAttackAIDto;
import com.fivlo.fivlo_backend.domain.timeattack.dto.TimeAttackGoalDto;
import com.fivlo.fivlo_backend.domain.timeattack.dto.TimeAttackSessionDto;
import com.fivlo.fivlo_backend.domain.timeattack.service.TimeAttackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.fivlo.fivlo_backend.security.CustomUserDetails;
import jakarta.validation.Valid;
import java.util.Map;

/**
 * 타임어택 컨트롤러 (API 41-48)
 * i18n 키 기반 다국어 지원과 AI 추천 캐싱을 포함한 REST API 엔드포인트 제공
 */
@Slf4j
@RestController
@RequestMapping(Routes.TIME_ATTACK_BASE)
@RequiredArgsConstructor
public class TimeAttackController {

    private final TimeAttackService timeAttackService;

    // ==================== 목적 관리 API (41-44) ====================

    /**
     * API 41: 타임어택 목적 목록 조회
     * GET /api/v1/time-attack/goals
     */
    @GetMapping("/goals")
    public ResponseEntity<TimeAttackGoalDto.GoalListResponse> getGoals(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = userDetails.getUser().getId();
        log.debug("Getting time attack goals for user: {}", userId);
        
        TimeAttackGoalDto.GoalListResponse response = timeAttackService.getAllGoals(userId);
        
        return ResponseEntity.ok(response);
    }

    /**
     * API 42: 타임어택 목적 추가
     * POST /api/v1/time-attack/goals
     */
    @PostMapping("/goals")
    public ResponseEntity<Map<String, Object>> createGoal(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody TimeAttackGoalDto.GoalRequest request) {

        Long userId = userDetails.getUser().getId();
        log.debug("Creating time attack goal for user: {}, name: {}, isPredefined: {}", 
                 userId, request.getName(), request.getIsPredefined());
        
        TimeAttackGoalDto.GoalResponse response = timeAttackService.createGoal(userId, request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
            "message", "목적이 추가되었습니다.",
            "goal_id", response.getId()
        ));
    }

    /**
     * API 43: 타임어택 목적 수정
     * PATCH /api/v1/time-attack/goals/{goalId}
     */
    @PatchMapping("/goals/{goalId}")
    public ResponseEntity<Map<String, String>> updateGoal(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long goalId,
            @Valid @RequestBody TimeAttackGoalDto.GoalRequest request) {

        Long userId = userDetails.getUser().getId();
        log.debug("Updating time attack goal: {} for user: {}", goalId, userId);
        
        timeAttackService.updateGoal(userId, goalId, request);
        
        return ResponseEntity.ok(Map.of("message", "목적이 수정되었습니다."));
    }

    /**
     * API 44: 타임어택 목적 삭제
     * DELETE /api/v1/time-attack/goals/{goalId}
     */
    @DeleteMapping("/goals/{goalId}")
    public ResponseEntity<Map<String, String>> deleteGoal(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long goalId) {

        Long userId = userDetails.getUser().getId();
        log.debug("Deleting time attack goal: {} for user: {}", goalId, userId);
        
        timeAttackService.deleteGoal(userId, goalId);
        
        return ResponseEntity.ok(Map.of("message", "목적이 성공적으로 삭제되었습니다."));
    }

    // ==================== AI 추천 API (45, 48) ====================

    /**
     * API 45: AI 기반 단계 추천
     * POST /api/v1/time-attack/recommend-steps
     */
    @PostMapping("/recommend-steps")
    public ResponseEntity<TimeAttackAIDto.RecommendStepsResponse> recommendSteps(
            @Valid @RequestBody TimeAttackAIDto.RecommendStepsRequest request,
            @RequestHeader(value = "Accept-Language", defaultValue = "ko") String acceptLanguage,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        Long userId = userDetails.getUser().getId();
        
        // Accept-Language 헤더에서 언어 코드 추출 (ko, en만 지원)
        String languageCode = extractLanguageCode(acceptLanguage);
        request.setLanguageCode(languageCode);
        
        log.debug("Requesting AI step recommendation for goalId: {}, duration: {}s, language: {}, user: {}", 
                 request.getGoalId(), request.getTotalDurationInSeconds(), languageCode, userId);
        
        TimeAttackAIDto.RecommendStepsResponse response = timeAttackService.recommendSteps(userId, request);
        
        return ResponseEntity.ok(response);
    }

    /**
     * API 48: 타임어택 목표의 마지막 추천 단계 조회 (캐싱)
     * GET /api/v1/time-attack/goals/{goalId}/last-recommended-steps
     */
    @GetMapping("/goals/{goalId}/last-recommended-steps")
    public ResponseEntity<TimeAttackAIDto.CachedStepsResponse> getLastRecommendedSteps(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long goalId,
            @RequestHeader(value = "Accept-Language", defaultValue = "ko") String acceptLanguage) {

        Long userId = userDetails.getUser().getId();
        String languageCode = extractLanguageCode(acceptLanguage);
        
        log.debug("Getting last recommended steps for goal: {}, user: {}, language: {}", 
                 goalId, userId, languageCode);
        
        TimeAttackAIDto.CachedStepsResponse response = timeAttackService.getLastRecommendedSteps(
                userId, goalId, languageCode);
        
        return ResponseEntity.ok(response);
    }

    // ==================== 세션 관리 API (46-47) ====================

    /**
     * API 46: 타임어택 세션 시작 (세션 및 단계 저장)
     * POST /api/v1/time-attack/sessions
     */
    @PostMapping("/sessions")
    public ResponseEntity<Map<String, Object>> startSession(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody TimeAttackSessionDto.SessionStartRequest request) {

        Long userId = userDetails.getUser().getId();
        log.debug("Starting time attack session for user: {}, goal: {}", userId, request.getGoalId());
        
        TimeAttackSessionDto.SessionResponse response = timeAttackService.startSession(userId, request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
            "message", "타임어택 세션이 시작되었습니다.",
            "session_id", response.getId()
        ));
    }

    /**
     * API 47: 타임어택 세션 기록 조회
     * GET /api/v1/time-attack/sessions
     */
    @GetMapping("/sessions")
    public ResponseEntity<TimeAttackSessionDto.SessionListResponse> getSessionHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Long userId = userDetails.getUser().getId();
        log.debug("Getting session history for user: {}", userId);
        
        TimeAttackSessionDto.SessionListResponse response = timeAttackService.getSessionHistory(userId, pageable);
        
        return ResponseEntity.ok(response);
    }

    // ==================== 유틸리티 메서드 ====================
    
    /**
     * Accept-Language 헤더에서 지원하는 언어 코드 추출
     * 
     * @param acceptLanguage Accept-Language 헤더 값
     * @return 지원하는 언어 코드 (ko 또는 en, 기본값: ko)
     */
    private String extractLanguageCode(String acceptLanguage) {
        if (acceptLanguage == null || acceptLanguage.trim().isEmpty()) {
            return "ko";
        }

        // Accept-Language 헤더 파싱 (예: "ko-KR,ko;q=0.9,en;q=0.8")
        String lowerCaseHeader = acceptLanguage.toLowerCase();
        
        if (lowerCaseHeader.contains("en")) {
            return "en";
        } else if (lowerCaseHeader.contains("ko")) {
            return "ko";
        }
        
        // 기본값: 한국어
        return "ko";
    }
}

// ==================== 제거된 API (구 스펙) ====================
// 
// 개별 Step 관리 API들이 제거되었습니다:
// - POST /api/v1/time-attack/steps (새로운 타임어택 단계 추가)
// - PATCH /api/v1/time-attack/steps/{stepId} (타임어택 단계 수정)  
// - DELETE /api/v1/time-attack/steps/{stepId} (타임어택 단계 삭제)
//
// 이유: 단계 관리가 세션 단위로 통합되어 개별 단계 관리가 불필요해짐
// 대체: API 46에서 세션 시작 시 모든 단계를 한번에 저장하는 방식으로 변경
