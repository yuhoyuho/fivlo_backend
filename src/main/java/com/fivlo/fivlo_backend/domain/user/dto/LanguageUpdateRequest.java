package com.fivlo.fivlo_backend.domain.user.dto;

import com.fivlo.fivlo_backend.domain.user.entity.User;
import jakarta.validation.constraints.NotNull;

public record LanguageUpdateRequest(
        @NotNull(message = "언어는 필수 항목입니다.")
        User.Language language
) {
}
