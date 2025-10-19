package com.fivlo.fivlo_backend.common.controller;

import com.fivlo.fivlo_backend.common.ai.GeminiService;
import com.fivlo.fivlo_backend.domain.timeattack.repository.TimeAttackSessionRepository;
import com.fivlo.fivlo_backend.domain.timeattack.service.TimeAttackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 성능 측정 통계 API
 * 자소서 검증용: AI 응답 시간, 캐시 히트율, 완료율 등 실제 수치 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/metrics")
@RequiredArgsConstructor
public class PerformanceMetricsController {

    private final GeminiService geminiService;
    private final TimeAttackSessionRepository sessionRepository;
    private final TimeAttackService timeAttackService;

    /**
     * 전체 성능 통계 조회
     * GET /api/v1/metrics/performance
     */
    @GetMapping("/performance")
    public ResponseEntity<Map<String, Object>> getPerformanceMetrics() {
        log.info("성능 통계 조회 요청");
        
        Map<String, Object> metrics = new HashMap<>();
        
        // 1. AI & 캐시 통계
        Map<String, Object> cacheStats = geminiService.getCacheStatistics();
        metrics.put("aiCache", cacheStats);
        
        // 2. DB 세션 재사용 통계
        Map<String, Object> dbSessionStats = timeAttackService.getDbSessionStatistics();
        metrics.put("dbSession", dbSessionStats);
        
        // 3. 타임어택 완료율 통계
        Map<String, Object> completionStats = sessionRepository.getCompletionStatistics();
        metrics.put("timeAttackCompletion", completionStats);
        
        // 4. 메타 정보
        metrics.put("timestamp", LocalDateTime.now());
        metrics.put("description", " 성능 측정 데이터");
        
        log.info(" 성능 통계 조회 완료 - AI 캐시 히트율: {}, DB 히트율: {}, 완료율: {}%",
                 cacheStats.get("hitRate"),
                 dbSessionStats.get("dbHitRate"),
                 completionStats.get("completionRate"));
        
        return ResponseEntity.ok(metrics);
    }

    /**
     *  AI 캐시 통계만 조회
     * GET /api/v1/metrics/ai-cache
     */
    @GetMapping("/ai-cache")
    public ResponseEntity<Map<String, Object>> getAICacheMetrics() {
        log.info(" AI 캐시 통계 조회");
        return ResponseEntity.ok(geminiService.getCacheStatistics());
    }
    
    /**
     *  DB 세션 재사용 통계만 조회
     * GET /api/v1/metrics/db-session
     */
    @GetMapping("/db-session")
    public ResponseEntity<Map<String, Object>> getDbSessionMetrics() {
        log.info(" DB 세션 통계 조회");
        return ResponseEntity.ok(timeAttackService.getDbSessionStatistics());
    }

    /**
     *  타임어택 완료율 통계만 조회
     * GET /api/v1/metrics/completion-rate
     */
    @GetMapping("/completion-rate")
    public ResponseEntity<Map<String, Object>> getCompletionRate(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        log.info("완료율 통계 조회 - userId: {}, 기간: {} ~ {}", userId, startDate, endDate);
        
        Map<String, Object> stats;
        
        if (userId != null) {
            // 사용자별 완료율
            stats = sessionRepository.getUserCompletionStatistics(userId);
        } else if (startDate != null && endDate != null) {
            // 기간별 완료율
            LocalDateTime start = LocalDateTime.parse(startDate + "T00:00:00");
            LocalDateTime end = LocalDateTime.parse(endDate + "T23:59:59");
            stats = sessionRepository.getCompletionStatisticsByDateRange(start, end);
        } else {
            // 전체 완료율
            stats = sessionRepository.getCompletionStatistics();
        }
        
        return ResponseEntity.ok(stats);
    }

    /**
     *  통계 초기화 (테스트용)
     * POST /api/v1/metrics/reset
     */
    @PostMapping("/reset")
    public ResponseEntity<Map<String, String>> resetMetrics() {
        log.warn(" 성능 통계 초기화 요청");
        
        geminiService.resetStatistics();
        timeAttackService.resetDbSessionStatistics();
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "AI 캐시 및 DB 세션 통계가 초기화되었습니다. (완료율 통계는 DB 데이터이므로 초기화되지 않습니다)");
        response.put("timestamp", LocalDateTime.now().toString());
        
        return ResponseEntity.ok(response);
    }

    /**
     *  측정 가이드 조회
     * GET /api/v1/metrics/guide
     */
    @GetMapping("/guide")
    public ResponseEntity<Map<String, Object>> getMeasurementGuide() {
        Map<String, Object> guide = new HashMap<>();
        
        guide.put("title", "성능 측정 가이드");
        
        Map<String, String> metrics = new HashMap<>();
        metrics.put("AI 응답 시간", "logs/fivlo-backend.log에서 ' AI 응답 완료' 로그의 시간 확인");
        metrics.put("캐시 히트율", "GET /api/v1/metrics/ai-cache → hitRate 필드");
        metrics.put("DB 조회 시간", "GET /api/v1/metrics/db-session → avgDbQueryTimeMs 필드");
        metrics.put("DB 히트율", "GET /api/v1/metrics/db-session → dbHitRate 필드");
        metrics.put("단계 전환 지연", "logs/fivlo-backend.log에서 ' 타임어택 단계 추천' 시작/완료 로그 시간 차이");
        metrics.put("완료율", "GET /api/v1/metrics/completion-rate → completionRate 필드");
        
        guide.put("측정지표", metrics);
        
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("전체 통계", "GET /api/v1/metrics/performance");
        endpoints.put("AI 캐시", "GET /api/v1/metrics/ai-cache");
        endpoints.put("DB 세션", "GET /api/v1/metrics/db-session");
        endpoints.put("완료율", "GET /api/v1/metrics/completion-rate");
        endpoints.put("사용자별 완료율", "GET /api/v1/metrics/completion-rate?userId={userId}");
        endpoints.put("기간별 완료율", "GET /api/v1/metrics/completion-rate?startDate=2025-01-01&endDate=2025-12-31");
        endpoints.put("통계 초기화", "POST /api/v1/metrics/reset");
        
        guide.put("API 엔드포인트", endpoints);
        
        Map<String, String> logFiles = new HashMap<>();
        logFiles.put("메인 로그", "logs/fivlo-backend.log");
        logFiles.put("로그 확인 명령", "tail -f logs/fivlo-backend.log");
        logFiles.put("AI 호출 로그 필터", "grep ' AI' logs/fivlo-backend.log");
        logFiles.put("완료 로그 필터", "grep ' 타임어택' logs/fivlo-backend.log");
        
        guide.put("로그 확인", logFiles);
        
        return ResponseEntity.ok(guide);
    }
}
