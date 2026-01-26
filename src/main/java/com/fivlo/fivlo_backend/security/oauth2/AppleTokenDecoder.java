package com.fivlo.fivlo_backend.security.oauth2;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.PublicKey;
import java.util.Base64;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class AppleTokenDecoder {

    private static final String APPLE_ISSUER = "https://appleid.apple.com";

    private final ApplePublicKeyProvider applePublicKeyProvider;
    private final AppleProperties appleProperties;
    private final ObjectMapper objectMapper;

    /**
     * Apple ID 토큰을 검증하고 클레임을 반환합니다.
     * - JWT 서명 검증 (Apple 공개키 사용)
     * - issuer 검증 (https://appleid.apple.com)
     * - audience 검증 (client_id와 일치)
     * - expiration 검증 (만료 여부)
     */
    public Map<String, Object> verifyAndGetClaims(String idToken) {
        try {
            // 1. JWT 헤더에서 kid 추출
            String kid = extractKid(idToken);

            // 2. Apple 공개키 가져오기
            PublicKey publicKey = applePublicKeyProvider.getPublicKey(kid);

            // 3. JWT 서명 검증 및 클레임 파싱
            Claims claims = Jwts.parser()
                    .verifyWith(publicKey)
                    .requireIssuer(APPLE_ISSUER)
                    .requireAudience(appleProperties.getClientId())
                    .build()
                    .parseSignedClaims(idToken)
                    .getPayload();

            // 4. 추가 검증: sub 필드 존재 확인
            String sub = claims.getSubject();
            if (sub == null || sub.isBlank()) {
                throw new IllegalArgumentException("Apple ID 토큰에 sub 클레임이 없습니다.");
            }

            log.debug("Apple ID 토큰 검증 성공: sub={}", sub);

            return Map.of(
                    "sub", sub,
                    "email", claims.get("email", String.class),
                    "email_verified", claims.get("email_verified", Boolean.class)
            );

        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            throw new IllegalArgumentException("Apple ID 토큰이 만료되었습니다.", e);
        } catch (io.jsonwebtoken.security.SignatureException e) {
            throw new IllegalArgumentException("Apple ID 토큰 서명이 유효하지 않습니다.", e);
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            throw new IllegalArgumentException("Apple ID 토큰 형식이 올바르지 않습니다.", e);
        } catch (Exception e) {
            throw new IllegalArgumentException("Apple ID 토큰 검증에 실패했습니다: " + e.getMessage(), e);
        }
    }

    private String extractKid(String idToken) {
        try {
            String header = idToken.split("\\.")[0];
            String decodedHeader = new String(Base64.getUrlDecoder().decode(header));
            Map<String, String> headerMap = objectMapper.readValue(decodedHeader, Map.class);
            String kid = headerMap.get("kid");
            if (kid == null || kid.isBlank()) {
                throw new IllegalArgumentException("Apple ID 토큰 헤더에 kid가 없습니다.");
            }
            return kid;
        } catch (Exception e) {
            throw new IllegalArgumentException("Apple ID 토큰 헤더 파싱에 실패했습니다.", e);
        }
    }

    /**
     * @deprecated 이 메서드는 서명 검증을 하지 않습니다. verifyAndGetClaims()를 사용하세요.
     */
    @Deprecated
    public Map<String, String> getPayload(String idToken) {
        Map<String, Object> claims = verifyAndGetClaims(idToken);
        return Map.of(
                "sub", (String) claims.get("sub"),
                "email", claims.get("email") != null ? (String) claims.get("email") : ""
        );
    }
}
