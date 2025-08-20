package com.fivlo.fivlo_backend.domain.task.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Task 삭제 요청 DTO
 * API 11: Task 삭제용
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TaskDeleteRequest {
    
    private Boolean deleteFutureTasks = false; // 기본값: 미래 Task 삭제 안함
    
    @Builder
    public TaskDeleteRequest(Boolean deleteFutureTasks) {
        this.deleteFutureTasks = deleteFutureTasks != null ? deleteFutureTasks : false;
    }
}
