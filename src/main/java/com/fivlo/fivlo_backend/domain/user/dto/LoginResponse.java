package com.fivlo.fivlo_backend.domain.user.dto;

import com.fivlo.fivlo_backend.domain.user.entity.User;

public record LoginResponse(String token,
                            Long userId,
                            User.OnboardingType onboardingType,
                            Boolean isPremium) {
}
