package com.fivlo.fivlo_backend.domain.task.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Task 완료 상태 변경 요청 DTO
 * API 10: Task 완료 상태 변경용
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TaskCompleteRequest {
    
    @NotNull(message = "완료 상태는 필수입니다")
    private Boolean isCompleted;
    
    @Builder
    public TaskCompleteRequest(Boolean isCompleted) {
        this.isCompleted = isCompleted;
    }
}
