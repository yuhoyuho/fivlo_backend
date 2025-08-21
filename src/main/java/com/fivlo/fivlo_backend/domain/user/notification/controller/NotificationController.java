package com.fivlo.fivlo_backend.domain.user.notification.controller;

import com.fivlo.fivlo_backend.domain.user.notification.dto.NotificationDto;
import com.fivlo.fivlo_backend.domain.user.notification.service.NotificationService;
import com.fivlo.fivlo_backend.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // FCM 토큰 등록 및 업데이트 API
    @PostMapping("/token")
    public ResponseEntity<String> updateFcmToken(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody NotificationDto.FcmTokenRequest request) {

        return ResponseEntity.ok(notificationService.updateFcmToken(userDetails.getUser().getId(), request));
    }

    // 위치정보 업데이트 및 알림 확인 API
    @PostMapping("/location")
    public ResponseEntity<Void> checkLocation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody NotificationDto.LocationUpdateRequest request) {

        notificationService.checkLocationAndSendReminder(userDetails.getUser().getId(), request);
        return ResponseEntity.ok().build();
    }
}
