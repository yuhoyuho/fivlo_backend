package com.fivlo.fivlo_backend.domain.user.controller;

import com.fivlo.fivlo_backend.common.Routes;
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
@RequestMapping(Routes.API_BASE)
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 이메일 회원가입
    @PostMapping(Routes.AUTH_SIGNUP)
    public ResponseEntity<JoinUserResponse> signUp(
            @Valid @RequestBody JoinUserRequest dto) {

        return ResponseEntity.status(201).body(userService.join(dto));
    }

    // 온보딩 목표 설정/수정
    @PostMapping(Routes.USERS_ONBOARDING)
    public ResponseEntity<User.OnboardingType> updateOnboardingType(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody OnboardingUpdateRequest dto) {

        String email = userDetails.getUsername();
        return ResponseEntity.status(200).body(userService.updateOnboardingType(email, dto.onboardingType()));
    }

    // 사용자 프로필 정보 조회
    @GetMapping(Routes.USERS_ME)
    public ResponseEntity<UserInfoResponse> userInfo(@AuthenticationPrincipal CustomUserDetails userDetails) {

        String email = userDetails.getUsername();
        return ResponseEntity.status(200).body(userService.getUserInfo(email));
    }

    // 사용자 프로필 정보 수정
    @PatchMapping(Routes.USERS_ME)
    public ResponseEntity<String> updateUserInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateUserRequest dto) {

        String email = userDetails.getUsername();
        String message = userService.updateUserInfo(email, dto);
        return ResponseEntity.status(200).body(message);
    }
}
