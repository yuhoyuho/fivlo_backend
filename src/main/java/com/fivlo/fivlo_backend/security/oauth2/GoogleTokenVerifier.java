package com.fivlo.fivlo_backend.security.oauth2;

import com.fivlo.fivlo_backend.domain.user.entity.User;
import com.fivlo.fivlo_backend.domain.user.repository.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Component
public class GoogleTokenVerifier implements OAuth2TokenVerifier {
    @Value("${google.allowed-client-ids}")
    private List<String> allowedClientIds;

    private final UserRepository userRepository;

    public GoogleTokenVerifier(UserRepository userRepository, @Value("${spring.security.oauth2.client.registration.google.client-id}") String allowedClientIds) {
        this.userRepository = userRepository;
        this.allowedClientIds = Arrays.asList(allowedClientIds.split(","));
    }

    @Override
    @Transactional
    public User verifyAndGetOrCreate(String token) {

        try {
            // Google ID 토큰 검증 - 다중 클라이언트 ID 지원
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(allowedClientIds)
                    .build();

            GoogleIdToken idToken = verifier.verify(token);
            if(idToken == null) {
                throw new IllegalArgumentException("유효하지 않은 Google ID 토큰입니다.");
            }
            GoogleIdToken.Payload payload = idToken.getPayload();

            // 토큰에서 사용자 정보 추출
            String email = payload.getEmail();
            String socialId = payload.getSubject();
            String nickname = (String) payload.get("name");

            return findOrCreateUser(email, socialId, nickname);

        } catch(Exception e) {
            throw new RuntimeException("Google 토큰 처리 중 문제가 발생했습니다.");
        }
    }

    private User findOrCreateUser(String email, String socialId, String nickname) {
        User.SocialProvider socialProvider = User.SocialProvider.GOOGLE;

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
                                    .status(User.Status.ACTIVE)
                                    .alarmStatus(true) //알람 상태 추가(기본값 true 설정)
                                    .isPremium(true)
                                    .build();
                            return userRepository.save(newUser);
                        }));
    }

    @Override
    public Boolean supports(String provider) {
        return "GOOGLE".equalsIgnoreCase(provider);
    }
}
