package com.fivlo.fivlo_backend.domain.task.dto;

import com.fivlo.fivlo_backend.domain.category.dto.CategoryInfo;
import com.fivlo.fivlo_backend.domain.task.entity.Task;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Task 기본 정보 DTO
 * Task 응답에 공통으로 사용되는 기본 정보
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TaskInfo {
    
    private Long id;
    private String content;
    private LocalDate dueDate;
    private Boolean isCompleted;
    private String repeatType;
    private LocalDate endDate;
    private Boolean isLinkedToGrowthAlbum;
    private CategoryInfo category;
    
    @Builder
    public TaskInfo(Long id, String content, LocalDate dueDate, Boolean isCompleted, 
                   String repeatType, LocalDate endDate, Boolean isLinkedToGrowthAlbum,
                   CategoryInfo category) {
        this.id = id;
        this.content = content;
        this.dueDate = dueDate;
        this.isCompleted = isCompleted;
        this.repeatType = repeatType;
        this.endDate = endDate;
        this.isLinkedToGrowthAlbum = isLinkedToGrowthAlbum;
        this.category = category;
    }
    
    /**
     * Entity를 DTO로 변환
     */
    public static TaskInfo from(Task task) {
        return TaskInfo.builder()
                .id(task.getId())
                .content(task.getContent())
                .dueDate(task.getDueDate())
                .isCompleted(task.getIsCompleted())
                .repeatType(task.getRepeatType().name())
                .endDate(task.getEndDate())
                .isLinkedToGrowthAlbum(task.getIsLinkedToGrowthAlbum())
                .category(task.getCategory() != null ? CategoryInfo.from(task.getCategory()) : null)
                .build();
    }
}
