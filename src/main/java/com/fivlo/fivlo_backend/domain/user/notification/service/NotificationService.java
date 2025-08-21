package com.fivlo.fivlo_backend.domain.user.notification.service;

import com.fivlo.fivlo_backend.domain.reminder.entity.ForgettingPreventionReminder;
import com.fivlo.fivlo_backend.domain.reminder.repository.ReminderRepository;
import com.fivlo.fivlo_backend.domain.user.entity.User;
import com.fivlo.fivlo_backend.domain.user.notification.dto.NotificationDto;
import com.fivlo.fivlo_backend.domain.user.repository.UserRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final UserRepository userRepository;
    private final ReminderRepository reminderRepository;

    @Transactional
    public String updateFcmToken(Long userId, NotificationDto.FcmTokenRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다."));

        user.updateFcmToken(request.token());
        return "FCM 토큰이 업데이트 되었습니다.";
    }

    @Transactional(readOnly = true)
    public void checkLocationAndSendReminder(Long userId, NotificationDto.LocationUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다."));

        if(!user.getIsPremium()) {
            return;
        }

        // 사용자의 위치 기반 알림 조회
        List<ForgettingPreventionReminder> locationReminders = reminderRepository.findByUserAndLocationNameIsNotNull(user);

        for (ForgettingPreventionReminder reminder : locationReminders) {
            double distance = calculateDistanceInMeters(
                    reminder.getLocationLatitude(),
                    reminder.getLocationLongitude(),
                    request.latitude(),
                    request.longitude()
            );

            if(distance > 100) {
                // TODO: 오늘 이미 이 알림을 보냈는지 확인하는 로직 추가 (중복 방지)
                log.info("사용자 {}가 설정 위치 '{}'에서 100m 이상 벗어남. 알림 전송.", userId, reminder.getLocationName());
                sendFcmNotification(user.getFcmToken(), reminder.getTitle(), "무언가 놓고 가신 건 없으신가요?");
            }
        }
    }

    private void sendFcmNotification(String deviceToken, String title, String body) {
        if (deviceToken == null || deviceToken.isEmpty()) {
            log.warn("FCM 토큰이 없어 알림을 보낼 수 없습니다.");
            return;
        }

        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();

        Message message = Message.builder()
                .setNotification(notification)
                .setToken(deviceToken) // 알림을 보낼 기기의 주소 (FCM 토큰)
                .build();

        try {
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("Successfully sent FCM message: {}", response);
        } catch (FirebaseMessagingException e) {
            log.error("Failed to send FCM message.", e);
        }
    }

    private double calculateDistanceInMeters(BigDecimal lat1, BigDecimal lon1, BigDecimal lat2, BigDecimal lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) return 0;

        final int R = 6371; // 지구 반지름 (km)
        double latDistance = Math.toRadians(lat2.doubleValue() - lat1.doubleValue());
        double lonDistance = Math.toRadians(lon2.doubleValue() - lon1.doubleValue());
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1.doubleValue())) * Math.cos(Math.toRadians(lat2.doubleValue()))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c * 1000; // meter로 변환
    }
}
