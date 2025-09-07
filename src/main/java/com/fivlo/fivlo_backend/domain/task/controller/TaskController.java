package com.fivlo.fivlo_backend.domain.task.controller;

import com.fivlo.fivlo_backend.common.Routes;
import com.fivlo.fivlo_backend.common.ai.dto.AddAITaskRequestDto;
import com.fivlo.fivlo_backend.common.ai.dto.GoalAnalysisRequestDto;
import com.fivlo.fivlo_backend.common.ai.dto.GoalAnalysisResponseDto;
import com.fivlo.fivlo_backend.domain.task.dto.*;
import com.fivlo.fivlo_backend.domain.task.service.TaskService;
import com.fivlo.fivlo_backend.domain.user.entity.User;
import com.fivlo.fivlo_backend.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

/**
 * Task 컨트롤러
 * Task 관리 관련 API 엔드포인트 처리
 * API 7-11: Task CRUD 기능 제공
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class TaskController {

    private final TaskService taskService;

    /**
     * API 7: 특정 날짜의 Task 목록 조회
     * GET /api/v1/tasks?date=YYYY-MM-DD
     * 특정 날짜에 해당하는 모든 Task 목록을 조회합니다.
     */
    @GetMapping(Routes.TASKS_BASE)
    public ResponseEntity<TaskListResponse> getTasksByDate(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        log.info("특정 날짜 Task 목록 조회 요청 - userId: {}, date: {}", userDetails.getUser().getId(), date);
        
        User user = userDetails.getUser();
        TaskListResponse response = taskService.getTasksByDate(user, date);
        
        log.info("특정 날짜 Task 목록 조회 응답 완료 - userId: {}, date: {}, Task 수: {}", 
                userDetails.getUser().getId(), date, response.getTasks().size());
        
        return ResponseEntity.ok(response);
    }

    /**
     * API 8: 새로운 Task 생성
     * POST /api/v1/tasks
     * 새로운 Task를 생성합니다.
     */
    @PostMapping(Routes.TASKS_BASE)
    public ResponseEntity<TaskCreateResponse> createTask(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody TaskCreateRequest request) {
        
        log.info("Task 생성 요청 - userId: {}, content: {}, dueDate: {}", 
                userDetails.getUser().getId(), request.getContent(), request.getDueDate());
        
        User user = userDetails.getUser();
        TaskCreateResponse response = taskService.createTask(user, request);
        
        log.info("Task 생성 응답 완료 - userId: {}, taskId: {}", 
                userDetails.getUser().getId(), response.getId());
        
        return ResponseEntity.ok(response);
    }

    /**
     * API 9: Task 수정
     * PATCH /api/v1/tasks/{taskId}
     * 기존 Task의 내용을 수정합니다.
     */
    @PatchMapping(Routes.TASKS_BY_ID)
    public ResponseEntity<TaskMessageResponse> updateTask(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long taskId,
            @Valid @RequestBody TaskUpdateRequest request) {
        
        log.info("Task 수정 요청 - userId: {}, taskId: {}", userDetails.getUser().getId(), taskId);
        
        User user = userDetails.getUser();
        TaskMessageResponse response = taskService.updateTask(user, taskId, request);
        
        log.info("Task 수정 응답 완료 - userId: {}, taskId: {}", userDetails.getUser().getId(), taskId);
        
        return ResponseEntity.ok(response);
    }

    /**
     * API 10: Task 완료 상태 변경
     * PATCH /api/v1/tasks/{taskId}/complete
     * Task의 완료 상태(isCompleted)를 토글합니다.
     */
    @PatchMapping(Routes.TASKS_COMPLETE_BY_ID)
    public ResponseEntity<TaskMessageResponse> updateTaskCompletion(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long taskId,
            @Valid @RequestBody TaskCompleteRequest request) {
        
        log.info("Task 완료 상태 변경 요청 - userId: {}, taskId: {}, isCompleted: {}", 
                userDetails.getUser().getId(), taskId, request.getIsCompleted());
        
        User user = userDetails.getUser();
        TaskMessageResponse response = taskService.updateTaskCompletion(user, taskId, request);
        
        log.info("Task 완료 상태 변경 응답 완료 - userId: {}, taskId: {}", 
                userDetails.getUser().getId(), taskId);
        
        return ResponseEntity.ok(response);
    }

    /**
     * API 11: Task 삭제
     * DELETE /api/v1/tasks/{taskId}
     * 특정 Task를 삭제합니다.
     */
    @DeleteMapping(Routes.TASKS_BY_ID)
    public ResponseEntity<TaskMessageResponse> deleteTask(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long taskId,
            @RequestBody(required = false) TaskDeleteRequest request) {
        
        log.info("Task 삭제 요청 - userId: {}, taskId: {}", userDetails.getUser().getId(), taskId);
        
        // request가 null인 경우 기본값으로 처리
        if (request == null) {
            request = TaskDeleteRequest.builder().deleteFutureTasks(false).build();
        }
        
        User user = userDetails.getUser();
        TaskMessageResponse response = taskService.deleteTask(user, taskId, request);
        
        log.info("Task 삭제 응답 완료 - userId: {}, taskId: {}", userDetails.getUser().getId(), taskId);
        
        return ResponseEntity.ok(response);
    }

    /** 목표 세분화
     * HTTP : POST
     * EndPoint : /api/v1/ai/goals
     * 언어별 지원: Accept-Language 헤더 (ko, en 지원, 기본값: ko)
     */
    @PostMapping(Routes.AI_GOALS)
    public ResponseEntity<GoalAnalysisResponseDto> analyzeGoalAndGetRecommendations(
            @RequestBody GoalAnalysisRequestDto request,
            @RequestHeader(value = "Accept-Language", required = false, defaultValue = "ko") String acceptLanguage) {
    
        log.info("AI 목표 분석 요청 - goalContent: {}, goalType: {}, language: {}", 
                request.getGoalContent(), request.getGoalType(), acceptLanguage);
    
        // Accept-Language 헤더에서 언어 코드 추출 (en, ko 지원)
        String languageCode = extractLanguageCode(acceptLanguage);
        
        GoalAnalysisResponseDto response = taskService.analyzeAndRecommendTasks(request, languageCode);
        
        log.info("AI 목표 분석 응답 완료 - language: {}, recommendedTasksCount: {}", 
                languageCode, response.getRecommendedTasks().size());
    
        return ResponseEntity.ok(response);
    }

    /** 루틴 설정
     * HTTP : POST
     * EndPoint : /api/v1/ai/goals/tasks
     */
    @PostMapping(Routes.AI_GOALS_TASKS)
    public ResponseEntity<String> addAiTasks(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody AddAITaskRequestDto requestDto) {

        return ResponseEntity.ok(taskService.addAiRecommendedTasks(userDetails.getUser().getId(), requestDto));
    }

    /**
     * Task 완료 코인 지급
     * HTTP 메서드: POST
     * 엔드포인트: /api/v1/tasks/coins
     */
    @PostMapping(Routes.TASKS_COINS)
    public ResponseEntity<TaskCoinResponse> earnTaskCoin(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody TaskCoinRequest dto) {

        TaskCoinResponse response = taskService.earnTaskCoin(userDetails.getUser().getId(), dto);
        return ResponseEntity.ok(response);
    }

    /**
     * Accept-Language 헤더에서 언어 코드 추출
     * 지원 언어: ko (한국어), en (영어)
     * 기본값: ko
     */
    private String extractLanguageCode(String acceptLanguage) {
        if (acceptLanguage == null || acceptLanguage.trim().isEmpty()) {
            return "ko"; // 기본값: 한국어
        }
        
        // Accept-Language 헤더 예시: "en-US,en;q=0.9,ko;q=0.8"
        String language = acceptLanguage.toLowerCase().trim();
        
        if (language.startsWith("en")) {
            return "en"; // 영어
        } else if (language.startsWith("ko")) {
            return "ko"; // 한국어
        } else {
            return "ko"; // 지원하지 않는 언어인 경우 기본값
        }
    }

}

