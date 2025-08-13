package com.fivlo.fivlo_backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        // 사용자 정보 가져오기
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        // jwt 토큰 생성
        String token = jwtTokenProvider.generateToken(user.getEmail());

        // 응답 dto 생성
        LoginResponse dto = new LoginResponse(token, user.getId(), user.getOnboardingType(), user.getIsPremium());

        // HTTP 응답 설정 및 JSON 변환
        response.setStatus(HttpServletResponse.SC_OK); // 200 OK
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharset.UTF_8.name());

        // ObjectMapper를 활용하여 dto -> json 변환 후 응답 body에 추가
        response.getWriter().write(objectMapper.writeValueAsString(dto));
    }
}
