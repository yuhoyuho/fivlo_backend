package com.fivlo.fivlo_backend.domain.user.dto;

import com.fivlo.fivlo_backend.domain.user.entity.User;

public record UserInfoResponse(Long id,
                               String nickname,
                               String profileImageUrl,
                               User.OnboardingType onboardingType,
                               Boolean isPremium,
                               Integer totalCoins) {
}
