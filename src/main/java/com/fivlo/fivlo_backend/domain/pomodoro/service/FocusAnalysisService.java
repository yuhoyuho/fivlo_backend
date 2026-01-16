package com.fivlo.fivlo_backend.domain.pomodoro.service;

import com.fivlo.fivlo_backend.common.ai.GeminiService;
import com.fivlo.fivlo_backend.domain.pomodoro.dto.*;
import com.fivlo.fivlo_backend.domain.pomodoro.entity.ConcentrationGoal;
import com.fivlo.fivlo_backend.domain.pomodoro.entity.PomodoroSession;
import com.fivlo.fivlo_backend.domain.pomodoro.repository.ConcentrationGoalRepository;
import com.fivlo.fivlo_backend.domain.pomodoro.repository.PomodoroSessionRepository;
import com.fivlo.fivlo_backend.domain.user.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 집중도 분석 서비스
 * 포모도로 세션 데이터를 기반으로 일간/주간/월간/D-Day/AI 분석 기능 제공
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FocusAnalysisService {

    private final PomodoroSessionRepository pomodoroSessionRepository;
    private final ConcentrationGoalRepository concentrationGoalRepository;
    private final GeminiService geminiService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final int MAX_CHART_MINUTES = 240; // 차트 최대값 (4시간)

    /**
     * API 30: 일간 집중도 분석 조회
     * 특정 날짜의 시간대별 집중 기록 및 통계 데이터를 조회합니다.
     */
    public DailyAnalysisResponse getDailyAnalysis(User user, LocalDate date) {
        log.info("일간 집중도 분석 조회 시작 - userId: {}, date: {}", user.getId(), date);

        // 날짜 범위를 LocalDateTime으로 변환
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime nextDay = date.plusDays(1).atStartOfDay();

        // 해당 날짜의 포모도로 세션 조회
        List<PomodoroSession> sessions = pomodoroSessionRepository.findByUserAndDateWithGoal(user, startOfDay, nextDay);

        if (sessions.isEmpty()) {
            log.info("일간 집중도 분석 - 데이터 없음 - userId: {}, date: {}", user.getId(), date);
            return DailyAnalysisResponse.empty();
        }

        // 시간대별 집중 기록 생성
        List<DailyAnalysisResponse.HourlyBreakdown> hourlyBreakdown = createHourlyBreakdown(sessions);

        // 일간 요약 통계 생성
        DailyAnalysisResponse.DailySummary summary = createDailySummary(sessions);

        log.info("일간 집중도 분석 조회 완료 - userId: {}, date: {}, 총 집중시간: {}초", 
                user.getId(), date, summary.getTotalFocusTime());

        return DailyAnalysisResponse.builder()
                .summary(summary)
                .hourlyBreakdown(hourlyBreakdown)
                .build();
    }

    /**
     * API 31: 주간 집중도 분석 조회
     * 특정 주의 요일별 집중 기록 및 통계 데이터를 조회합니다.
     */
    public WeeklyAnalysisResponse getWeeklyAnalysis(User user, LocalDate startDate) {
        log.info("주간 집중도 분석 조회 시작 - userId: {}, startDate: {}", user.getId(), startDate);

        // 주간 범위 계산 (월요일 시작)
        LocalDate weekStart = startDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = weekStart.plusDays(6);
        
        LocalDateTime startDateTime = weekStart.atStartOfDay();
        LocalDateTime endDateTime = weekEnd.plusDays(1).atStartOfDay();

        // 주간 포모도로 세션 조회
        List<PomodoroSession> sessions = pomodoroSessionRepository.findByUserAndWeekWithGoal(user, startDateTime, endDateTime);

        if (sessions.isEmpty()) {
            log.info("주간 집중도 분석 - 데이터 없음 - userId: {}, weekStart: {}", user.getId(), weekStart);
            return WeeklyAnalysisResponse.empty();
        }

        // 요일별 집중 기록 생성
        List<WeeklyAnalysisResponse.DailyBreakdown> dailyBreakdown = createWeeklyDailyBreakdown(sessions, weekStart);

        // 주간 요약 통계 생성
        WeeklyAnalysisResponse.WeeklySummary summary = createWeeklySummary(sessions, dailyBreakdown);

        log.info("주간 집중도 분석 조회 완료 - userId: {}, weekStart: {}, 총 집중시간: {}초", 
                user.getId(), weekStart, summary.getTotalFocusTime());

        return WeeklyAnalysisResponse.builder()
                .summary(summary)
                .dailyBreakdown(dailyBreakdown)
                .build();
    }

    /**
     * API 32: 월간 집중도 분석 조회
     * 특정 월의 일별 집중 기록 및 통계 데이터를 조회합니다.
     */
    public MonthlyAnalysisResponse getMonthlyAnalysis(User user, int year, int month) {
        log.info("월간 집중도 분석 조회 시작 - userId: {}, year: {}, month: {}", user.getId(), year, month);

        // 월간 범위 계산
        LocalDate monthStart = LocalDate.of(year, month, 1);
        LocalDate monthEnd = monthStart.with(TemporalAdjusters.lastDayOfMonth());
        
        LocalDateTime startDateTime = monthStart.atStartOfDay();
        LocalDateTime endDateTime = monthEnd.plusDays(1).atStartOfDay();

        // 월간 포모도로 세션 조회
        List<PomodoroSession> sessions = pomodoroSessionRepository.findByUserAndMonthWithGoal(user, startDateTime, endDateTime);

        if (sessions.isEmpty()) {
            log.info("월간 집중도 분석 - 데이터 없음 - userId: {}, year: {}, month: {}", user.getId(), year, month);
            return MonthlyAnalysisResponse.empty();
        }

        // 일별 집중 기록 생성
        List<MonthlyAnalysisResponse.DailyBreakdown> dailyBreakdown = createMonthlyDailyBreakdown(sessions, monthStart, monthEnd);

        // 월간 요약 통계 생성
        MonthlyAnalysisResponse.MonthlySummary summary = createMonthlySummary(sessions);

        log.info("월간 집중도 분석 조회 완료 - userId: {}, year: {}, month: {}, 총 집중시간: {}초", 
                user.getId(), year, month, summary.getTotalFocusTime());

        return MonthlyAnalysisResponse.builder()
                .summary(summary)
                .dailyBreakdown(dailyBreakdown)
                .build();
    }

    // ==================== 헬퍼 메서드들 ====================

    /**
     * 시간대별 집중 기록 생성 (일간 분석용)
     */
    private List<DailyAnalysisResponse.HourlyBreakdown> createHourlyBreakdown(List<PomodoroSession> sessions) {
        // 시간대별로 그룹핑
        Map<Integer, List<PomodoroSession>> sessionsByHour = sessions.stream()
                .filter(Objects::nonNull)
                .filter(s -> s.getCreatedAt() != null)
                .collect(Collectors.groupingBy(session -> session.getCreatedAt().getHour()));

        List<DailyAnalysisResponse.HourlyBreakdown> hourlyBreakdown = new ArrayList<>();

        // 0시부터 23시까지 처리
        for (int hour = 0; hour < 24; hour++) {
            List<PomodoroSession> hourSessions = sessionsByHour.getOrDefault(hour, List.of());
            
            if (!hourSessions.isEmpty()) {
                // 해당 시간대에 세션이 있는 경우
                for (PomodoroSession session : hourSessions) {
                    if (session != null && session.getPomodoroGoal() != null 
                        && session.getDurationInSeconds() != null) {
                        hourlyBreakdown.add(DailyAnalysisResponse.HourlyBreakdown.builder()
                                .hour(hour)
                                .durationInSeconds(session.getDurationInSeconds())
                                .pomodoroGoalId(session.getPomodoroGoal().getId())
                                .goalName(session.getPomodoroGoal().getName() != null ? 
                                         session.getPomodoroGoal().getName() : "")
                                .goalColor(session.getPomodoroGoal().getColor() != null ? 
                                          session.getPomodoroGoal().getColor() : "#000000")
                                .build());
                    }
                }
            }
        }

        return hourlyBreakdown;
    }

    /**
     * 일간 요약 통계 생성
     */
    private DailyAnalysisResponse.DailySummary createDailySummary(List<PomodoroSession> sessions) {
        int totalFocusTime = sessions.stream()
                .filter(Objects::nonNull)
                .filter(s -> s.getDurationInSeconds() != null)
                .mapToInt(PomodoroSession::getDurationInSeconds)
                .sum();

        // 집중 비율 계산 (임시로 집중시간 / (집중시간 + 휴식시간 추정치))
        // 휴식시간은 포모도로 원칙에 따라 집중시간의 20% 정도로 추정
        int estimatedRestTime = (int) (totalFocusTime * 0.2);
        double focusRatio = totalFocusTime > 0 ? 
                (double) totalFocusTime / (totalFocusTime + estimatedRestTime) * 100 : 0.0;

        return DailyAnalysisResponse.DailySummary.builder()
                .totalFocusTime(totalFocusTime)
                .focusRatio(Math.round(focusRatio * 10.0) / 10.0) // 소수점 첫째자리까지
                .build();
    }

    /**
     * 주간 요일별 집중 기록 생성
     */
    private List<WeeklyAnalysisResponse.DailyBreakdown> createWeeklyDailyBreakdown(List<PomodoroSession> sessions, LocalDate weekStart) {
        // 날짜별로 그룹핑
        Map<LocalDate, List<PomodoroSession>> sessionsByDate = sessions.stream()
                .filter(Objects::nonNull)
                .filter(s -> s.getCreatedAt() != null)
                .collect(Collectors.groupingBy(session -> session.getCreatedAt().toLocalDate()));

        List<WeeklyAnalysisResponse.DailyBreakdown> dailyBreakdown = new ArrayList<>();

        // 월요일부터 일요일까지 처리
        for (int i = 0; i < 7; i++) {
            LocalDate currentDate = weekStart.plusDays(i);
            List<PomodoroSession> daySessions = sessionsByDate.getOrDefault(currentDate, List.of());
            
            int totalDuration = daySessions.stream()
                    .filter(s -> s.getDurationInSeconds() != null)
                    .mapToInt(PomodoroSession::getDurationInSeconds)
                    .sum();

            // 요일 이름 생성 (예: MON, TUE)
            String dayOfWeek = currentDate.getDayOfWeek().name().substring(0, 3);

            dailyBreakdown.add(WeeklyAnalysisResponse.DailyBreakdown.builder()
                    .dayOfWeek(dayOfWeek)
                    .durationInSeconds(totalDuration)
                    .date(currentDate.format(DATE_FORMATTER))
                    .build());
        }

        return dailyBreakdown;
    }

    /**
     * 주간 요약 통계 생성
     */
    private WeeklyAnalysisResponse.WeeklySummary createWeeklySummary(List<PomodoroSession> sessions, 
                                                                   List<WeeklyAnalysisResponse.DailyBreakdown> dailyBreakdown) {
        int totalFocusTime = sessions.stream()
                .filter(Objects::nonNull)
                .filter(s -> s.getDurationInSeconds() != null)
                .mapToInt(PomodoroSession::getDurationInSeconds)
                .sum();

        int estimatedRestTime = (int) (totalFocusTime * 0.2);
        int averageDailyFocusTime = totalFocusTime / 7; // 7일로 나누기

        // 가장 집중한 요일 찾기
        String mostFocusedDay = dailyBreakdown.stream()
                .max(Comparator.comparing(WeeklyAnalysisResponse.DailyBreakdown::getDurationInSeconds))
                .map(WeeklyAnalysisResponse.DailyBreakdown::getDayOfWeek)
                .orElse("");

        return WeeklyAnalysisResponse.WeeklySummary.builder()
                .totalFocusTime(totalFocusTime)
                .totalRestTime(estimatedRestTime)
                .averageDailyFocusTime(averageDailyFocusTime)
                .mostFocusedDay(mostFocusedDay)
                .build();
    }

    /**
     * 월간 일별 집중 기록 생성
     */
    private List<MonthlyAnalysisResponse.DailyBreakdown> createMonthlyDailyBreakdown(List<PomodoroSession> sessions, 
                                                                                   LocalDate monthStart, LocalDate monthEnd) {
        // 날짜별로 그룹핑
        Map<LocalDate, List<PomodoroSession>> sessionsByDate = sessions.stream()
                .filter(Objects::nonNull)
                .filter(s -> s.getCreatedAt() != null)
                .collect(Collectors.groupingBy(session -> session.getCreatedAt().toLocalDate()));

        List<MonthlyAnalysisResponse.DailyBreakdown> dailyBreakdown = new ArrayList<>();

        // 월의 모든 날짜 처리
        LocalDate currentDate = monthStart;
        while (!currentDate.isAfter(monthEnd)) {
            List<PomodoroSession> daySessions = sessionsByDate.getOrDefault(currentDate, List.of());
            
            int totalDuration = daySessions.stream()
                    .filter(s -> s.getDurationInSeconds() != null)
                    .mapToInt(PomodoroSession::getDurationInSeconds)
                    .sum();

            dailyBreakdown.add(MonthlyAnalysisResponse.DailyBreakdown.builder()
                    .date(currentDate.format(DATE_FORMATTER))
                    .durationInSeconds(totalDuration)
                    .build());

            currentDate = currentDate.plusDays(1);
        }

        return dailyBreakdown;
    }

    /**
     * 월간 요약 통계 생성
     */
    private MonthlyAnalysisResponse.MonthlySummary createMonthlySummary(List<PomodoroSession> sessions) {
        int totalFocusTime = sessions.stream()
                .filter(Objects::nonNull)
                .filter(s -> s.getDurationInSeconds() != null)
                .mapToInt(PomodoroSession::getDurationInSeconds)
                .sum();

        int estimatedRestTime = (int) (totalFocusTime * 0.2);
        
        // 집중한 날의 수 계산
        long focusedDays = sessions.stream()
                .filter(Objects::nonNull)
                .filter(s -> s.getCreatedAt() != null)
                .collect(Collectors.groupingBy(session -> session.getCreatedAt().toLocalDate()))
                .size();

        int averageDailyFocusTime = focusedDays > 0 ? (int) (totalFocusTime / focusedDays) : 0;

        return MonthlyAnalysisResponse.MonthlySummary.builder()
                .totalFocusTime(totalFocusTime)
                .totalRestTime(estimatedRestTime)
                .averageDailyFocusTime(averageDailyFocusTime)
                .build();
    }

    /**
     * API 33: 월간 AI 분석 제안서 조회
     * 월간 집중 기록을 바탕으로 AI가 분석한 맞춤형 제안을 조회합니다.
     */
    public AIAnalysisResponse getMonthlyAIAnalysis(User user, int year, int month, String languageCode) {
        log.info("월간 AI 분석 조회 시작 - userId: {}, year: {}, month: {}, language: {}", user.getId(), year, month, languageCode);

        // 월간 범위 계산
        LocalDate monthStart = LocalDate.of(year, month, 1);
        LocalDate monthEnd = monthStart.with(TemporalAdjusters.lastDayOfMonth());
        
        LocalDateTime startDateTime = monthStart.atStartOfDay();
        LocalDateTime endDateTime = monthEnd.plusDays(1).atStartOfDay();

        // 월간 포모도로 세션 조회
        List<PomodoroSession> sessions = pomodoroSessionRepository.findByUserAndMonthWithGoal(user, startDateTime, endDateTime);

        if (sessions.isEmpty() || sessions.size() < 10) {
            log.info("월간 AI 분석 - 데이터 부족 - userId: {}, sessionCount: {}", user.getId(), sessions.size());
            return AIAnalysisResponse.empty();
        }

        try {
            // 세션 데이터를 JSON 문자열로 변환
            String analysisData = buildAnalysisDataJson(sessions);
            log.info("월간 AI 분석 데이터 준비 완료 - userId: {}, sessionCount: {}", user.getId(), sessions.size());

            // 1. GeminiService를 통한 종합 AI 분석 수행
            String aiResponse = geminiService.generateMonthlyAnalysisSuggestions(analysisData, languageCode);
            
            // 2. 개별 AI 헬퍼 메서드들을 통한 상세 분석 수행
            AIAnalysisResponse.OptimalStartTimeInfo optimalStartTime = analyzeOptimalStartTime(sessions, languageCode);
            List<AIAnalysisResponse.OptimalDayInfo> optimalDays = analyzeOptimalDays(sessions, languageCode);
            AIAnalysisResponse.LowConcentrationTimeInfo lowConcentrationTime = analyzeLowConcentrationTime(sessions, languageCode);
            List<AIAnalysisResponse.ActivitySuggestion> activitySuggestions = analyzeActivitySuggestions(sessions);
            
            // 3. 종합 코멘트 생성
            String overallComment = generateOverallAnalysisComment(sessions, optimalStartTime, optimalDays, lowConcentrationTime, languageCode);
            
            // 4. 결과 통합
            AIAnalysisResponse result = AIAnalysisResponse.builder()
                    .optimalStartTimeInfo(optimalStartTime)
                    .optimalDayInfo(optimalDays)
                    .lowConcentrationTimeInfo(lowConcentrationTime)
                    .activitySuggestions(activitySuggestions)
                    .overallComment(overallComment)
                    .build();
            
            log.info("월간 AI 분석 조회 완료 - userId: {}, year: {}, month: {}", user.getId(), year, month);
            return result;
            
        } catch (Exception e) {
            log.error("월간 AI 분석 실패 - userId: {}, year: {}, month: {}, error: {}", 
                     user.getId(), year, month, e.getMessage());
            
            // AI 분석 실패 시 안전한 fallback 응답 반환
            return AIAnalysisResponse.empty();
        }
    }

    /**
     * 포모도로 세션 데이터를 AI 분석용 JSON 문자열로 변환
     */
    private String buildAnalysisDataJson(List<PomodoroSession> sessions) {
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{\"sessions\": [");
        
        for (int i = 0; i < sessions.size(); i++) {
            PomodoroSession session = sessions.get(i);
            if (i > 0) jsonBuilder.append(",");
            
            jsonBuilder.append("{")
                    .append("\"date\": \"").append(session.getCreatedAt().toLocalDate()).append("\",")
                    .append("\"hour\": ").append(session.getCreatedAt().getHour()).append(",")
                    .append("\"dayOfWeek\": \"").append(session.getCreatedAt().getDayOfWeek()).append("\",")
                    .append("\"durationSeconds\": ").append(session.getDurationInSeconds()).append(",")
                    .append("\"isCompleted\": ").append(session.getIsCycleCompleted()).append(",")
                    .append("\"goalName\": \"").append(session.getPomodoroGoal() != null ? 
                                                      session.getPomodoroGoal().getName() : "없음").append("\"")
                    .append("}");
        }
        
        jsonBuilder.append("]}");
        return jsonBuilder.toString();
    }

    /**
     * GeminiService AI 응답을 AIAnalysisResponse로 파싱 (DTO 기반 안전한 파싱)
     */
    private AIAnalysisResponse parseAIResponse(String aiResponse, String languageCode) {
        try {
            log.info("AI 응답 파싱 시작 - responseLength: {}, language: {}", aiResponse.length(), languageCode);
            
            if (aiResponse == null || aiResponse.trim().isEmpty()) {
                log.warn("AI 응답이 비어있음, 기본 응답 반환");
                return createFallbackResponse(languageCode);
            }
            
            // ObjectMapper를 사용한 안전한 DTO 기반 파싱
            ObjectMapper objectMapper = new ObjectMapper();
            
            try {
                // AI 응답에서 JSON 추출
                String cleanJson = extractFirstJsonFromResponse(aiResponse);
                
                if (cleanJson == null || cleanJson.trim().isEmpty()) {
                    log.warn("AI 응답에서 유효한 JSON을 찾을 수 없음");
                    return createFallbackResponse(languageCode);
                }
                
                // JSON을 DTO로 직접 파싱 (타입 안전)
                AIResponseDto aiResponseDto = objectMapper.readValue(cleanJson, AIResponseDto.class);
                
                // DTO를 도메인 객체로 변환
                return convertDtoToResponse(aiResponseDto, languageCode);
                
            } catch (Exception jsonException) {
                log.error("JSON 파싱 실패: {}, AI 응답 일부: {}", 
                         jsonException.getMessage(), 
                         aiResponse.substring(0, Math.min(200, aiResponse.length())));
                
                // JSON 파싱 실패 시 언어별 기본 응답 반환
                return createFallbackResponse(languageCode);
            }
            
        } catch (Exception e) {
            log.error("AI 응답 파싱 중 예외 발생: {}", e.getMessage());
            return createFallbackResponse(languageCode);
        }
    }

    /**
     * AIResponseDto를 AIAnalysisResponse로 변환 (타입 안전한 변환)
     */
    private AIAnalysisResponse convertDtoToResponse(AIResponseDto dto, String languageCode) {
        boolean isEnglish = "en".equalsIgnoreCase(languageCode);
        
        // OptimalStartTimeInfo 변환
        AIAnalysisResponse.OptimalStartTimeInfo optimalStartTime = convertOptimalStartTime(dto.getOptimalStartTimeInfo(), isEnglish);
        
        // OptimalDayInfo 변환
        List<AIAnalysisResponse.OptimalDayInfo> optimalDays = convertOptimalDays(dto.getOptimalDayInfo(), isEnglish);
        
        // LowConcentrationTimeInfo 변환
        AIAnalysisResponse.LowConcentrationTimeInfo lowConcentrationTime = convertLowConcentrationTime(dto.getLowConcentrationTimeInfo(), isEnglish);
        
        // ActivitySuggestions 변환
        List<AIAnalysisResponse.ActivitySuggestion> activitySuggestions = convertActivitySuggestions(dto.getActivitySuggestions(), isEnglish);
        
        // OverallComment 처리
        String overallComment = dto.getOverallComment() != null ? dto.getOverallComment() : 
                (isEnglish ? "AI-analyzed monthly concentration comprehensive results." : 
                           "AI가 분석한 월간 집중도 종합 결과입니다.");
        
        return AIAnalysisResponse.builder()
                .optimalStartTimeInfo(optimalStartTime)
                .optimalDayInfo(optimalDays)
                .lowConcentrationTimeInfo(lowConcentrationTime)
                .activitySuggestions(activitySuggestions)
                .overallComment(overallComment)
                .build();
    }

    /**
     * OptimalStartTimeInfoDto를 도메인 객체로 변환
     */
    private AIAnalysisResponse.OptimalStartTimeInfo convertOptimalStartTime(AIResponseDto.OptimalStartTimeInfoDto dto, boolean isEnglish) {
        if (dto == null) {
            return AIAnalysisResponse.OptimalStartTimeInfo.builder()
                    .time("AM 09:00")
                    .pomodoroSetCount(3)
                    .interruptionRate(8.0)
                    .averageFocusTimeInMinutes(88)
                    .aiComment(isEnglish ? "AI-analyzed optimal focus start time." : "AI가 분석한 최적의 집중 시간입니다.")
                    .build();
        }
        
        return AIAnalysisResponse.OptimalStartTimeInfo.builder()
                .time(dto.getTime() != null ? dto.getTime() : "AM 09:00")
                .pomodoroSetCount(dto.getPomodoroSetCount() != null ? dto.getPomodoroSetCount() : 3)
                .interruptionRate(dto.getInterruptionRate() != null ? dto.getInterruptionRate() : 8.0)
                .averageFocusTimeInMinutes(dto.getAverageFocusTimeInMinutes() != null ? dto.getAverageFocusTimeInMinutes() : 88)
                .aiComment(dto.getAiComment() != null ? dto.getAiComment() : 
                        (isEnglish ? "AI-analyzed optimal focus start time." : "AI가 분석한 최적의 집중 시간입니다."))
                .build();
    }
    
    /**
     * OptimalDayInfoDto 리스트를 도메인 객체로 변환
     */
    private List<AIAnalysisResponse.OptimalDayInfo> convertOptimalDays(List<AIResponseDto.OptimalDayInfoDto> dtoList, boolean isEnglish) {
        if (dtoList == null || dtoList.isEmpty()) {
            return List.of(AIAnalysisResponse.OptimalDayInfo.builder()
                    .day(isEnglish ? "Wednesday" : "수요일")
                    .averageSetCount(3.4)
                    .successRate(92.0)
                    .averageFocusTimeInMinutes(98)
                    .aiComment(isEnglish ? "The day with the highest concentration." : "가장 집중력이 높은 요일입니다.")
                    .build());
        }
        
        return dtoList.stream()
                .map(dto -> AIAnalysisResponse.OptimalDayInfo.builder()
                        .day(dto.getDay() != null ? dto.getDay() : (isEnglish ? "Wednesday" : "수요일"))
                        .averageSetCount(dto.getAverageSetCount() != null ? dto.getAverageSetCount() : 3.4)
                        .successRate(dto.getSuccessRate() != null ? dto.getSuccessRate() : 92.0)
                        .averageFocusTimeInMinutes(dto.getAverageFocusTimeInMinutes() != null ? dto.getAverageFocusTimeInMinutes() : 98)
                        .aiComment(dto.getAiComment() != null ? dto.getAiComment() : 
                                (isEnglish ? "High concentration day." : "집중력이 높은 요일입니다."))
                        .build())
                .collect(Collectors.toList());
    }
    
    /**
     * LowConcentrationTimeInfoDto를 도메인 객체로 변환
     */
    private AIAnalysisResponse.LowConcentrationTimeInfo convertLowConcentrationTime(AIResponseDto.LowConcentrationTimeInfoDto dto, boolean isEnglish) {
        if (dto == null) {
            return AIAnalysisResponse.LowConcentrationTimeInfo.builder()
                    .timeRange("PM 13:00 ~ 14:00")
                    .interruptionRate(52.0)
                    .averageFocusTimeInMinutes(33)
                    .setSuccessRate(48.0)
                    .aiComment(isEnglish ? "Recommend rest or light activities during this time." : "이 시간에는 휴식이나 가벼운 활동을 추천합니다.")
                    .build();
        }
        
        return AIAnalysisResponse.LowConcentrationTimeInfo.builder()
                .timeRange(dto.getTimeRange() != null ? dto.getTimeRange() : "PM 13:00 ~ 14:00")
                .interruptionRate(dto.getInterruptionRate() != null ? dto.getInterruptionRate() : 52.0)
                .averageFocusTimeInMinutes(dto.getAverageFocusTimeInMinutes() != null ? dto.getAverageFocusTimeInMinutes() : 33)
                .setSuccessRate(dto.getSetSuccessRate() != null ? dto.getSetSuccessRate() : 48.0)
                .aiComment(dto.getAiComment() != null ? dto.getAiComment() : 
                        (isEnglish ? "Recommend rest during this time." : "이 시간에는 휴식을 추천합니다."))
                .build();
    }
    
    /**
     * ActivitySuggestionsDto를 도메인 객체로 변환
     */
    private List<AIAnalysisResponse.ActivitySuggestion> convertActivitySuggestions(AIResponseDto.ActivitySuggestionsDto dto, boolean isEnglish) {
        if (dto == null || dto.getSuggestions() == null || dto.getSuggestions().isEmpty()) {
            return List.of(AIAnalysisResponse.ActivitySuggestion.builder()
                    .activityName(isEnglish ? "Focused Study" : "집중 학습")
                    .timeRange("AM 9시 ~ 11시")
                    .build());
        }
        
        return dto.getSuggestions().stream()
                .map(suggestionDto -> AIAnalysisResponse.ActivitySuggestion.builder()
                        .activityName(suggestionDto.getActivityName() != null ? suggestionDto.getActivityName() : 
                                (isEnglish ? "Focused Study" : "집중 학습"))
                        .timeRange(suggestionDto.getTimeRange() != null ? suggestionDto.getTimeRange() : "AM 9시 ~ 11시")
                        .build())
                .collect(Collectors.toList());
    }
    
    /**
     * AI 응답에서 첫 번째 JSON 객체 추출
     */
    private String extractFirstJsonFromResponse(String response) {
        if (response == null) return null;
        String text = response.trim();
        
        int objStart = text.indexOf('{');
        if (objStart == -1) return null;
        
        int braceCount = 0;
        int objEnd = objStart;
        
        for (int i = objStart; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '{') braceCount++;
            else if (c == '}') {
                braceCount--;
                if (braceCount == 0) {
                    objEnd = i;
                    break;
                }
            }
        }
        
        if (braceCount == 0) {
            return text.substring(objStart, objEnd + 1);
        }
        
        return null;
    }
    
    /**
     * JSON Map에서 AIAnalysisResponse 구성
     */
    private AIAnalysisResponse buildAIAnalysisFromJson(Map<String, Object> jsonMap, String languageCode) {
        boolean isEnglish = "en".equalsIgnoreCase(languageCode);
        
        // optimal_start_time_info 추출
        AIAnalysisResponse.OptimalStartTimeInfo optimalStartTime = extractOptimalStartTimeInfo(jsonMap, isEnglish);
        
        // optimal_day_info 추출
        List<AIAnalysisResponse.OptimalDayInfo> optimalDays = extractOptimalDayInfo(jsonMap, isEnglish);
        
        // low_concentration_time_info 추출  
        AIAnalysisResponse.LowConcentrationTimeInfo lowConcentrationTime = extractLowConcentrationTimeInfo(jsonMap, isEnglish);
        
        // activity_suggestions 추출
        List<AIAnalysisResponse.ActivitySuggestion> activitySuggestions = extractActivitySuggestions(jsonMap, isEnglish);
        
        // overall_comment 추출 (JSON에 있으면 사용, 없으면 기본값)
        String overallComment = (String) jsonMap.getOrDefault("overall_comment", 
                isEnglish ? "AI-analyzed monthly concentration comprehensive results." : 
                           "AI가 분석한 월간 집중도 종합 결과입니다.");
        
        return AIAnalysisResponse.builder()
                .optimalStartTimeInfo(optimalStartTime)
                .optimalDayInfo(optimalDays)
                .lowConcentrationTimeInfo(lowConcentrationTime)
                .activitySuggestions(activitySuggestions)
                .overallComment(overallComment)
                .build();
    }
    
    /**
     * JSON 파싱 실패 시 언어별 기본 응답 생성
     */
    private AIAnalysisResponse createFallbackResponse(String languageCode) {
        boolean isEnglish = "en".equalsIgnoreCase(languageCode);
        
        return AIAnalysisResponse.builder()
                .optimalStartTimeInfo(AIAnalysisResponse.OptimalStartTimeInfo.builder()
                        .time("AM 09:00")
                        .pomodoroSetCount(3)
                        .interruptionRate(8.0)
                        .averageFocusTimeInMinutes(88)
                        .aiComment(isEnglish ? 
                                "AI-analyzed optimal focus start time." : 
                                "AI가 분석한 최적의 집중 시작 시간입니다.")
                        .build())
                .optimalDayInfo(List.of(
                        AIAnalysisResponse.OptimalDayInfo.builder()
                                .day(isEnglish ? "Wednesday" : "수요일")
                                .averageSetCount(3.4)
                                .successRate(92.0)
                                .averageFocusTimeInMinutes(98)
                                .aiComment(isEnglish ? 
                                        "The day with the highest concentration." : 
                                        "가장 집중력이 높은 요일입니다.")
                                .build()
                ))
                .lowConcentrationTimeInfo(AIAnalysisResponse.LowConcentrationTimeInfo.builder()
                        .timeRange("PM 13:00 ~ 14:00")
                        .interruptionRate(52.0)
                        .averageFocusTimeInMinutes(33)
                        .setSuccessRate(48.0)
                        .aiComment(isEnglish ? 
                                "Recommend rest or light activities during this time." : 
                                "이 시간에는 휴식이나 가벼운 활동을 추천합니다.")
                        .build())
                .activitySuggestions(List.of(
                        AIAnalysisResponse.ActivitySuggestion.builder()
                                .activityName(isEnglish ? "Focused Study" : "집중 학습")
                                .timeRange("AM 9시 ~ 11시")
                                .build()
                ))
                .overallComment(isEnglish ? 
                        "AI-analyzed monthly concentration comprehensive results." : 
                        "AI가 분석한 월간 집중도 종합 결과입니다.")
                .build();
    }

    /**
     * JSON에서 OptimalStartTimeInfo 추출
     */
    private AIAnalysisResponse.OptimalStartTimeInfo extractOptimalStartTimeInfo(Map<String, Object> jsonMap, boolean isEnglish) {
        Map<String, Object> optimalStartTimeMap = (Map<String, Object>) jsonMap.get("optimal_start_time_info");
        
        if (optimalStartTimeMap == null) {
            return AIAnalysisResponse.OptimalStartTimeInfo.builder()
                    .time("AM 09:00")
                    .pomodoroSetCount(3)
                    .interruptionRate(8.0)
                    .averageFocusTimeInMinutes(88)
                    .aiComment(isEnglish ? "AI-analyzed optimal focus start time." : "AI가 분석한 최적의 집중 시간입니다.")
                    .build();
        }
        
        return AIAnalysisResponse.OptimalStartTimeInfo.builder()
                .time((String) optimalStartTimeMap.getOrDefault("time", "AM 09:00"))
                .pomodoroSetCount(((Number) optimalStartTimeMap.getOrDefault("pomodoro_set_count", 3)).intValue())
                .interruptionRate(((Number) optimalStartTimeMap.getOrDefault("interruption_rate", 8.0)).doubleValue())
                .averageFocusTimeInMinutes(((Number) optimalStartTimeMap.getOrDefault("average_focus_time_in_minutes", 88)).intValue())
                .aiComment((String) optimalStartTimeMap.getOrDefault("ai_comment", 
                        isEnglish ? "AI-analyzed optimal focus start time." : "AI가 분석한 최적의 집중 시간입니다."))
                .build();
    }
    
    /**
     * JSON에서 OptimalDayInfo 리스트 추출
     */
    private List<AIAnalysisResponse.OptimalDayInfo> extractOptimalDayInfo(Map<String, Object> jsonMap, boolean isEnglish) {
        List<Map<String, Object>> optimalDayList = (List<Map<String, Object>>) jsonMap.get("optimal_day_info");
        
        if (optimalDayList == null || optimalDayList.isEmpty()) {
            return List.of(AIAnalysisResponse.OptimalDayInfo.builder()
                    .day(isEnglish ? "Wednesday" : "수요일")
                    .averageSetCount(3.4)
                    .successRate(92.0)
                    .averageFocusTimeInMinutes(98)
                    .aiComment(isEnglish ? "The day with the highest concentration." : "가장 집중력이 높은 요일입니다.")
                    .build());
        }
        
        return optimalDayList.stream()
                .map(dayMap -> AIAnalysisResponse.OptimalDayInfo.builder()
                        .day((String) dayMap.getOrDefault("day", isEnglish ? "Wednesday" : "수요일"))
                        .averageSetCount(((Number) dayMap.getOrDefault("average_set_count", 3.4)).doubleValue())
                        .successRate(((Number) dayMap.getOrDefault("success_rate", 92.0)).doubleValue())
                        .averageFocusTimeInMinutes(((Number) dayMap.getOrDefault("average_focus_time_in_minutes", 98)).intValue())
                        .aiComment((String) dayMap.getOrDefault("ai_comment", 
                                isEnglish ? "High concentration day." : "집중력이 높은 요일입니다."))
                        .build())
                .collect(Collectors.toList());
    }
    
    /**
     * JSON에서 LowConcentrationTimeInfo 추출
     */
    private AIAnalysisResponse.LowConcentrationTimeInfo extractLowConcentrationTimeInfo(Map<String, Object> jsonMap, boolean isEnglish) {
        Map<String, Object> lowConcentrationMap = (Map<String, Object>) jsonMap.get("low_concentration_time_info");
        
        if (lowConcentrationMap == null) {
            return AIAnalysisResponse.LowConcentrationTimeInfo.builder()
                    .timeRange("PM 13:00 ~ 14:00")
                    .interruptionRate(52.0)
                    .averageFocusTimeInMinutes(33)
                    .setSuccessRate(48.0)
                    .aiComment(isEnglish ? "Recommend rest or light activities during this time." : "이 시간에는 휴식이나 가벼운 활동을 추천합니다.")
                    .build();
        }
        
        return AIAnalysisResponse.LowConcentrationTimeInfo.builder()
                .timeRange((String) lowConcentrationMap.getOrDefault("time_range", "PM 13:00 ~ 14:00"))
                .interruptionRate(((Number) lowConcentrationMap.getOrDefault("interruption_rate", 52.0)).doubleValue())
                .averageFocusTimeInMinutes(((Number) lowConcentrationMap.getOrDefault("average_focus_time_in_minutes", 33)).intValue())
                .setSuccessRate(((Number) lowConcentrationMap.getOrDefault("set_success_rate", 48.0)).doubleValue())
                .aiComment((String) lowConcentrationMap.getOrDefault("ai_comment", 
                        isEnglish ? "Recommend rest during this time." : "이 시간에는 휴식을 추천합니다."))
                .build();
    }
    
    /**
     * JSON에서 ActivitySuggestion 리스트 추출
     */
    private List<AIAnalysisResponse.ActivitySuggestion> extractActivitySuggestions(Map<String, Object> jsonMap, boolean isEnglish) {
        Map<String, Object> activitySuggestionsMap = (Map<String, Object>) jsonMap.get("activity_suggestions");
        
        if (activitySuggestionsMap == null) {
            return List.of(AIAnalysisResponse.ActivitySuggestion.builder()
                    .activityName(isEnglish ? "Focused Study" : "집중 학습")
                    .timeRange("AM 9시 ~ 11시")
                    .build());
        }
        
        List<Map<String, Object>> suggestionsList = (List<Map<String, Object>>) activitySuggestionsMap.get("suggestions");
        
        if (suggestionsList == null || suggestionsList.isEmpty()) {
            return List.of(AIAnalysisResponse.ActivitySuggestion.builder()
                    .activityName(isEnglish ? "Focused Study" : "집중 학습")
                    .timeRange("AM 9시 ~ 11시")
                    .build());
        }
        
        return suggestionsList.stream()
                .map(suggestionMap -> AIAnalysisResponse.ActivitySuggestion.builder()
                        .activityName((String) suggestionMap.getOrDefault("activity_name", 
                                isEnglish ? "Focused Study" : "집중 학습"))
                        .timeRange((String) suggestionMap.getOrDefault("time_range", "AM 9시 ~ 11시"))
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * API 34: D-Day 목표 설정 (프리미엄 전용)
     * D-Day 분석을 위한 새로운 목표를 설정합니다.
     */
    @Transactional
    public ConcentrationGoalAnalysisResponse.ConcentrationGoalCreateResponse createConcentrationGoal(User user, ConcentrationGoalRequest request) {
        log.info("D-Day 목표 설정 시작 - userId: {}, goalName: {}", user.getId(), request.name());

        // 프리미엄 사용자 확인
        if (!user.getIsPremium()) {
            throw new IllegalArgumentException("D-Day 목표 설정은 프리미엄 사용자만 이용 가능합니다.");
        }

        // 목표 기간 유효성 검증
        if (!request.isValidDateRange()) {
            throw new IllegalArgumentException("목표 기간이 유효하지 않습니다. 종료일은 시작일보다 늦어야 합니다.");
        }

        if (request.isTooLong()) {
            throw new IllegalArgumentException("목표 기간이 너무 깁니다. 최대 1년까지 설정 가능합니다.");
        }

        // 새로운 집중 목표 생성
        ConcentrationGoal goal = ConcentrationGoal.builder()
                .user(user)
                .name(request.name())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .build();

        ConcentrationGoal savedGoal = concentrationGoalRepository.save(goal);

        log.info("D-Day 목표 설정 완료 - userId: {}, goalId: {}, goalName: {}", 
                user.getId(), savedGoal.getId(), savedGoal.getName());

        return ConcentrationGoalAnalysisResponse.ConcentrationGoalCreateResponse.success(savedGoal.getId());
    }

    /**
     * API 신규: D-Day 목표 목록 조회 (프리미엄 전용)
     * 사용자의 모든 D-Day 목표 목록을 조회합니다.
     */
    public ConcentrationGoalAnalysisResponse.ConcentrationGoalListResponse getConcentrationGoals(User user) {
        log.info("D-Day 목표 목록 조회 시작 - userId: {}", user.getId());

        // 프리미엄 사용자 확인
        if (!user.getIsPremium()) {
            throw new IllegalArgumentException("D-Day 목표는 프리미엄 사용자만 이용 가능합니다.");
        }

        // 사용자의 모든 목표 조회 (최신순)
        List<ConcentrationGoal> goals = concentrationGoalRepository.findByUserOrderByCreatedAtDesc(user);

        // DTO 변환
        List<ConcentrationGoalAnalysisResponse.ConcentrationGoalItem> goalItems = goals.stream()
                .map(goal -> ConcentrationGoalAnalysisResponse.ConcentrationGoalItem.builder()
                        .id(goal.getId())
                        .name(goal.getName())
                        .startDate(goal.getStartDate().format(DATE_FORMATTER))
                        .endDate(goal.getEndDate().format(DATE_FORMATTER))
                        .totalDays(goal.getTotalDays())
                        .elapsedDays(goal.getElapsedDays())
                        .remainingDays(goal.getRemainingDays())
                        .isActive(goal.isActive())
                        .isCompleted(goal.isCompleted())
                        .build())
                .collect(Collectors.toList());

        log.info("D-Day 목표 목록 조회 완료 - userId: {}, 목표 개수: {}", user.getId(), goalItems.size());

        return ConcentrationGoalAnalysisResponse.ConcentrationGoalListResponse.builder()
                .goals(goalItems)
                .build();
    }

    /**
     * API 35: D-Day 목표 분석 조회 (프리미엄 전용)
     * D-Day 목표에 대한 기간 동안의 집중도 분석 데이터를 조회합니다.
     */
    public ConcentrationGoalAnalysisResponse getConcentrationGoalAnalysis(User user, Long goalId) {
        log.info("D-Day 목표 분석 조회 시작 - userId: {}, goalId: {}", user.getId(), goalId);

        // 프리미엄 사용자 확인
        if (!user.getIsPremium()) {
            throw new IllegalArgumentException("D-Day 목표 분석은 프리미엄 사용자만 이용 가능합니다.");
        }

        // 집중 목표 조회
        ConcentrationGoal goal = concentrationGoalRepository.findByUserAndId(user, goalId)
                .orElseThrow(() -> new IllegalArgumentException("해당 D-Day 목표를 찾을 수 없습니다."));

        // 목표 기간 동안의 포모도로 세션 조회
        LocalDateTime startDateTime = goal.getStartDate().atStartOfDay();
        LocalDateTime endDateTime = goal.getEndDate().plusDays(1).atStartOfDay();
        
        List<PomodoroSession> sessions = pomodoroSessionRepository.findByUserAndDateRangeWithGoal(
                user, startDateTime, endDateTime);

        // 목표 정보 생성
        ConcentrationGoalAnalysisResponse.GoalInfo goalInfo = createGoalInfo(goal, sessions);

        // 일별 달력 데이터 생성
        List<ConcentrationGoalAnalysisResponse.DailyCalendar> dailyCalendar = createDailyCalendar(goal, sessions);

        log.info("D-Day 목표 분석 조회 완료 - userId: {}, goalId: {}, 총 집중시간: {}초", 
                user.getId(), goalId, goalInfo.getTotalFocusTime());

        return ConcentrationGoalAnalysisResponse.builder()
                .goalInfo(goalInfo)
                .dailyCalendar(dailyCalendar)
                .build();
    }

    // ==================== AI 분석 헬퍼 메서드들 ====================

    /**
     * 최적의 집중 시작 시간 분석
     */
    private AIAnalysisResponse.OptimalStartTimeInfo analyzeOptimalStartTime(List<PomodoroSession> sessions, String languageCode) {
        // 시간대별 세션 그룹핑 및 성공률 계산
        Map<Integer, List<PomodoroSession>> sessionsByHour = sessions.stream()
                .filter(Objects::nonNull)
                .filter(s -> s.getCreatedAt() != null)
                .collect(Collectors.groupingBy(session -> session.getCreatedAt().getHour()));

        int bestHour = 9; // 기본값
        double bestSuccessRate = 0.0;
        int bestSetCount = 0;
        int bestAvgFocusTime = 0;

        for (Map.Entry<Integer, List<PomodoroSession>> entry : sessionsByHour.entrySet()) {
            List<PomodoroSession> hourSessions = entry.getValue();
            if (hourSessions.size() < 3) continue; // 최소 3개 세션 필요

            double successRate = hourSessions.stream()
                    .filter(Objects::nonNull)
                    .filter(s -> s.getIsCycleCompleted() != null)
                    .mapToDouble(s -> s.getIsCycleCompleted() ? 1.0 : 0.0)
                    .average().orElse(0.0) * 100;

            if (successRate > bestSuccessRate) {
                bestSuccessRate = successRate;
                bestHour = entry.getKey();
                bestSetCount = hourSessions.size();
                bestAvgFocusTime = (int) hourSessions.stream()
                        .filter(s -> s.getDurationInSeconds() != null)
                        .mapToInt(PomodoroSession::getDurationInSeconds)
                        .average().orElse(0.0) / 60;
            }
        }

        String timeStr = String.format("%s %02d:00", bestHour < 12 ? "AM" : "PM", 
                                     bestHour == 0 ? 12 : (bestHour > 12 ? bestHour - 12 : bestHour));

        // 언어별 프롬프트 생성
        boolean isEnglish = "en".equalsIgnoreCase(languageCode);
        String prompt;
        
        if (isEnglish) {
            prompt = String.format(
                "User's Pomodoro analysis results: %s with average %d sessions, success rate %.1f%%, average focus time %d minutes. " +
                "Please explain why this time is optimal and provide focus improvement advice in 50 characters or less.",
                timeStr, bestSetCount, bestSuccessRate, bestAvgFocusTime
            );
        } else {
            prompt = String.format(
                "사용자의 포모도로 분석 결과입니다. %s에 평균 %d회 세션을 진행하며, 성공률은 %.1f%%, 평균 집중시간은 %d분입니다. " +
                "이 시간대가 최적인 이유와 집중력 향상을 위한 조언을 50자 이내로 간결하게 제시해주세요.",
                timeStr, bestSetCount, bestSuccessRate, bestAvgFocusTime
            );
        }
        
        String aiComment;
        try {
            aiComment = geminiService.generatePlainText(prompt, languageCode);
            // 50자 제한 적용
            if (aiComment.length() > 50) {
                aiComment = aiComment.substring(0, 47) + "...";
            }
        } catch (Exception e) {
            log.warn("Gemini AI 코멘트 생성 실패, 기본 메시지 사용: {}", e.getMessage());
            aiComment = isEnglish ? 
                "High success rate and most stable focus flow." :
                "성공률도 높고, 이후 집중 흐름도 가장 안정적으로 유지됩니다.";
        }

        return AIAnalysisResponse.OptimalStartTimeInfo.builder()
                .time(timeStr)
                .pomodoroSetCount(bestSetCount)
                .interruptionRate(Math.round((100 - bestSuccessRate) * 10.0) / 10.0)
                .averageFocusTimeInMinutes(bestAvgFocusTime)
                .aiComment(aiComment)
                .build();
    }

    /**
     * 최적의 집중 요일 분석 (최대 2개)
     */
    private List<AIAnalysisResponse.OptimalDayInfo> analyzeOptimalDays(List<PomodoroSession> sessions, String languageCode) {
        // 요일별 세션 그룹핑
        Map<DayOfWeek, List<PomodoroSession>> sessionsByDay = sessions.stream()
                .filter(Objects::nonNull)
                .filter(s -> s.getCreatedAt() != null)
                .collect(Collectors.groupingBy(session -> session.getCreatedAt().getDayOfWeek()));

        List<AIAnalysisResponse.OptimalDayInfo> optimalDays = new ArrayList<>();
        boolean isEnglish = "en".equalsIgnoreCase(languageCode);

        for (Map.Entry<DayOfWeek, List<PomodoroSession>> entry : sessionsByDay.entrySet()) {
            List<PomodoroSession> daySessions = entry.getValue();
            if (daySessions.size() < 3) continue;

            double successRate = daySessions.stream()
                    .filter(Objects::nonNull)
                    .filter(s -> s.getIsCycleCompleted() != null)
                    .mapToDouble(s -> s.getIsCycleCompleted() ? 1.0 : 0.0)
                    .average().orElse(0.0) * 100;

            double avgSetCount = daySessions.size() / 4.0; // 주 단위로 평균
            int avgFocusTime = (int) daySessions.stream()
                    .filter(s -> s.getDurationInSeconds() != null)
                    .mapToInt(PomodoroSession::getDurationInSeconds)
                    .average().orElse(0.0) / 60;

            // 언어별 요일명과 프롬프트 생성
            String dayName = isEnglish ? getDayNameInEnglish(entry.getKey()) : getDayNameInKorean(entry.getKey());
            String prompt;
            
            if (isEnglish) {
                prompt = String.format(
                    "%s Pomodoro analysis: Average %.1f sessions, success rate %.1f%%, average focus time %d minutes. " +
                    "Please explain why this day is good for concentration in 40 characters or less.",
                    dayName, avgSetCount, successRate, avgFocusTime
                );
            } else {
                prompt = String.format(
                    "%s의 포모도로 분석: 평균 %.1f회 세션, 성공률 %.1f%%, 평균 집중시간 %d분. " +
                    "이 요일이 집중에 좋은 이유를 40자 이내로 설명해주세요.",
                    dayName, avgSetCount, successRate, avgFocusTime
                );
            }
            
            String aiComment;
            try {
                aiComment = geminiService.generatePlainText(prompt, languageCode);
                if (aiComment.length() > 40) {
                    aiComment = aiComment.substring(0, 37) + "...";
                }
            } catch (Exception e) {
                log.warn("Gemini AI 코멘트 생성 실패: {}", e.getMessage());
                aiComment = isEnglish ? 
                    "Long focus time, low failure rate day." :
                    "집중 시간이 길고, 실패율이 낮은 요일입니다.";
            }

            optimalDays.add(AIAnalysisResponse.OptimalDayInfo.builder()
                    .day(dayName)
                    .averageSetCount(Math.round(avgSetCount * 10.0) / 10.0)
                    .successRate(Math.round(successRate * 10.0) / 10.0)
                    .averageFocusTimeInMinutes(avgFocusTime)
                    .aiComment(aiComment)
                    .build());
        }

        // 성공률 기준으로 정렬하여 상위 2개 반환
        return optimalDays.stream()
                .sorted(Comparator.comparing(AIAnalysisResponse.OptimalDayInfo::getSuccessRate).reversed())
                .limit(2)
                .collect(Collectors.toList());
    }

    /**
     * 집중도가 낮은 시간 분석
     */
    private AIAnalysisResponse.LowConcentrationTimeInfo analyzeLowConcentrationTime(List<PomodoroSession> sessions, String languageCode) {
        Map<Integer, List<PomodoroSession>> sessionsByHour = sessions.stream()
                .filter(Objects::nonNull)
                .filter(s -> s.getCreatedAt() != null)
                .collect(Collectors.groupingBy(session -> session.getCreatedAt().getHour()));

        int worstHour = 13; // 기본값 (점심시간)
        double worstSuccessRate = 100.0;

        for (Map.Entry<Integer, List<PomodoroSession>> entry : sessionsByHour.entrySet()) {
            List<PomodoroSession> hourSessions = entry.getValue();
            if (hourSessions.size() < 2) continue;

            double successRate = hourSessions.stream()
                    .filter(Objects::nonNull)
                    .filter(s -> s.getIsCycleCompleted() != null)
                    .mapToDouble(s -> s.getIsCycleCompleted() ? 1.0 : 0.0)
                    .average().orElse(1.0) * 100;

            if (successRate < worstSuccessRate) {
                worstSuccessRate = successRate;
                worstHour = entry.getKey();
            }
        }

        String timeRange = String.format("PM %02d:00 ~ %02d:00", 
                                       worstHour > 12 ? worstHour - 12 : worstHour,
                                       worstHour > 12 ? worstHour - 11 : worstHour + 1);

        List<PomodoroSession> worstHourSessions = sessionsByHour.getOrDefault(worstHour, List.of());
        int avgFocusTime = worstHourSessions.isEmpty() ? 0 : 
                (int) worstHourSessions.stream()
                        .filter(s -> s.getDurationInSeconds() != null)
                        .mapToInt(PomodoroSession::getDurationInSeconds)
                        .average().orElse(0.0) / 60;

        // 언어별 프롬프트 생성
        boolean isEnglish = "en".equalsIgnoreCase(languageCode);
        String prompt;
        
        if (isEnglish) {
            prompt = String.format(
                "%s time slot has low concentration with %.1f%% interruption rate, average focus time %d minutes, success rate %.1f%%. " +
                "Please suggest improvement strategies for this time slot in 50 characters or less.",
                timeRange, Math.round((100 - worstSuccessRate) * 10.0) / 10.0, avgFocusTime, worstSuccessRate
            );
        } else {
            prompt = String.format(
                "%s 시간대는 중단율 %.1f%%, 평균 집중시간 %d분, 성공률 %.1f%%로 집중도가 낮습니다. " +
                "이 시간대에 대한 개선 방안을 50자 이내로 제안해주세요.",
                timeRange, Math.round((100 - worstSuccessRate) * 10.0) / 10.0, avgFocusTime, worstSuccessRate
            );
        }
        
        String aiComment;
        try {
            aiComment = geminiService.generatePlainText(prompt, languageCode);
            if (aiComment.length() > 50) {
                aiComment = aiComment.substring(0, 47) + "...";
            }
        } catch (Exception e) {
            log.warn("Gemini AI 코멘트 생성 실패: {}", e.getMessage());
            aiComment = isEnglish ? 
                "Recommend rest routines or light activities during this time." :
                "이 시간에는 휴식 루틴이나 가벼운 활동을 추천합니다.";
        }

        return AIAnalysisResponse.LowConcentrationTimeInfo.builder()
                .timeRange(timeRange)
                .interruptionRate(Math.round((100 - worstSuccessRate) * 10.0) / 10.0)
                .averageFocusTimeInMinutes(avgFocusTime)
                .setSuccessRate(Math.round(worstSuccessRate * 10.0) / 10.0)
                .aiComment(aiComment)
                .build();
    }

    /**
     * 활동별 시간 제안 분석
     */
    private List<AIAnalysisResponse.ActivitySuggestion> analyzeActivitySuggestions(List<PomodoroSession> sessions) {
        // 활동별로 그룹핑
        Map<String, List<PomodoroSession>> sessionsByActivity = sessions.stream()
                .filter(Objects::nonNull)
                .filter(s -> s.getPomodoroGoal() != null && s.getPomodoroGoal().getName() != null)
                .collect(Collectors.groupingBy(session -> session.getPomodoroGoal().getName()));

        List<AIAnalysisResponse.ActivitySuggestion> suggestions = new ArrayList<>();

        for (Map.Entry<String, List<PomodoroSession>> entry : sessionsByActivity.entrySet()) {
            List<PomodoroSession> activitySessions = entry.getValue();
            if (activitySessions.size() < 3) continue;

            // 해당 활동의 최적 시간대 찾기
            Map<Integer, List<PomodoroSession>> sessionsByHour = activitySessions.stream()
                    .filter(s -> s.getCreatedAt() != null)
                    .collect(Collectors.groupingBy(session -> session.getCreatedAt().getHour()));

            int bestHour = sessionsByHour.entrySet().stream()
                    .max(Comparator.comparing(e -> e.getValue().stream()
                            .filter(Objects::nonNull)
                            .filter(s -> s.getIsCycleCompleted() != null)
                            .mapToDouble(s -> s.getIsCycleCompleted() ? 1.0 : 0.0)
                            .average().orElse(0.0)))
                    .map(Map.Entry::getKey)
                    .orElse(9);

            String timeRange = String.format("%s%d시 ~ %d시", 
                                            bestHour < 12 ? "AM " : "PM ",
                                            bestHour < 12 ? bestHour : (bestHour == 12 ? 12 : bestHour - 12),
                                            bestHour < 11 ? bestHour + 2 : (bestHour == 11 ? 1 : bestHour - 10));

            suggestions.add(AIAnalysisResponse.ActivitySuggestion.builder()
                    .activityName(entry.getKey())
                    .timeRange(timeRange)
                    .build());
        }

        return suggestions.stream().limit(3).collect(Collectors.toList()); // 상위 3개만
    }

    /**
     * 전체 월간 분석 종합 코멘트 생성
     */
    private String generateOverallAnalysisComment(List<PomodoroSession> sessions,
                                                 AIAnalysisResponse.OptimalStartTimeInfo optimalStartTime,
                                                 List<AIAnalysisResponse.OptimalDayInfo> optimalDays,
                                                 AIAnalysisResponse.LowConcentrationTimeInfo lowConcentrationTime,
                                                 String languageCode) {
        
        int totalSessions = sessions.size();
        int totalFocusTime = sessions.stream()
                .filter(Objects::nonNull)
                .filter(s -> s.getDurationInSeconds() != null)
                .mapToInt(PomodoroSession::getDurationInSeconds)
                .sum();
        int totalFocusHours = totalFocusTime / 3600;
        
        double successRate = sessions.stream()
                .filter(Objects::nonNull)
                .filter(s -> s.getIsCycleCompleted() != null)
                .mapToDouble(s -> s.getIsCycleCompleted() ? 1.0 : 0.0)
                .average().orElse(0.0) * 100;

        boolean isEnglish = "en".equalsIgnoreCase(languageCode);
        String bestDay = optimalDays.isEmpty() ? 
            (isEnglish ? "Insufficient data" : "데이터 부족") : 
            optimalDays.get(0).getDay();

        String prompt;
        if (isEnglish) {
            prompt = String.format(
                "This month's Pomodoro analysis results: Total %d sessions, %d hours of focus, success rate %.1f%%. " +
                "Optimal time: %s, optimal day: %s, weak time: %s. " +
                "Please analyze this user's focus patterns and suggest improvements for next month in 100 characters or less.",
                totalSessions, totalFocusHours, successRate,
                optimalStartTime.getTime(), bestDay, lowConcentrationTime.getTimeRange()
            );
        } else {
            prompt = String.format(
                "이번 달 포모도로 분석 결과입니다. 총 %d회 세션, %d시간 집중, 성공률 %.1f%%. " +
                "최적 시간: %s, 최적 요일: %s, 취약 시간: %s. " +
                "이 사용자의 집중 패턴을 분석하고 다음 달 개선방안을 100자 이내로 제안해주세요.",
                totalSessions, totalFocusHours, successRate,
                optimalStartTime.getTime(), bestDay, lowConcentrationTime.getTimeRange()
            );
        }

        try {
            String aiComment = geminiService.generatePlainText(prompt, languageCode);
            if (aiComment.length() > 100) {
                aiComment = aiComment.substring(0, 97) + "...";
            }
            return aiComment;
        } catch (Exception e) {
            log.warn("전체 분석 코멘트 생성 실패: {}", e.getMessage());
            if (isEnglish) {
                return String.format("You focused for %d hours this month! %s time and %s were most effective.", 
                                   totalFocusHours, optimalStartTime.getTime(), bestDay);
            } else {
                return String.format("이번 달 %d시간 집중하셨네요! %s 시간대와 %s이 가장 효과적이었습니다.", 
                                   totalFocusHours, optimalStartTime.getTime(), bestDay);
            }
        }
    }

    
    /**
     * D-Day 목표 정보 생성
     */
    private ConcentrationGoalAnalysisResponse.GoalInfo createGoalInfo(ConcentrationGoal goal, List<PomodoroSession> sessions) {
        int totalFocusTime = sessions.stream()
                .mapToInt(PomodoroSession::getDurationInSeconds)
                .sum();

        // 집중한 날 수 계산
        long daysFocused = sessions.stream()
                .collect(Collectors.groupingBy(session -> session.getCreatedAt().toLocalDate()))
                .size();

        // 목표 달성률 계산 (경과 일수 대비 집중한 날 비율)
        long elapsedDays = goal.getElapsedDays();
        double achievementRate = elapsedDays > 0 ? (double) daysFocused / elapsedDays * 100 : 0.0;

        return ConcentrationGoalAnalysisResponse.GoalInfo.builder()
                .name(goal.getName())
                .totalDays(goal.getTotalDays())
                .daysFocused(daysFocused)
                .totalFocusTime(totalFocusTime)
                .achievementRate(Math.round(achievementRate * 10.0) / 10.0)
                .remainingDays(goal.getRemainingDays())
                .build();
    }

    /**
     * D-Day 일별 달력 데이터 생성
     */
    private List<ConcentrationGoalAnalysisResponse.DailyCalendar> createDailyCalendar(ConcentrationGoal goal, List<PomodoroSession> sessions) {
        // 날짜별 세션 그룹핑
        Map<LocalDate, List<PomodoroSession>> sessionsByDate = sessions.stream()
                .filter(Objects::nonNull)
                .filter(s -> s.getCreatedAt() != null)
                .collect(Collectors.groupingBy(session -> session.getCreatedAt().toLocalDate()));

        List<ConcentrationGoalAnalysisResponse.DailyCalendar> calendar = new ArrayList<>();

        // 목표 기간의 모든 날짜 처리
        LocalDate currentDate = goal.getStartDate();
        while (!currentDate.isAfter(goal.getEndDate())) {
            List<PomodoroSession> daySessions = sessionsByDate.getOrDefault(currentDate, List.of());
            
            int totalDuration = daySessions.stream()
                    .filter(Objects::nonNull)
                    .filter(s -> s.getDurationInSeconds() != null)
                    .mapToInt(PomodoroSession::getDurationInSeconds)
                    .sum();

            String obooniImageType = ConcentrationGoalAnalysisResponse.determineObooniImageType(totalDuration);

            calendar.add(ConcentrationGoalAnalysisResponse.DailyCalendar.builder()
                    .date(currentDate.format(DATE_FORMATTER))
                    .durationInSeconds(totalDuration)
                    .obooniImageType(obooniImageType)
                    .build());

            currentDate = currentDate.plusDays(1);
        }

        return calendar;
    }

    /**
     * 요일을 한국어로 변환
     */
    private String getDayNameInKorean(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> "월요일";
            case TUESDAY -> "화요일";
            case WEDNESDAY -> "수요일";
            case THURSDAY -> "목요일";
            case FRIDAY -> "금요일";
            case SATURDAY -> "토요일";
            case SUNDAY -> "일요일";
        };
    }

    /**
     * 요일을 영어로 변환
     */
    private String getDayNameInEnglish(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> "Monday";
            case TUESDAY -> "Tuesday";
            case WEDNESDAY -> "Wednesday";
            case THURSDAY -> "Thursday";
            case FRIDAY -> "Friday";
            case SATURDAY -> "Saturday";
            case SUNDAY -> "Sunday";
        };
    }
}