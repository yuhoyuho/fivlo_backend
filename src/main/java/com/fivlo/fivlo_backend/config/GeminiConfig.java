package com.fivlo.fivlo_backend.config;

import com.google.genai.Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Google Gen AI SDK 설정 */
@Configuration
public class GeminiConfig {

    @Value("${gemini.api.key:}")           // application.yml 또는 env로 주입
    private String apiKey;

    @Value("${gemini.model.name:gemini-2.5-flash}")
    private String modelName;

    /** Gen AI 클라이언트 Bean */
    @Bean
    public Client genaiClient() {
        // API Key를 코드로 주입하거나, 환경변수 GOOGLE_API_KEY 설정도 가능
        // Client.builder()는 공식 문서 예제와 동일합니다.
        // https://github.com/googleapis/java-genai (Client.builder().apiKey(...).build())
        return (apiKey != null && !apiKey.isEmpty())
                ? Client.builder().apiKey(apiKey).build()
                : new Client(); // GOOGLE_API_KEY 환경변수 자동 사용
    }

    /** 기본 모델명 Bean (주입용) */
    @Bean
    public String genaiModelName() {
        return modelName;
    }
}
