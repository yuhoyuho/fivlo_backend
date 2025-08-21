package com.fivlo.fivlo_backend.domain.task.dto;

/**
 * Task 완료 코인 지급 응답 DTO
 */
public record TaskCoinResponse(
        Integer totalCoins,
        String message
) {
}
