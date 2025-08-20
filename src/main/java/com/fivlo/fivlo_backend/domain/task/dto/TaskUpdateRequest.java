package com.fivlo.fivlo_backend.domain.task.dto;

import com.fivlo.fivlo_backend.domain.task.entity.Task;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Task 수정 요청 DTO
 * API 9: Task 수정용
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TaskUpdateRequest {
    
    private String content;
    
    private Long categoryId;
    
    private String repeatType;
    
    private LocalDate endDate;
    
    private Boolean isLinkedToGrowthAlbum;
    
    @Builder
    public TaskUpdateRequest(String content, Long categoryId, String repeatType, 
                           LocalDate endDate, Boolean isLinkedToGrowthAlbum) {
        this.content = content;
        this.categoryId = categoryId;
        this.repeatType = repeatType;
        this.endDate = endDate;
        this.isLinkedToGrowthAlbum = isLinkedToGrowthAlbum;
    }
    
    /**
     * RepeatType enum으로 변환
     */
    public Task.RepeatType getRepeatTypeEnum() {
        if (this.repeatType == null) {
            return null;
        }
        try {
            return Task.RepeatType.valueOf(this.repeatType);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
