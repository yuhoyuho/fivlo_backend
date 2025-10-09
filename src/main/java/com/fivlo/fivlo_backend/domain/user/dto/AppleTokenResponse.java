package com.fivlo.fivlo_backend.domain.user.dto;

public record AppleTokenResponse(
        String access_token,
        Long expires_in,
        String id_token,
        String refresh_token,
        String token_type
) {}
