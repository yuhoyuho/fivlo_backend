package com.fivlo.fivlo_backend.domain.user.dto;

public record SocialLoginRequest(String provider,
                                 String token) {
}
