package com.fivlo.fivlo_backend.domain.task.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Task 완료 코인 지급 요청 DTO
 */
public record TaskCoinRequest(
        @NotNull(message = "Task ID는 필수입니다.")
        Long taskId
) {
}
