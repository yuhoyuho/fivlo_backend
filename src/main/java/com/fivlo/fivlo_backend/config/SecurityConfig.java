package com.fivlo.fivlo_backend.config;

import com.fivlo.fivlo_backend.security.CustomUserDetailsService;
import com.fivlo.fivlo_backend.security.JwtTokenProvider;
import com.fivlo.fivlo_backend.security.LoginSuccessHandler;
import com.fivlo.fivlo_backend.security.OAuth2SuccessHandler;
import com.fivlo.fivlo_backend.security.filter.JwtFilter;
import com.fivlo.fivlo_backend.security.filter.LoginFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationConfiguration configuration;
    private final LoginSuccessHandler loginSuccessHandler;
    private final CustomUserDetailsService customUserDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF 비활성화 (JWT 사용으로 인해)
            .csrf(AbstractHttpConfigurer::disable)
            
            // CORS 설정
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 세션 관리 설정 (JWT 사용으로 STATELESS)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // API 경로별 보안 설정
            .authorizeHttpRequests(authz -> authz
                // 인증 없이 접근 가능한 경로들
                .requestMatchers("/").permitAll()
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/oauth2/**").permitAll()
                .requestMatchers("/api/v1/auth/social-login").permitAll()
                .requestMatchers("/api/v1/auth/signup").permitAll()
                .requestMatchers("/api/v1/auth/signin").permitAll()
                .requestMatchers("/api/v1/auth/reissue").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                
                // 나머지 모든 요청은 인증 필요
                .anyRequest().authenticated()
            );
            
//            // OAuth2 로그인 설정
//            .oauth2Login(oauth2 -> oauth2
//                .successHandler(oAuth2SuccessHandler)
//                .failureUrl("/login?error=true")
//            );

        http.formLogin(auth -> auth.disable());
        http.httpBasic(auth -> auth.disable());


        // 커스텀 필터 등록
        http.addFilterBefore(new JwtFilter(jwtTokenProvider, customUserDetailsService), LogoutFilter.class);

        http.addFilterAt(new LoginFilter(authenticationManager(configuration), loginSuccessHandler), UsernamePasswordAuthenticationFilter.class);


        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 허용할 Origin (프론트엔드 URL)
        configuration.setAllowedOriginPatterns(Arrays.asList(
                // 웹 프론트엔드
                "http://localhost:3000",
                // 실제 도메인
                "https://fivlo.net",
                //expo 주소
                "exp://*:8081",              // ← 모든 IP 허용
                "exp://*:19000",             // ← Expo Metro bundler 포트들
                "exp://*:19001",
                "exp://*:19006",
                "http://192.168.*:*",        // ← 로컬 네트워크 대역
                "http://10.*:*",
                "http://172.16.*:*",
                // 안드로이드 시뮬레이터
                "http://localhost",
                // iOS 시뮬레이터 (Capacitor Framework)
                "capacitor://localhost",
                // iOS 시뮬레이터 (Ionic Framework)
                "ionic://localhost"
        ));

        // 허용할 HTTP 메서드
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        
        // 허용할 헤더
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // 인증 정보 포함 허용
        configuration.setAllowCredentials(true);
        
        // 노출할 헤더 (JWT 토큰 등)
        configuration.setExposedHeaders(Arrays.asList("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
