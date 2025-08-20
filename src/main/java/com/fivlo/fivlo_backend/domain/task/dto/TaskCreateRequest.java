package com.fivlo.fivlo_backend.domain.task.dto;

import com.fivlo.fivlo_backend.domain.task.entity.Task;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Task 생성 요청 DTO
 * API 8: 새로운 Task 생성용
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TaskCreateRequest {
    
    @NotBlank(message = "Task 내용은 필수입니다")
    private String content;
    
    @NotNull(message = "수행 예정일은 필수입니다")
    private LocalDate dueDate;
    
    private Long categoryId;
    
    private String repeatType = "NONE"; // 기본값: 반복 없음
    
    private LocalDate endDate;
    
    private Boolean isLinkedToGrowthAlbum = false; // 기본값: 연동 안함
    
    @Builder
    public TaskCreateRequest(String content, LocalDate dueDate, Long categoryId, 
                           String repeatType, LocalDate endDate, Boolean isLinkedToGrowthAlbum) {
        this.content = content;
        this.dueDate = dueDate;
        this.categoryId = categoryId;
        this.repeatType = repeatType != null ? repeatType : "NONE";
        this.endDate = endDate;
        this.isLinkedToGrowthAlbum = isLinkedToGrowthAlbum != null ? isLinkedToGrowthAlbum : false;
    }
    
    /**
     * RepeatType enum으로 변환
     */
    public Task.RepeatType getRepeatTypeEnum() {
        try {
            return Task.RepeatType.valueOf(this.repeatType);
        } catch (IllegalArgumentException e) {
            return Task.RepeatType.NONE;
        }
    }
}
