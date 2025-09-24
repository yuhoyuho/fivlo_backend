package com.fivlo.fivlo_backend.domain.user.controller;

import com.fivlo.fivlo_backend.common.Routes;
import com.fivlo.fivlo_backend.domain.user.auth.dto.ReissueRequestDto;
import com.fivlo.fivlo_backend.domain.user.auth.dto.TokenResponseDto;
import com.fivlo.fivlo_backend.domain.user.dto.*;
import com.fivlo.fivlo_backend.domain.user.entity.User;
import com.fivlo.fivlo_backend.domain.user.service.UserService;
import com.fivlo.fivlo_backend.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * HTTP 메서드: POST
     * 엔드포인트: /api/v1/auth/social-login
     */
    // 소셜 로그인
    @PostMapping(Routes.AUTH_SOCIAL_LOGIN)
    public ResponseEntity<SocialLoginResponse> socialLogin(
            @Valid @RequestBody SocialLoginRequest dto) {

        return ResponseEntity.status(200).body(userService.socialLogin(dto));
    }

    /**
     * HTTP 메서드: POST
     * 엔드포인트: /api/v1/auth/signup
     */
    // 이메일 회원가입
    @PostMapping(Routes.AUTH_SIGNUP)
    public ResponseEntity<JoinUserResponse> signUp(
            @Valid @RequestBody JoinUserRequest dto) {

        return ResponseEntity.status(201).body(userService.join(dto));
    }

    /**
     * HTTP 메서드: POST
     * 엔드포인트: /api/v1/users/onboarding
     */
    // 온보딩 목표 설정/수정
    @PostMapping(Routes.USERS_ONBOARDING)
    public ResponseEntity<User.OnboardingType> updateOnboardingType(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody OnboardingUpdateRequest dto) {

        return ResponseEntity.status(200).body(userService.updateOnboardingType(userDetails.getUser().getId(), dto.onboardingType()));
    }

    /**
     * HTTP 메서드 : POST
     * 엔드포인트 : /api/v1/users/languages
     */
    // 언어 설정
    @PostMapping(Routes.USERS_LANGUAGES)
    public ResponseEntity<User.Language> updateLanguage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody LanguageUpdateRequest dto) {

        return ResponseEntity.status(200).body(userService.updateLanguage(userDetails.getUser().getId(), dto.language()));
    }

    /**
     * HTTP 메서드 : POST
     * 엔드포인트 : /api/v1/users/alarms
     */
    // 알림 기능 On/Off
    @PostMapping(Routes.USERS_ALARM)
    public ResponseEntity<String> updateAlarmStatus(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        return ResponseEntity.status(200).body(userService.updateAlarmStatus(userDetails.getUser().getId()));
    }

    /**
     * HTTP 메서드: GET
     * 엔드포인트: /api/v1/users/me
     */
    // 사용자 프로필 정보 조회
    @GetMapping(Routes.USERS_ME)
    public ResponseEntity<UserInfoResponse> userInfo(@AuthenticationPrincipal CustomUserDetails userDetails) {

        return ResponseEntity.status(200).body(userService.getUserInfo(userDetails));
    }

    /**
     * HTTP 메서드: PATCH
     * 엔드포인트: /api/v1/users/me
     */
    // 사용자 프로필 정보 수정
    @PatchMapping(Routes.USERS_ME)
    public ResponseEntity<String> updateUserInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateUserRequest dto) {

        String message = userService.updateUserInfo(userDetails, dto);
        return ResponseEntity.status(200).body(message);
    }

    /**
     * HTTP 메서드 : DELETE
     * 엔드포인트 : /api/v1/users/delete
     */
    // 사용자 탈퇴
    @DeleteMapping(Routes.USERS_DELETE)
    public ResponseEntity<String> deleteUser(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        return ResponseEntity.ok(userService.deleteUser(userDetails.getUser().getId()));
    }

    /**
     * HTTP 메서드 : POST
     * 엔드포인트: /api/v1/users/attendance
     */
    // 출석 시 코인 지급
    @PostMapping(Routes.USERS_ATTENDANCE)
    public ResponseEntity<String> attendance(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        return ResponseEntity.ok(userService.checkAttendanceAndReward(userDetails.getUser().getId()));
    }

    /**
     * Refresh 토큰 재발급 API
     * HTTP : POST
     * EndPoint : /api/v1/auth/reissue
     */
    @PostMapping(Routes.AUTH_REFRESH)
    public ResponseEntity<TokenResponseDto> reissue(@Valid @RequestBody ReissueRequestDto dto) {
        return ResponseEntity.ok(userService.reissue(dto));
    }

    /**
     * HTTP : POST
     * EndPoint : /api/v1/auth/logout
     */
    // 사용자 로그아웃
    @PostMapping(Routes.AUTH_LOGOUT)
    public ResponseEntity<String> logout(@Valid @RequestBody LogoutRequest dto) {
        return ResponseEntity.ok(userService.logout(dto.refreshToken()));
    }
}
