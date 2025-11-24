package com.fivlo.fivlo_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * JWT 관련 설정 클래스
 * JWT 토큰 생성 및 검증에 필요한 설정값들과 Bean들을 관리
 */
@Configuration
public class JwtConfig {

    // application.properties에서 JWT 설정값 주입
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    @Value("${jwt.refresh-expiration}")
    private Long jwtRefreshExpiration;

    /**
     * JWT Secret Key 반환
     * 
     * @return JWT 서명에 사용할 비밀키
     */
    public String getJwtSecret() {
        return jwtSecret;
    }

    /**
     * JWT 토큰 만료시간 반환
     * 
     * @return JWT 토큰 만료시간 (밀리초)
     */
    public Long getJwtExpiration() {
        return jwtExpiration;
    }

    /**
     * JWT Refresh 토큰 만료시간 반환
     */
    public Long getJwtRefreshExpiration() {
        return jwtRefreshExpiration;
    }

    /**
     * JWT 토큰 만료시간을 초 단위로 반환
     * 
     * @return JWT 토큰 만료시간 (초)
     */
    public Long getJwtExpirationInSeconds() {
        return jwtExpiration / 1000;
    }

    /**
     * JWT 토큰 만료시간을 분 단위로 반환
     * 
     * @return JWT 토큰 만료시간 (분)
     */
    public Long getJwtExpirationInMinutes() {
        return jwtExpiration / (1000 * 60);
    }

    /**
     * JWT 토큰 만료시간을 시간 단위로 반환
     * 
     * @return JWT 토큰 만료시간 (시간)
     */
    public Long getJwtExpirationInHours() {
        return jwtExpiration / (1000 * 60 * 60);
    }
}
