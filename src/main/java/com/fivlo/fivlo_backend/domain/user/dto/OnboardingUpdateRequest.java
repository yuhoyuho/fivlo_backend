package com.fivlo.fivlo_backend.domain.user.dto;

import com.fivlo.fivlo_backend.domain.user.entity.User;
import jakarta.validation.constraints.NotNull;

public record OnboardingUpdateRequest(
        @NotNull(message = "온보딩 타입은 필수 항목입니다.")
        User.OnboardingType onboardingType) {
}
