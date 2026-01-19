package com.fivlo.fivlo_backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fivlo.fivlo_backend.domain.user.auth.entity.RefreshEntity;
import com.fivlo.fivlo_backend.domain.user.auth.repository.RefreshRepository;
import com.fivlo.fivlo_backend.domain.user.dto.LoginResponse;
import com.fivlo.fivlo_backend.domain.user.entity.User;
import com.nimbusds.jose.util.StandardCharset;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;
    private final RefreshRepository refreshRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        // 사용자 정보 가져오기
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        // jwt access 토큰 생성
        String access = jwtTokenProvider.generateAccessToken(user.getId());

        // jwt refresh 토큰 생성
        String refresh = jwtTokenProvider.generateRefreshToken(user.getId());

        // Redis에 refresh 토큰 저장
        RefreshEntity redisToken = new RefreshEntity(user.getId(), refresh);
        refreshRepository.save(redisToken);

        // 신규 사용자 여부 확인 (온보딩 완료하지 않은 경우)
        boolean isNewUser = user.getOnboardingType() == null;

        // 응답 dto 생성
        LoginResponse dto = new LoginResponse(isNewUser, access, refresh, user.getId(), user.getOnboardingType(), user.getIsPremium());

        // HTTP 응답 설정 및 JSON 변환
        response.setStatus(HttpServletResponse.SC_OK); // 200 OK
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharset.UTF_8.name());

        // ObjectMapper를 활용하여 dto -> json 변환 후 응답 body에 추가
        response.getWriter().write(objectMapper.writeValueAsString(dto));
    }
}
