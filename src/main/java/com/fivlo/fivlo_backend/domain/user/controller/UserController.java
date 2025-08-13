package com.fivlo.fivlo_backend.domain.user.controller;

import com.fivlo.fivlo_backend.common.Routes;
import com.fivlo.fivlo_backend.domain.user.dto.JoinUserRequest;
import com.fivlo.fivlo_backend.domain.user.dto.JoinUserResponse;
import com.fivlo.fivlo_backend.domain.user.dto.OnboardingUpdateRequest;
import com.fivlo.fivlo_backend.domain.user.entity.User;
import com.fivlo.fivlo_backend.domain.user.service.UserService;
import com.fivlo.fivlo_backend.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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


}
