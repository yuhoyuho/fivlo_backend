package com.fivlo.fivlo_backend.security;

import com.fivlo.fivlo_backend.config.JwtConfig;
import com.fivlo.fivlo_backend.domain.user.auth.dto.TokenResponseDto;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {

    private final JwtConfig jwtConfig;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtConfig.getJwtSecret().getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    // 공통 파서 (0.12.x)
    private JwtParser getParser() {
        return Jwts.parser()
                .verifyWith(getSigningKey()) // setSigningKey 대체
                .build();
    }

    /** 사용자 ID로 JWT Access 토큰 생성 */
    public String generateAccessToken(Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtConfig.getJwtExpiration());

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey(), Jwts.SIG.HS256) // 0.12 스타일
                .compact();
    }

    public String generateRefreshToken(Long userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtConfig.getJwtRefreshExpiration());

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    /** Authentication으로 JWT 토큰 생성 */
    public String generateToken(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return generateAccessToken(userDetails.getUser().getId());
    }

    /** 토큰에서 사용자 ID 추출 */
    public Long getUserIdFromToken(String token) {
        Claims claims = getParser()
                .parseSignedClaims(token)
                .getPayload();
        return Long.valueOf(claims.getSubject());
    }

    /** 토큰에서 이메일 추출 */
    public String getUserEmailFromToken(String token) {
        Claims claims = getParser()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    /** 토큰에서 만료일 추출 */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = getParser()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getExpiration();
    }

    /** 토큰 유효성 검증 */
    public boolean validateToken(String token) {
        try {
            getParser().parseSignedClaims(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException ex) {
            log.error("Invalid JWT signature: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty: {}", ex.getMessage());
        }
        return false;
    }

    /** 토큰 만료 여부 확인 */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            log.error("Error checking token expiration: {}", e.getMessage());
            return true;
        }
    }

    public TokenResponseDto generateTokenDto(Long userId) {
        String access = generateAccessToken(userId);
        String refresh = generateRefreshToken(userId);

        return new TokenResponseDto(access, refresh);
    }
}
