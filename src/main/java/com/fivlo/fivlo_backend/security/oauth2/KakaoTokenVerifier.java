package com.fivlo.fivlo_backend.security.oauth2;

import com.fivlo.fivlo_backend.domain.user.entity.User;
import com.fivlo.fivlo_backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class KakaoTokenVerifier implements OAuth2TokenVerifier {

    private final WebClient webClient; // 카카오 서버와 통신하기 위한 WebClient
    private final UserRepository userRepository;

    @Override
    public User verifyAndGetOrCreate(String token) {

        try {
            // 카카오 서버로 요청을 보내서 사용자 정보 가져오기
            Map<String, Object> attributes = webClient.get()
                    .uri("https://kapi.kakao.com/v2/user/me")
                    .headers(h -> h.setBearerAuth(token))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if(attributes == null) {
                throw new IllegalArgumentException("카카오 사용자 정보를 가져올 수 없습니다.");
            }

            // 사용자 정보 추출
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

            String socialId = String.valueOf(attributes.get("id"));
            String email = String.valueOf(kakaoAccount.get("email"));
            String nickname = String.valueOf(profile.get("nickname"));

            // 사용자 조회 또는 생성
            return findOrCreateUser(email, socialId, nickname);

        } catch(Exception e) {
            throw new RuntimeException("카카오 토큰 처리 중 문제가 발생했습니다.", e);
        }
    }

    @Override
    public Boolean supports(String provider) {
        return "KAKAO".equalsIgnoreCase(provider);
    }

    private User findOrCreateUser(String email, String socialId, String nickname) {
        User.SocialProvider socialProvider = User.SocialProvider.KAKAO;

        return userRepository.findBySocialIdAndSocialProvider(socialId, socialProvider)
                .orElseGet(() -> userRepository.findByEmail(email)
                        .map(existUser -> {
                            existUser.linkSocialAccount(socialId, socialProvider);
                            return existUser;
                        })
                        .orElseGet(() -> {
                            User newUser = User.builder()
                                    .email(email)
                                    .socialId(socialId)
                                    .socialProvider(socialProvider)
                                    .nickname(nickname)
                                    .build();
                            return userRepository.save(newUser);
                        }));
    }
}
