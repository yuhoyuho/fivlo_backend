package com.fivlo.fivlo_backend.security.oauth2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApplePublicKeyProvider {

    private static final String APPLE_KEYS_URL = "https://appleid.apple.com/auth/keys";
    private static final long CACHE_EXPIRATION_MS = 24 * 60 * 60 * 1000; // 24시간

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    private final Map<String, CachedKey> keyCache = new ConcurrentHashMap<>();
    private volatile long lastFetchTime = 0;
    private volatile JsonNode cachedKeys = null;

    public PublicKey getPublicKey(String kid) {
        // 캐시된 키가 있으면 반환
        CachedKey cached = keyCache.get(kid);
        if (cached != null && !cached.isExpired()) {
            return cached.key;
        }

        // Apple 서버에서 키 가져오기
        refreshKeysIfNeeded();

        // 키 찾기 및 파싱
        PublicKey key = findAndParseKey(kid);
        keyCache.put(kid, new CachedKey(key));
        return key;
    }

    private synchronized void refreshKeysIfNeeded() {
        long now = System.currentTimeMillis();
        if (cachedKeys == null || (now - lastFetchTime) > CACHE_EXPIRATION_MS) {
            try {
                String response = webClient.get()
                        .uri(APPLE_KEYS_URL)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                cachedKeys = objectMapper.readTree(response);
                lastFetchTime = now;
                keyCache.clear();
                log.info("Apple 공개키를 새로 가져왔습니다.");
            } catch (Exception e) {
                throw new RuntimeException("Apple 공개키를 가져오는 데 실패했습니다.", e);
            }
        }
    }

    private PublicKey findAndParseKey(String kid) {
        if (cachedKeys == null || !cachedKeys.has("keys")) {
            throw new IllegalStateException("Apple 공개키가 로드되지 않았습니다.");
        }

        JsonNode keys = cachedKeys.get("keys");
        for (JsonNode keyNode : keys) {
            if (kid.equals(keyNode.get("kid").asText())) {
                return parseRsaPublicKey(keyNode);
            }
        }

        // 키를 찾지 못한 경우, 캐시를 무효화하고 다시 시도
        cachedKeys = null;
        refreshKeysIfNeeded();

        for (JsonNode keyNode : cachedKeys.get("keys")) {
            if (kid.equals(keyNode.get("kid").asText())) {
                return parseRsaPublicKey(keyNode);
            }
        }

        throw new IllegalArgumentException("해당 kid에 맞는 Apple 공개키를 찾을 수 없습니다: " + kid);
    }

    private PublicKey parseRsaPublicKey(JsonNode keyNode) {
        try {
            String n = keyNode.get("n").asText();
            String e = keyNode.get("e").asText();

            byte[] nBytes = Base64.getUrlDecoder().decode(n);
            byte[] eBytes = Base64.getUrlDecoder().decode(e);

            BigInteger modulus = new BigInteger(1, nBytes);
            BigInteger exponent = new BigInteger(1, eBytes);

            RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(spec);
        } catch (Exception ex) {
            throw new RuntimeException("RSA 공개키 파싱에 실패했습니다.", ex);
        }
    }

    private static class CachedKey {
        final PublicKey key;
        final long createdAt;

        CachedKey(PublicKey key) {
            this.key = key;
            this.createdAt = System.currentTimeMillis();
        }

        boolean isExpired() {
            return (System.currentTimeMillis() - createdAt) > CACHE_EXPIRATION_MS;
        }
    }
}
