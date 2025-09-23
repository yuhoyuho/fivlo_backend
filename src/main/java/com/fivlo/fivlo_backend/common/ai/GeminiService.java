package com.fivlo.fivlo_backend.common.ai;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/** Gemini AI 서비스 (Google Gen AI SDK) */
@Service
public class GeminiService {

    private static final Logger logger = LoggerFactory.getLogger(GeminiService.class);
    
    // Redis 캐싱 설정
    private static final String CACHE_KEY_PREFIX = "ai:gemini:";
    private static final Duration CACHE_TTL = Duration.ofHours(24); // 24시간 캐시

    private final Client client;
    private final String model;
    private final RedisTemplate<String, Object> redisTemplate;

    public GeminiService(Client client, String genaiModelName, RedisTemplate<String, Object> redisTemplate) {
        this.client = client;
        this.model = genaiModelName; // 예: gemini-2.5-flash
        this.redisTemplate = redisTemplate;
    }

    /** 
     * 동기 호출 (Redis 캐싱 지원)
     * 프롬프트 기반으로 캐시 키를 생성하여 중복 호출 방지
     */
    public String generateContent(String prompt) {
        try {
            if (prompt == null) throw new IllegalArgumentException("prompt is null");
            
            // 1. 캐시 키 생성 (프롬프트 해시 기반)
            String cacheKey = generateCacheKey(prompt);
            
            // 2. 캐시에서 먼저 조회
            String cachedResponse = getCachedResponse(cacheKey);
            if (cachedResponse != null) {
                logger.debug("Cache HIT for prompt preview: {}", prompt.substring(0, Math.min(50, prompt.length())));
                return cachedResponse;
            }
            
            // 3. 캐시 미스 - 실제 AI 호출
            logger.debug("Cache MISS - Generating content, prompt preview: {}", prompt.substring(0, Math.min(100, prompt.length())));

            // JSON만 생성하도록 모델에 강제
            GenerateContentConfig cfg = GenerateContentConfig.builder()
                    .responseMimeType("application/json") // JSON만 달라!
                    .build();

            // 공식 시그니처: (model, contents, config)
            GenerateContentResponse res = client.models.generateContent(model, prompt, cfg);
            String text = res.text();
            logger.debug("Generated content length: {}", (text != null ? text.length() : 0));

            // 혹시라도 모델이 앞뒤로 설명/마크다운을 섞어 보내면 첫 번째 JSON만 추출
            String response = extractFirstJson(text);
            
            // 4. 응답을 캐시에 저장
            cacheResponse(cacheKey, response);
            
            return response;

        } catch (Exception e) {
            logger.error("Error generating content with Gemini", e);
            throw new RuntimeException("AI 콘텐츠 생성 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /** 비동기 호출 */
    public CompletableFuture<String> generateContentAsync(String prompt) {
        return CompletableFuture.supplyAsync(() -> generateContent(prompt));
    }


    /** 일반 텍스트 응답용 동기 호출 (집중도 분석용) - 언어별 지원 + Redis 캐싱 */
    public String generatePlainText(String prompt, String languageCode) {
        try {
            if (prompt == null) throw new IllegalArgumentException("prompt is null");
            
            // 1. 언어 코드를 포함한 캐시 키 생성 (언어별 다른 응답)
            String cacheKey = generateCacheKeyWithLanguage(prompt, languageCode);
            
            // 2. 캐시에서 먼저 조회
            String cachedResponse = getCachedResponse(cacheKey);
            if (cachedResponse != null) {
                logger.debug("Cache HIT for plain text, prompt preview: {}, language: {}", 
                           prompt.substring(0, Math.min(50, prompt.length())), languageCode);
                return cachedResponse;
            }
            
            // 3. 캐시 미스 - 실제 AI 호출
            logger.debug("Cache MISS - Generating plain text, prompt preview: {}, language: {}", 
                        prompt.substring(0, Math.min(100, prompt.length())), languageCode);

            // 일반 텍스트 응답을 위한 설정 (JSON 강제 없음)
            GenerateContentConfig cfg = GenerateContentConfig.builder()
                    .build(); // JSON 강제 제거

            GenerateContentResponse res = client.models.generateContent(model, prompt, cfg);
            String text = res.text();
            logger.debug("Generated plain text length: {}", (text != null ? text.length() : 0));

            if (text == null || text.trim().isEmpty()) {
                // 언어별 기본 메시지
                String defaultMessage = getDefaultErrorMessage(languageCode);
                // 기본 메시지도 캐시 (에러 응답 중복 방지)
                cacheResponse(cacheKey, defaultMessage);
                return defaultMessage;
            }

            String response = text.trim();
            
            // 4. 응답을 캐시에 저장
            cacheResponse(cacheKey, response);
            
            return response;

        } catch (Exception e) {
            logger.error("Error generating plain text with Gemini", e);
            // 언어별 에러 메시지
            String errorMessage = getErrorMessage(languageCode);
            throw new RuntimeException(errorMessage + ": " + e.getMessage(), e);
        }
    }
    
    /** 기존 메서드 호환성 유지 (기본값: 한국어) */
    public String generatePlainText(String prompt) {
        return generatePlainText(prompt, "ko");
    }

    // === 프롬프트 도우미들 ===

    public String analyzeGoalAndRecommendTasks(String goalContent, String goalType, String startDate, String endDate, String languageCode) {
        // 언어별 프롬프트 생성
        String prompt = buildTaskRecommendationPrompt(goalContent, goalType, startDate, endDate, languageCode);
        
        return generateContent(prompt);
    }
    
    // 기존 메서드 호환성 유지 (기본값: 한국어)
    public String analyzeGoalAndRecommendTasks(String goalContent, String goalType, String startDate, String endDate) {
        return analyzeGoalAndRecommendTasks(goalContent, goalType, startDate, endDate, "ko");
    }
    
    /**
     * 언어별 Task 추천 프롬프트 생성
     */
    private String buildTaskRecommendationPrompt(String goalContent, String goalType, String startDate, String endDate, String languageCode) {
        StringBuilder prompt = new StringBuilder();
        
        if ("en".equalsIgnoreCase(languageCode)) {
            // 영어 프롬프트
            prompt.append("You are a planning design expert. Please provide specific steps to complete the following goal.\\n\\n")
                    .append("- Goal: ").append(goalContent).append("\\n")
                    .append("- Goal Type: ").append(goalType).append("\\n");
        
            if (startDate != null && endDate != null) {
                prompt.append("- Period: ").append(startDate).append(" ~ ").append(endDate).append("\\n");
            }
        
            prompt.append("Response must be provided only in the JSON format below, without any other explanations or markdown.\\n")
                    .append("{\\n")
                    .append("  \\\"recommended_tasks\\\": [\\n")
                    .append("    {\\n")
                    .append("      \\\"content\\\": \\\"Specific task content generated by AI\\\",\\n")
                    .append("      \\\"due_date\\\": \\\"YYYY-MM-DD\\\",\\n")
                    .append("      \\\"repeat_type\\\": \\\"DAILY\\\",\\n")
                    .append("      \\\"end_date\\\": \\\"YYYY-MM-DD or null\\\"\\n")
                    .append("    }\\n")
                    .append("  ]\\n")
                    .append("}\\n");
        
            prompt.append("- Generate 3 to 5 specific and actionable tasks to achieve the 'goal'.\\n");
        
            if ("DEFINITE".equalsIgnoreCase(goalType)) {
                prompt.append("- Each task's 'due_date' should be logically distributed between '").append(startDate).append("' and '").append(endDate).append("'.\\n");
                prompt.append("- All tasks' 'end_date' must be set to '").append(endDate).append("'.\\n");
            } else { // INDEFINITE or null
                prompt.append("- Suggest about 3-5 tasks per week that can be repeated weekly.\\n");
                prompt.append("- All tasks' 'end_date' must be set to null.\\n");
            }
        
            prompt.append("- All tasks' 'repeat_type' should be fixed to 'DAILY'.\\n");
            prompt.append("- Content should be as specific as possible within 20 characters.");
            
        } else {
            // 한국어 프롬프트 (기본값)
            prompt.append("당신은 계획 설계 전문가입니다. 다음 목표를 완료할 수 있도록 구체적인 단계를 제시해주세요.\\n\\n")
                    .append("- 목표: ").append(goalContent).append("\\n")
                    .append("- 목표 유형: ").append(goalType).append("\\n");
            
            if (startDate != null && endDate != null) {
                prompt.append("- 기간: ").append(startDate).append(" ~ ").append(endDate).append("\\n");
            }
            
            prompt.append("응답은 반드시 아래 JSON 형식으로만, 다른 설명이나 마크다운 없이 제공해야 합니다.\\n")
                    .append("{\\n")
                    .append("  \\\"recommended_tasks\\\": [\\n")
                    .append("    {\\n")
                    .append("      \\\"content\\\": \\\"AI가 생성한 구체적인 Task 내용\\\",\\n")
                    .append("      \\\"due_date\\\": \\\"YYYY-MM-DD\\\",\\n")
                    .append("      \\\"repeat_type\\\": \\\"DAILY\\\",\\n")
                    .append("      \\\"end_date\\\": \\\"YYYY-MM-DD 또는 null\\\"\\n")
                    .append("    }\\n")
                    .append("  ]\\n")
                    .append("}\\n");
            
            prompt.append("- '목표'를 달성하기 위한 구체적이고 실천 가능한 Task를 3개에서 5개 사이로 생성하세요.\\n");
            
            if ("DEFINITE".equalsIgnoreCase(goalType)) {
                prompt.append("- 각 Task의 'due_date'는 '").append(startDate).append("'와 '").append(endDate).append("' 사이에서 논리적으로 분배되어야 합니다.\\n");
                prompt.append("- 모든 Task의 'end_date'는 반드시 '").append(endDate).append("'로 설정하세요.\\n");
            } else { // INDEFINITE 또는 null인 경우
                prompt.append("- 단계는 매주 반복할 수 있는 정도로 1주에 3~5개 정도로 제안하세요.\\n");
                prompt.append("- 모든 Task의 'end_date'는 반드시 null로 설정하세요.\\n");
            }
            
            prompt.append("- 모든 Task의 'repeat_type'은 'DAILY'로 고정하세요.\\n");
            prompt.append("- 'content'에 들어갈 내용은 최대 10자 이내로 최대한 구체적으로 설정하세요.");
            }
        return prompt.toString();
    }

    public String recommendTimeAttackSteps(String goalName, Integer totalDurationInSeconds, String languageCode) {
        int totalMinutes = Math.max(0, (totalDurationInSeconds != null ? totalDurationInSeconds : 0) / 60);
        
        // 언어별 프롬프트 생성
        String prompt = buildTimeAttackPrompt(goalName, totalMinutes, totalDurationInSeconds, languageCode);
        
        return generateContent(prompt);
    }
    
    // 기존 메서드 호환성 유지 (기본값: 한국어)
    public String recommendTimeAttackSteps(String goalName, Integer totalDurationInSeconds) {
        return recommendTimeAttackSteps(goalName, totalDurationInSeconds, "ko");
    }
    
    /**
     * 언어별 타임어택 프롬프트 생성
     */
    private String buildTimeAttackPrompt(String goalName, int totalMinutes, int totalDurationInSeconds, String languageCode) {
        StringBuilder prompt = new StringBuilder();
        
        if ("en".equalsIgnoreCase(languageCode)) {
            // 영어 프롬프트
            prompt.append("You are a time management expert. Please recommend step-by-step schedule to efficiently complete the following activity.\n\n")
                    .append("Activity: ").append(goalName).append("\n")
                    .append("Total time: ").append(totalMinutes).append(" minutes\n\n")
                    .append("Please respond only in the JSON format below, without any additional text or markdown.\n")
                    .append("{\n")
                    .append("  \"recommended_steps\": [\n")
                    .append("    { \"content\": \"Step-by-step activity content\", \"duration_in_seconds\": 0 }\n")
                    .append("  ]\n")
                    .append("}\n")
                    .append("- Steps should be in logical order with 3-5 suggestions.\n")
                    .append("- The total time of all steps should be exactly ").append(totalDurationInSeconds).append(" seconds.\n");
        } else {
            // 한국어 프롬프트 (기본값)
            prompt.append("당신은 시간 관리 전문가입니다. 다음 활동을 효율적으로 완료할 수 있도록 단계별 일정을 추천해주세요.\n\n")
                    .append("활동: ").append(goalName).append("\n")
                    .append("총 시간: ").append(totalMinutes).append("분\n\n")
                    .append("오직 아래 JSON 형식으로만, 추가 텍스트/마크다운 없이 응답하세요.\n")
                    .append("{\n")
                    .append("  \"recommended_steps\": [\n")
                    .append("    { \"content\": \"단계별 활동 내용\", \"duration_in_seconds\": 0 }\n")
                    .append("  ]\n")
                    .append("}\n")
                    .append("- 단계는 논리적 순서를 가지며 3~5개로 제안하세요.\n")
                    .append("- 전체 단계의 시간 합은 정확히 ").append(totalDurationInSeconds).append("초가 되도록 분배하세요.\n");
        }
        
        return prompt.toString();
    }
    

    public String generateMonthlyAnalysisSuggestions(String analysisData, String languageCode) {
        // 언어별 프롬프트 생성
        String prompt = buildMonthlyAnalysisPrompt(analysisData, languageCode);
        
        return generateContent(prompt);
    }
    
    // 기존 메서드 호환성 유지 (기본값: 한국어)
    public String generateMonthlyAnalysisSuggestions(String analysisData) {
        return generateMonthlyAnalysisSuggestions(analysisData, "ko");
    }
    
    /**
     * 언어별 월간 분석 프롬프트 생성
     */
    private String buildMonthlyAnalysisPrompt(String analysisData, String languageCode) {
        StringBuilder prompt = new StringBuilder();
        
        if ("en".equalsIgnoreCase(languageCode)) {
            // 영어 프롬프트
            prompt.append("You are a focus analysis expert. Please analyze the user's monthly Pomodoro focus data and provide customized suggestions.\\n\\n")
                    .append("Analysis Data:\\n").append(analysisData).append("\\n\\n")
                    .append("Respond only in the JSON format below, without any additional text.\\n")
                    .append("{\\n")
                    .append("  \\\"optimal_start_time_info\\\": {\\\"time\\\": \\\"AM/PM HH:MM\\\", \\\"ai_comment\\\": \\\"Reason for optimal time selection\\\"},\\n")
                    .append("  \\\"optimal_day_info\\\": [{\\\"day\\\": \\\"Day name\\\", \\\"ai_comment\\\": \\\"Why this day is good\\\"}],\\n")
                    .append("  \\\"low_concentration_time_info\\\": {\\\"time_range\\\": \\\"PM HH:MM ~ HH:MM\\\", \\\"ai_comment\\\": \\\"How to utilize low concentration time\\\"},\\n")
                    .append("  \\\"activity_suggestions\\\": {\\\"suggestions\\\": [{\\\"activity_name\\\": \\\"Activity name\\\", \\\"time_range\\\": \\\"Recommended time\\\"}], \\\"ai_comment\\\": \\\"Activity time allocation advice\\\"}\\n")
                    .append("}\\n");
        } else {
            // 한국어 프롬프트 (기본값)
            prompt.append("당신은 집중도 분석 전문가입니다. 사용자의 월간 포모도로 집중 데이터를 분석하고 맞춤형 제안을 해주세요.\\n\\n")
                    .append("분석 데이터:\\n").append(analysisData).append("\\n\\n")
                    .append("오직 아래 JSON 형식으로만, 추가 텍스트 없이 응답하세요.\\n")
                    .append("{\\n")
                    .append("  \\\"optimal_start_time_info\\\": {\\\"time\\\": \\\"AM/PM HH:MM\\\", \\\"ai_comment\\\": \\\"최적 시간대 선택 이유\\\"},\\n")
                    .append("  \\\"optimal_day_info\\\": [{\\\"day\\\": \\\"요일명\\\", \\\"ai_comment\\\": \\\"해당 요일이 좋은 이유\\\"}],\\n")
                    .append("  \\\"low_concentration_time_info\\\": {\\\"time_range\\\": \\\"PM HH:MM ~ HH:MM\\\", \\\"ai_comment\\\": \\\"집중도가 낮은 시간대 활용 방법\\\"},\\n")
                    .append("  \\\"activity_suggestions\\\": {\\\"suggestions\\\": [{\\\"activity_name\\\": \\\"활동명\\\", \\\"time_range\\\": \\\"추천 시간대\\\"}], \\\"ai_comment\\\": \\\"활동별 시간 배치 조언\\\"}\\n")
                    .append("}\\n");
        }
        
        return prompt.toString();
    }

    /**
     * 모델 응답에서 첫 번째 JSON(Object/Array)을 안전하게 추출
     * - 앞뒤 설명, 마크다운, 로그 텍스트가 섞여 와도 동작
     */
    private String extractFirstJson(String s) {
        if (s == null) return null;
        String text = s.trim();

        int objStart = text.indexOf('{');
        int arrStart = text.indexOf('[');

        if (objStart == -1 && arrStart == -1) {
            return text; // 어차피 ObjectMapper가 다시 실패를 던질 것
        }

        int start = -1;
        char open, close;
        if (objStart != -1 && (arrStart == -1 || objStart < arrStart)) {
            start = objStart; open = '{'; close = '}';
        } else {
            start = arrStart; open = '['; close = ']';
        }

        int depth = 0;
        boolean inString = false;
        boolean escape = false;

        for (int i = start; i < text.length(); i++) {
            char c = text.charAt(i);

            if (escape) { escape = false; continue; }
            if (c == '\\') { escape = true; continue; }
            if (c == '"') { inString = !inString; continue; }
            if (inString) continue;

            if (c == open) depth++;
            else if (c == close) {
                depth--;
                if (depth == 0) {
                    return text.substring(start, i + 1).trim();
                }
            }
        }

        // 매칭 실패 시, 일단 시작부터 끝까지 반환(파서가 다시 검증)
        return text.substring(start).trim();
    }
    
    // === 언어별 메시지 헬퍼 메서드들 ===
    
    /**
     * 언어별 기본 에러 메시지 반환
     */
    private String getDefaultErrorMessage(String languageCode) {
        if ("en".equalsIgnoreCase(languageCode)) {
            return "Unable to generate analysis results.";
        }
        return "분석 결과를 생성할 수 없습니다.";
    }
    
    /**
     * 언어별 에러 메시지 반환  
     */
    private String getErrorMessage(String languageCode) {
        if ("en".equalsIgnoreCase(languageCode)) {
            return "An error occurred while generating AI text";
        }
        return "AI 텍스트 생성 중 오류가 발생했습니다";
    }
    
    // === Redis 캐싱 유틸리티 메서드들 ===
    
    /**
     * 프롬프트 기반 캐시 키 생성
     * 프롬프트의 해시값을 사용하여 고유한 캐시 키 생성
     */
    private String generateCacheKey(String prompt) {
        if (prompt == null || prompt.trim().isEmpty()) {
            return CACHE_KEY_PREFIX + "empty";
        }
        
        // 프롬프트를 정규화하여 일관된 키 생성
        String normalizedPrompt = prompt.trim().replaceAll("\\s+", " ");
        int hash = normalizedPrompt.hashCode();
        
        // 양수로 변환하여 키 생성
        String hashString = String.valueOf(Math.abs(hash));
        return CACHE_KEY_PREFIX + hashString;
    }
    
    /**
     * 언어별 캐시 키 생성 (generatePlainText용)
     * 같은 프롬프트라도 언어가 다르면 다른 캐시 키 생성
     */
    private String generateCacheKeyWithLanguage(String prompt, String languageCode) {
        if (prompt == null || prompt.trim().isEmpty()) {
            return CACHE_KEY_PREFIX + "empty:" + (languageCode != null ? languageCode : "ko");
        }
        
        // 프롬프트를 정규화하여 일관된 키 생성
        String normalizedPrompt = prompt.trim().replaceAll("\\s+", " ");
        String combinedInput = normalizedPrompt + ":" + (languageCode != null ? languageCode : "ko");
        int hash = combinedInput.hashCode();
        
        // 양수로 변환하여 키 생성
        String hashString = String.valueOf(Math.abs(hash));
        return CACHE_KEY_PREFIX + hashString + ":" + (languageCode != null ? languageCode : "ko");
    }
    
    /**
     * 캐시에서 응답 조회
     */
    private String getCachedResponse(String cacheKey) {
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached instanceof String) {
                return (String) cached;
            }
            return null;
        } catch (Exception e) {
            logger.warn("Failed to get cached response for key: {}", cacheKey, e);
            return null;
        }
    }
    
    /**
     * 응답을 캐시에 저장
     */
    private void cacheResponse(String cacheKey, String response) {
        try {
            redisTemplate.opsForValue().set(cacheKey, response, CACHE_TTL);
            logger.debug("Cached AI response with key: {}, TTL: {}h", cacheKey, CACHE_TTL.toHours());
        } catch (Exception e) {
            logger.warn("Failed to cache AI response for key: {}", cacheKey, e);
        }
    }
    
    /**
     * 캐시 통계 조회 (디버깅/모니터링용)
     */
    public void logCacheStats() {
        try {
            // Redis 키 패턴으로 캐시된 항목 수 조회
            logger.info("AI Cache Statistics - Current cached items with prefix: {}", CACHE_KEY_PREFIX);
        } catch (Exception e) {
            logger.warn("Failed to get cache statistics", e);
        }
    }
}
