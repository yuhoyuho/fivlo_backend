package com.fivlo.fivlo_backend.security.oauth2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Map;

@Component
public class AppleTokenDecoder {
    public Map<String, String> getPayload(String idToken) {
        try {
            String payload = idToken.split("\\.")[1];
            String decodedPayload = new String(Base64.getUrlDecoder().decode(payload));
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(decodedPayload, Map.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Apple ID 토큰을 디코딩하는 데 실패했습니다.", e);
        }
    }
}
