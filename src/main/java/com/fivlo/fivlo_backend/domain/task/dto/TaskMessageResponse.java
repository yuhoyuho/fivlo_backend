package com.fivlo.fivlo_backend.domain.task.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 공통 메시지 응답 DTO
 * API 9, 10, 11: Task 수정/완료/삭제 응답용
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TaskMessageResponse {
    
    private Long id;
    private String message;
    
    @Builder
    public TaskMessageResponse(Long id, String message) {
        this.id = id;
        this.message = message;
    }
    
    /**
     * 수정 성공 응답 생성
     */
    public static TaskMessageResponse updateSuccess(Long taskId) {
        return TaskMessageResponse.builder()
                .id(taskId)
                .message("Task가 성공적으로 수정되었습니다.")
                .build();
    }
    
    /**
     * 완료 상태 변경 성공 응답 생성
     */
    public static TaskMessageResponse completeSuccess(Long taskId) {
        return TaskMessageResponse.builder()
                .id(taskId)
                .message("Task 완료 상태가 업데이트되었습니다.")
                .build();
    }
    
    /**
     * 삭제 성공 응답 생성
     */
    public static TaskMessageResponse deleteSuccess() {
        return TaskMessageResponse.builder()
                .message("Task가 성공적으로 삭제되었습니다.")
                .build();
    }
}
