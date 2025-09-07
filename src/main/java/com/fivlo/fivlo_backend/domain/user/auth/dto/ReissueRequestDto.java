package com.fivlo.fivlo_backend.domain.user.auth.dto;

public record ReissueRequestDto(String accessToken,
                                String refreshToken) {
}
