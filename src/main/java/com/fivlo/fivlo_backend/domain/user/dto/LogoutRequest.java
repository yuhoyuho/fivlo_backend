package com.fivlo.fivlo_backend.domain.user.dto;

public record LogoutRequest(
        String refreshToken
) {
}
