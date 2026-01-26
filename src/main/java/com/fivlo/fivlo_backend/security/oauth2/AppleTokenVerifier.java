package com.fivlo.fivlo_backend.security.oauth2;

import com.fivlo.fivlo_backend.domain.user.dto.AppleTokenResponse;
import com.fivlo.fivlo_backend.domain.user.entity.User;
import com.fivlo.fivlo_backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class AppleTokenVerifier implements OAuth2TokenVerifier {

    private final UserRepository userRepository;
    private final AppleClientSecretGenerator appleClientSecretGenerator;
    private final AppleTokenDecoder appleTokenDecoder;
    private final WebClient webClient;

    @Override
    @Transactional
    public User verifyAndGetOrCreate(String token) {
        // 1. Client Secret 생성
        String clientSecret = appleClientSecretGenerator.generate();
        String clientId = appleClientSecretGenerator.getClientId();

        // 2. Apple 서버에 토큰 요청 (Authorization Code 교환)
        AppleTokenResponse tokenResponse = requestAppleToken(clientId, clientSecret, token);

        // 3. id_token 검증 및 사용자 정보 추출 (서명, iss, aud, exp 검증 포함)
        Map<String, Object> claims = appleTokenDecoder.verifyAndGetClaims(tokenResponse.id_token());
        String socialId = (String) claims.get("sub");
        String email = (String) claims.get("email");
        Boolean emailVerified = (Boolean) claims.get("email_verified");

        // 4. 이메일 검증 상태 확인 (선택적 - 로그만 남김)
        if (emailVerified != null && !emailVerified) {
            log.warn("Apple 로그인: 이메일이 미인증 상태입니다. socialId={}, email={}", socialId, email);
        }

        // 5. 사용자 조회 또는 생성
        return findOrCreateUser(email, socialId);
    }

    @Override
    public Boolean supports(String provider) {
        return "APPLE".equalsIgnoreCase(provider);
    }

    private AppleTokenResponse requestAppleToken(String clientId, String clientSecret, String code) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("code", code);
        formData.add("grant_type", "authorization_code");

        return webClient.post()
                .uri("https://appleid.apple.com/auth/token")
                .bodyValue(formData)
                .retrieve()
                .bodyToMono(AppleTokenResponse.class)
                .block();
    }

    private User findOrCreateUser(String email, String socialId) {
        User.SocialProvider socialProvider = User.SocialProvider.APPLE;

        return userRepository.findBySocialIdAndSocialProvider(socialId, socialProvider)
                .orElseGet(() -> {
                    // socialId로 찾지 못한 경우

                    // email이 null이거나 빈 문자열인 경우 (Apple이 이메일을 제공하지 않은 경우)
                    if (email == null || email.isBlank()) {
                        log.info("Apple 로그인: 이메일 없이 신규 사용자 생성. socialId={}", socialId);
                        return createNewUser(null, socialId, socialProvider);
                    }

                    // email로 기존 사용자 찾기
                    return userRepository.findByEmail(email)
                            .map(existUser -> {
                                existUser.linkSocialAccount(socialId, socialProvider);
                                log.info("Apple 로그인: 기존 사용자와 소셜 계정 연결. userId={}, socialId={}", existUser.getId(), socialId);
                                return existUser;
                            })
                            .orElseGet(() -> createNewUser(email, socialId, socialProvider));
                });
    }

    private User createNewUser(String email, String socialId, User.SocialProvider socialProvider) {
        User newUser = User.builder()
                .email(email)
                .socialId(socialId)
                .socialProvider(socialProvider)
                .nickname("사용자_" + socialId.substring(0, Math.min(5, socialId.length())))
                .alarmStatus(true)
                .status(User.Status.ACTIVE)
                .isPremium(true) // 정책에 맞게 수정
                .build();

        User savedUser = userRepository.save(newUser);
        log.info("Apple 로그인: 신규 사용자 생성. userId={}, email={}", savedUser.getId(), email);
        return savedUser;
    }
}
