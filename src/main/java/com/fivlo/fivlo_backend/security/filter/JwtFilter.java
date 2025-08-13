package com.fivlo.fivlo_backend.security.filter;

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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

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
//
//        // 만료일 검증 (위에서 true가 반환된다면 만료되지 않은 토큰임이 보장되지만 이중 방어를 위해 추가)
//        if(jwtTokenProvider.isTokenExpired(token)) {
//            throw new ServletException("Token expired");
//        }
//
//        String email = jwtTokenProvider.getUserEmailFromToken(token);
//        String role = "ROLE_USER";
//
//        List<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(role));
//
//        Authentication auth = new UsernamePasswordAuthenticationToken(email, null, authorities);

        // 4. SecurityContextHolder에 인증 정보 저장
        SecurityContextHolder.getContext().setAuthentication(auth);

        filterChain.doFilter(request, response);
    }
}
