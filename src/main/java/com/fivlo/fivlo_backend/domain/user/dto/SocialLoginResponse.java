package com.fivlo.fivlo_backend.domain.user.dto;

import com.fivlo.fivlo_backend.domain.user.entity.User;

public record SocialLoginResponse(Boolean isNewUser,
                                  String accessToken,
                                  String refreshToken,
                                  Long userId,
                                  User.OnboardingType onboardingType,
                                  Boolean isPremium) {
}
