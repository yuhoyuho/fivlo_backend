package com.fivlo.fivlo_backend.domain.task.dto;

import com.fivlo.fivlo_backend.domain.task.entity.Task;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Task 생성 응답 DTO
 * API 8: 새로운 Task 생성 응답용
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TaskCreateResponse {
    
    private Long id;
    private String message;
    
    @Builder
    public TaskCreateResponse(Long id, String message) {
        this.id = id;
        this.message = message;
    }
    
    /**
     * Task 생성 성공 응답 생성
     */
    public static TaskCreateResponse success(Task task) {
        return TaskCreateResponse.builder()
                .id(task.getId())
                .message("Task가 성공적으로 생성되었습니다.")
                .build();
    }
}
