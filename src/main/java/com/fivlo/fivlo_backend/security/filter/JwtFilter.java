package com.fivlo.fivlo_backend.security.filter;

import com.fivlo.fivlo_backend.common.Routes;
import com.fivlo.fivlo_backend.security.CustomUserDetails;
import com.fivlo.fivlo_backend.security.CustomUserDetailsService;
import com.fivlo.fivlo_backend.security.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        if(request.getRequestURI().equals(Routes.AUTH_REFRESH)) {
            filterChain.doFilter(request, response);
            return;
        }

        String authorization = request.getHeader("Authorization");
        if(authorization == null) {
            filterChain.doFilter(request, response);
            return;
        }

        if(!authorization.startsWith("Bearer ")) {
            throw new ServletException("Invalid token");
        }

        // 토큰 파싱
        String token = authorization.split(" ")[1];

        // 토큰 유효성 검증
        if(!jwtTokenProvider.validateToken(token)) {
            throw new ServletException("Invalid token");
        }

        // 1. 토큰에서 사용자 id 추출
        Long userId = jwtTokenProvider.getUserIdFromToken(token);

        // 2. 사용자 조회
        CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserById(userId);

        // 3. 조회한 userDetails로 인증 객체 생성
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        // 4. SecurityContextHolder에 인증 정보 저장
        SecurityContextHolder.getContext().setAuthentication(auth);

        filterChain.doFilter(request, response);
    }
}
