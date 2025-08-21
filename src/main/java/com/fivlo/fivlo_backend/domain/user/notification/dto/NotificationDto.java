package com.fivlo.fivlo_backend.domain.user.notification.dto;

import java.math.BigDecimal;

public class NotificationDto {
    public record FcmTokenRequest(String token) {}
    public record LocationUpdateRequest(BigDecimal latitude, BigDecimal longitude) {}
}
