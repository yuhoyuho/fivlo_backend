package com.fivlo.fivlo_backend.common.ai;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/** Gemini AI 서비스 (Google Gen AI SDK) */
@Service
public class GeminiService {

    private static final Logger logger = LoggerFactory.getLogger(GeminiService.class);

    private final Client client;
    private final String model;

    public GeminiService(Client client, String genaiModelName) {
        this.client = client;
        this.model = genaiModelName; // 예: gemini-2.5-flash
    }

    /** 동기 호출 */
    public String generateContent(String prompt) {
        try {
            if (prompt == null) throw new IllegalArgumentException("prompt is null");
            logger.debug("Generating content, prompt preview: {}", prompt.substring(0, Math.min(100, prompt.length())));

            // 공식 예제: client.models.generateContent(model, contents, config)
            GenerateContentResponse res = client.models.generateContent(model, prompt, null);
            String text = res.text(); // quick accessor
            logger.debug("Generated content length: {}", (text != null ? text.length() : 0));

            // AI 응답에서 마크다운 코드 블록 제거
            if (text != null) {
                text = cleanMarkdownCodeBlocks(text);
            }

            return text;

        } catch (Exception e) {
            logger.error("Error generating content with Gemini", e);
            throw new RuntimeException("AI 콘텐츠 생성 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /** 비동기 호출 */
    public CompletableFuture<String> generateContentAsync(String prompt) {
        return CompletableFuture.supplyAsync(() -> generateContent(prompt));
    }

    // 아래 도우미들은 기존 프롬프트 조립 로직을 그대로 사용하고,
    // 마지막에 this.generateContent(prompt)만 호출하도록 유지합니다.
    public String analyzeGoalAndRecommendTasks(String goalContent, String goalType, String startDate, String endDate) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("당신은 생산성 앱의 AI 어시스턴트입니다. 사용자의 목표를 분석하고 실행 가능한 Task들을 추천해주세요.\n\n")
                .append("목표: ").append(goalContent).append("\n")
                .append("목표 유형: ").append(goalType).append("\n");
        if (startDate != null && endDate != null) {
            prompt.append("기간: ").append(startDate).append(" ~ ").append(endDate).append("\n");
        }
        prompt.append("\n다음 JSON 형식으로 응답해주세요:\n")
                .append("{\n")
                .append("  \"recommended_tasks\": [\n")
                .append("    {\"content\": \"구체적인 Task 내용\", \"due_date\": \"YYYY-MM-DD\", \"repeat_type\": \"DAILY 또는 NONE\", \"end_date\": \"YYYY-MM-DD 또는 null\"}\n")
                .append("  ]\n")
                .append("}\n")
                .append("- Task는 구체적이고 실행 가능해야 합니다\n")
                .append("- 목표 달성에 도움이 되는 순서로 배치해주세요\n")
                .append("- 각 Task는 하루에 완료 가능한 크기여야 합니다\\n")
                .append("주의: ```json 같은 마크다운 문법을 사용하지 말고, { 로 시작하는 순수 JSON만 반환하세요.");
        return generateContent(prompt.toString());
    }

    public String recommendTimeAttackSteps(String goalName, Integer totalDurationInSeconds) {
        int totalMinutes = Math.max(0, (totalDurationInSeconds != null ? totalDurationInSeconds : 0) / 60);
        StringBuilder prompt = new StringBuilder();
        prompt.append("당신은 시간 관리 전문가입니다. 다음 활동을 효율적으로 완료할 수 있도록 단계별 일정을 추천해주세요.\n\n")
                .append("활동: ").append(goalName).append("\n")
                .append("총 시간: ").append(totalMinutes).append("분\n\n")
                .append("다음 JSON 형식으로만 응답해주세요. 마크다운 코드 블록이나 추가 설명 없이 순수 JSON만 반환하세요:\\n")
                .append("{\\\"recommended_steps\\\": [{\\\"content\\\": \\\"단계별 활동 내용\\\", \\\"duration_in_seconds\\\": 할당된_시간_초단위}]}\\n\\n")
                .append("주의: ```json 같은 마크다운 문법을 사용하지 말고, { 로 시작하는 순수 JSON만 반환하세요.\\n")
                .append("- 각 단계는 논리적 순서를 가져야 합니다\n")
                .append("- 총 시간이 정확히 ").append(totalDurationInSeconds).append("초가 되도록 배분해주세요\n")
                .append("- 단계는 3-8개 정도가 적당합니다");
        return generateContent(prompt.toString());
    }

    public String generateMonthlyAnalysisSuggestions(String analysisData) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("당신은 집중도 분석 전문가입니다. 사용자의 월간 포모도로 집중 데이터를 분석하고 맞춤형 제안을 해주세요.\n\n")
                .append("분석 데이터:\n").append(analysisData).append("\n\n")
                .append("{\n")
                .append("  \"optimal_start_time_info\": {\"time\": \"AM/PM HH:MM\", \"ai_comment\": \"최적 시간대 선택 이유\"},\n")
                .append("  \"optimal_day_info\": [{\"day\": \"요일명\", \"ai_comment\": \"해당 요일이 좋은 이유\"}],\n")
                .append("  \"low_concentration_time_info\": {\"time_range\": \"PM HH:MM ~ HH:MM\", \"ai_comment\": \"집중도가 낮은 시간대 활용 방법\"},\n")
                .append("  \"activity_suggestions\": {\"suggestions\": [{\"activity_name\": \"활동명\", \"time_range\": \"추천 시간대\"}], \"ai_comment\": \"활동별 시간 배치 조언\"}\n")
                .append("}\n")
                .append("- 실제 데이터에 기반한 개인화된 조언을 제공해주세요\n")
                .append("- 구체적이고 실행 가능한 제안을 해주세요\\n")
                .append("주의: ```json 같은 마크다운 문법을 사용하지 말고, { 로 시작하는 순수 JSON만 반환하세요.");
        return generateContent(prompt.toString());
    }

    /**
     * AI 응답에서 마크다운 코드 블록을 제거하여 순수 JSON만 추출
     */
    private String cleanMarkdownCodeBlocks(String response) {
        if (response == null) return null;
        
        // ```json ... ``` 형태의 마크다운 코드 블록 제거
        String cleaned = response.trim();
        
        // 시작 부분의 ```json 또는 ``` 제거
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        
        // 끝 부분의 ``` 제거
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        
        return cleaned.trim();
    }
}
