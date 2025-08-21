package com.fivlo.fivlo_backend.domain.pomodoro.controller;

import com.fivlo.fivlo_backend.common.Routes;
import com.fivlo.fivlo_backend.domain.pomodoro.dto.*;
import com.fivlo.fivlo_backend.domain.pomodoro.service.FocusAnalysisService;
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

/**
 * 집중도 분석 컨트롤러
 * 포모도로 기반 집중도 분석 API 제공 (API 30-35)
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class FocusAnalysisController {

    private final FocusAnalysisService focusAnalysisService;

    /**
     * API 30: 일간 집중도 분석 조회
     * GET /api/v1/analysis/daily?date=YYYY-MM-DD
     * 특정 날짜의 시간대별 집중 기록 및 통계 데이터를 조회합니다.
     */
    @GetMapping(Routes.ANALYSIS_DAILY)
    public ResponseEntity<DailyAnalysisResponse> getDailyAnalysis(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        log.info("일간 집중도 분석 요청 - userId: {}, date: {}", userDetails.getUser().getId(), date);

        User user = userDetails.getUser();
        DailyAnalysisResponse response = focusAnalysisService.getDailyAnalysis(user, date);

        log.info("일간 집중도 분석 응답 완료 - userId: {}, date: {}, 총 집중시간: {}초",
                userDetails.getUser().getId(), date, response.getSummary().getTotalFocusTime());

        return ResponseEntity.ok(response);
    }

    /**
     * API 31: 주간 집중도 분석 조회
     * GET /api/v1/analysis/weekly?start_date=YYYY-MM-DD
     * 특정 주의 요일별 집중 기록 및 통계 데이터를 조회합니다.
     */
    @GetMapping(Routes.ANALYSIS_WEEKLY)
    public ResponseEntity<WeeklyAnalysisResponse> getWeeklyAnalysis(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("start_date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate) {

        log.info("주간 집중도 분석 요청 - userId: {}, startDate: {}", userDetails.getUser().getId(), startDate);

        User user = userDetails.getUser();
        WeeklyAnalysisResponse response = focusAnalysisService.getWeeklyAnalysis(user, startDate);

        log.info("주간 집중도 분석 응답 완료 - userId: {}, startDate: {}, 총 집중시간: {}초",
                userDetails.getUser().getId(), startDate, response.getSummary().getTotalFocusTime());

        return ResponseEntity.ok(response);
    }

    /**
     * API 32: 월간 집중도 분석 조회
     * GET /api/v1/analysis/monthly?year=2025&month=7
     * 특정 월의 일별 집중 기록 및 통계 데이터를 조회합니다.
     */
    @GetMapping(Routes.ANALYSIS_MONTHLY)
    public ResponseEntity<MonthlyAnalysisResponse> getMonthlyAnalysis(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam int year,
            @RequestParam int month) {

        log.info("월간 집중도 분석 요청 - userId: {}, year: {}, month: {}", 
                userDetails.getUser().getId(), year, month);

        User user = userDetails.getUser();
        MonthlyAnalysisResponse response = focusAnalysisService.getMonthlyAnalysis(user, year, month);

        log.info("월간 집중도 분석 응답 완료 - userId: {}, year: {}, month: {}, 총 집중시간: {}초",
                userDetails.getUser().getId(), year, month, response.getSummary().getTotalFocusTime());

        return ResponseEntity.ok(response);
    }

    /**
     * API 33: 월간 AI 분석 제안서 조회
     * GET /api/v1/analysis/monthly/ai-suggestions?year=2025&month=7
     * 월간 집중 기록을 바탕으로 AI가 분석한 맞춤형 제안을 조회합니다.
     */
    @GetMapping(Routes.ANALYSIS_MONTHLY_AI_SUGGESTIONS)
    public ResponseEntity<AIAnalysisResponse> getMonthlyAIAnalysis(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam int year,
            @RequestParam int month) {

        log.info("월간 AI 분석 요청 - userId: {}, year: {}, month: {}", 
                userDetails.getUser().getId(), year, month);

        User user = userDetails.getUser();
        AIAnalysisResponse response = focusAnalysisService.getMonthlyAIAnalysis(user, year, month);

        log.info("월간 AI 분석 응답 완료 - userId: {}, year: {}, month: {}", 
                userDetails.getUser().getId(), year, month);

        return ResponseEntity.ok(response);
    }

    /**
     * API 34: D-Day 목표 설정 (프리미엄 전용)
     * POST /api/v1/analysis/goals
     * D-Day 분석을 위한 새로운 목표를 설정합니다.
     */
    @PostMapping(Routes.ANALYSIS_GOALS)
    public ResponseEntity<ConcentrationGoalAnalysisResponse.ConcentrationGoalCreateResponse> createConcentrationGoal(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ConcentrationGoalRequest request) {

        log.info("D-Day 목표 설정 요청 - userId: {}, goalName: {}", 
                userDetails.getUser().getId(), request.name());

        User user = userDetails.getUser();
        ConcentrationGoalAnalysisResponse.ConcentrationGoalCreateResponse response = 
                focusAnalysisService.createConcentrationGoal(user, request);

        log.info("D-Day 목표 설정 응답 완료 - userId: {}, goalId: {}", 
                userDetails.getUser().getId(), response.getId());

        return ResponseEntity.ok(response);
    }

    /**
     * API 35: D-Day 목표 분석 조회 (프리미엄 전용)
     * GET /api/v1/analysis/goals/{goalId}
     * D-Day 목표에 대한 기간 동안의 집중도 분석 데이터를 조회합니다.
     */
    @GetMapping(Routes.ANALYSIS_GOALS_BY_ID)
    public ResponseEntity<ConcentrationGoalAnalysisResponse> getConcentrationGoalAnalysis(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long goalId) {

        log.info("D-Day 목표 분석 요청 - userId: {}, goalId: {}", 
                userDetails.getUser().getId(), goalId);

        User user = userDetails.getUser();
        ConcentrationGoalAnalysisResponse response = 
                focusAnalysisService.getConcentrationGoalAnalysis(user, goalId);

        log.info("D-Day 목표 분석 응답 완료 - userId: {}, goalId: {}, 총 집중시간: {}초", 
                userDetails.getUser().getId(), goalId, response.getGoalInfo().getTotalFocusTime());

        return ResponseEntity.ok(response);
    }
}
