package com.fivlo.fivlo_backend.security.oauth2;

import io.jsonwebtoken.Jwts;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

@Component
public class AppleClientSecretGenerator {

    // application.yml 설정 주입받을 클래스
    private final AppleProperties appleProperties;

    @Getter
    private final String clientId;

    public AppleClientSecretGenerator(AppleProperties appleProperties) {
        this.appleProperties = appleProperties;
        this.clientId = appleProperties.getClientId();
    }

    public String generate() {
        Date now = new Date();
        long expirationTime = 3600000; // 1h
        return Jwts.builder()
                .header()
                .add("kid", appleProperties.getKeyId())
                .add("alg", "ES256")
                .and()
                // Payload 설정 (Claims)
                .issuer(appleProperties.getTeamId())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationTime))
                .audience().add("https://appleid.apple.com")//오타 수정
                .and()
                .subject(appleProperties.getClientId())
                // Signature 설정
                .signWith(getPrivateKey())
                .compact();
    }

    private PrivateKey getPrivateKey() {
        try {

            String privateKeyString = appleProperties.getPrivateKey()
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");

            byte[] decodedKey = Base64.getDecoder().decode(privateKeyString);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedKey);
            KeyFactory keyFactory = KeyFactory.getInstance("EC");

            return keyFactory.generatePrivate(keySpec);

        } catch(Exception e) {
            throw new RuntimeException("Apple Private Key 생성에 실패했습니다.", e);
        }
    }
}
