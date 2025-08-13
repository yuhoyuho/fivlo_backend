package com.fivlo.fivlo_backend.domain.user.controller;

import com.fivlo.fivlo_backend.common.Routes;
import com.fivlo.fivlo_backend.domain.user.dto.JoinUserRequest;
import com.fivlo.fivlo_backend.domain.user.dto.JoinUserResponse;
import com.fivlo.fivlo_backend.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(Routes.API_BASE)
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 소셜 로그인
    @PostMapping(Routes.AUTH_SOCIAL_LOGIN)
    public ResponseEntity<JoinUserResponse> socialLogin() {
        return ResponseEntity.ok().build();
    }

    // 이메일 회원가입
    @PostMapping(Routes.AUTH_SIGNUP)
    public ResponseEntity<JoinUserResponse> signUp(
            @Valid @RequestBody JoinUserRequest dto) {

        return ResponseEntity.status(201).body(userService.join(dto));
    }
}
