package com.fivlo.fivlo_backend.security;

import com.fivlo.fivlo_backend.domain.user.entity.User;
import com.fivlo.fivlo_backend.domain.user.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

/**
 * OAuth2 로그인 성공 핸들러
 * Google, Kakao 등 소셜 로그인 성공 시 JWT 토큰 생성 및 리다이렉션 처리
 * 실제 데이터베이스에서 사용자를 조회하거나 생성하는 로직 포함
 */
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2SuccessHandler.class);

    private final JwtTokenProvider jwtTokenProvider;

    private final UserRepository userRepository;

    // 프론트엔드 리다이렉션 URL (환경에 따라 설정 가능)
    private static final String FRONTEND_URL = "http://localhost:3000"; // React 앱 URL
    private static final String REDIRECT_PATH = "/auth/callback";

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                      HttpServletResponse response,
                                      Authentication authentication) throws IOException, ServletException {
        
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        logger.info("OAuth2 login success for user: {}", oauth2User.getName());

        try {
            // OAuth2 제공자별 사용자 정보 추출
            String provider = determineProvider(request);
            String email = extractEmail(oauth2User, provider);
            String nickname = extractNickname(oauth2User, provider);
            String profileImageUrl = extractProfileImageUrl(oauth2User, provider);
            String socialId = extractSocialId(oauth2User, provider);

            if (email == null || socialId == null) {
                logger.error("Required user information missing: email={}, socialId={}", email, socialId);
                response.sendRedirect(FRONTEND_URL + "/login?error=missing_user_info");
                return;
            }

            // 데이터베이스에서 사용자 조회 또는 생성
            User user = findOrCreateUser(email, socialId, provider, nickname, profileImageUrl);
            
            // JWT 토큰 생성
            String jwtToken = jwtTokenProvider.generateToken(user.getId());

            // 신규 사용자 여부 확인
            boolean isNewUser = user.getOnboardingType() == null;

            // 프론트엔드로 리다이렉션 (토큰과 사용자 정보 포함)
            String redirectUrl = UriComponentsBuilder.fromUriString(FRONTEND_URL + REDIRECT_PATH)
                    .queryParam("token", jwtToken)
                    .queryParam("userId", user.getId())
                    .queryParam("email", user.getEmail())
                    .queryParam("nickname", user.getNickname())
                    .queryParam("provider", provider)
                    .queryParam("isNewUser", isNewUser)
                    .queryParam("isPremium", user.getIsPremium())
                    .build().toUriString();

            logger.info("Redirecting user {} to frontend with JWT token", user.getId());
            response.sendRedirect(redirectUrl);

        } catch (Exception e) {
            logger.error("Error processing OAuth2 authentication success", e);
            response.sendRedirect(FRONTEND_URL + "/login?error=oauth2_processing_failed");
        }
    }

    /**
     * 사용자 조회 또는 생성
     */
    private User findOrCreateUser(String email, String socialId, String provider, String nickname, String profileImageUrl) {
        User.SocialProvider socialProvider = User.SocialProvider.valueOf(provider.toUpperCase());
        
        // 1. 소셜 ID로 먼저 조회
        Optional<User> userBySocial = userRepository.findBySocialIdAndSocialProvider(socialId, socialProvider);
        if (userBySocial.isPresent()) {
            logger.info("Found existing user by social ID: {}", userBySocial.get().getId());
            return userBySocial.get();
        }

        // 2. 이메일로 조회 (기존 계정이 있을 수 있음)
        Optional<User> userByEmail = userRepository.findByEmail(email);
        if (userByEmail.isPresent()) {
            // 기존 계정에 소셜 정보 연동
            User existingUser = userByEmail.get();
            logger.info("Linking social account to existing user: {}", existingUser.getId());
            // 여기서는 소셜 정보를 업데이트하는 로직이 필요하지만, User 엔티티에 해당 메서드가 없으므로 그대로 반환
            return existingUser;
        }

        // 3. 신규 사용자 생성
        User newUser = User.builder()
                .email(email)
                .socialId(socialId)
                .socialProvider(socialProvider)
                .nickname(nickname != null ? nickname : "사용자")
                .profileImageUrl(profileImageUrl)
                .isPremium(false)
                .totalCoins(0)
                .build();

        User savedUser = userRepository.save(newUser);
        logger.info("Created new user: {}", savedUser.getId());
        return savedUser;
    }

    /**
     * OAuth2 제공자 판별
     * @param request HTTP 요청
     * @return 제공자 이름 (google, kakao)
     */
    private String determineProvider(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        if (requestURI.contains("google")) {
            return "google";
        } else if (requestURI.contains("kakao")) {
            return "kakao";
        }
        return "unknown";
    }

    /**
     * OAuth2 사용자 정보에서 이메일 추출
     * @param oauth2User OAuth2 사용자 객체
     * @param provider 제공자 (google, kakao)
     * @return 사용자 이메일
     */
    private String extractEmail(OAuth2User oauth2User, String provider) {
        Map<String, Object> attributes = oauth2User.getAttributes();
        
        switch (provider) {
            case "google":
                return (String) attributes.get("email");
            case "kakao":
                Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
                return kakaoAccount != null ? (String) kakaoAccount.get("email") : null;
            default:
                return null;
        }
    }

    /**
     * OAuth2 사용자 정보에서 소셜 ID 추출
     * @param oauth2User OAuth2 사용자 객체
     * @param provider 제공자 (google, kakao)
     * @return 소셜 로그인 고유 ID
     */
    private String extractSocialId(OAuth2User oauth2User, String provider) {
        Map<String, Object> attributes = oauth2User.getAttributes();
        
        switch (provider) {
            case "google":
                return (String) attributes.get("sub"); // Google의 고유 ID
            case "kakao":
                Object id = attributes.get("id");
                return id != null ? String.valueOf(id) : null; // Kakao의 고유 ID
            default:
                return null;
        }
    }

    /**
     * OAuth2 사용자 정보에서 닉네임 추출
     * @param oauth2User OAuth2 사용자 객체
     * @param provider 제공자 (google, kakao)
     * @return 사용자 닉네임
     */
    private String extractNickname(OAuth2User oauth2User, String provider) {
        Map<String, Object> attributes = oauth2User.getAttributes();
        
        switch (provider) {
            case "google":
                return (String) attributes.get("name");
            case "kakao":
                Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
                return properties != null ? (String) properties.get("nickname") : null;
            default:
                return null;
        }
    }

    /**
     * OAuth2 사용자 정보에서 프로필 이미지 URL 추출
     * @param oauth2User OAuth2 사용자 객체
     * @param provider 제공자 (google, kakao)
     * @return 사용자 프로필 이미지 URL
     */
    private String extractProfileImageUrl(OAuth2User oauth2User, String provider) {
        Map<String, Object> attributes = oauth2User.getAttributes();
        
        switch (provider) {
            case "google":
                return (String) attributes.get("picture");
            case "kakao":
                Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
                return properties != null ? (String) properties.get("profile_image") : null;
            default:
                return null;
        }
    }
}
