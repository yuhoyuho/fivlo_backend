package com.fivlo.fivlo_backend.security.oauth2;

import com.fivlo.fivlo_backend.domain.user.dto.AppleTokenResponse;
import com.fivlo.fivlo_backend.domain.user.entity.User;
import com.fivlo.fivlo_backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
@RequiredArgsConstructor
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

        // 2. Apple 서버에 토큰 요청
        AppleTokenResponse tokenResponse = requestAppleToken(clientId, clientSecret, token);

        // 3. id_token에서 사용자 정보 추출
        Map<String, String> userInfo = appleTokenDecoder.getPayload(tokenResponse.id_token());
        String socialId = userInfo.get("sub");
        String email = userInfo.get("email");

        // 4. 사용자 조회 또는 생성
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
                .orElseGet(() -> userRepository.findByEmail(email)
                        .map(existUser -> {
                            existUser.linkSocialAccount(socialId, socialProvider);
                            return existUser;
                        })
                        .orElseGet(() -> {
                            // Apple은 최초 로그인 시에만 이름 정보를 주므로, 초기 닉네임은 임의로 설정합니다.
                            User newUser = User.builder()
                                    .email(email)
                                    .socialId(socialId)
                                    .socialProvider(socialProvider)
                                    .nickname("사용자_" + socialId.substring(0, 5))
                                    .status(User.Status.ACTIVE)
                                    .isPremium(true) // 정책에 맞게 수정
                                    .build();
                            return userRepository.save(newUser);
                        }));
    }
}
