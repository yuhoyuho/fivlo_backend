package com.fivlo.fivlo_backend.domain.task.dto;

import com.fivlo.fivlo_backend.domain.task.entity.Task;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Task 목록 조회 응답 DTO
 * API 7: 특정 날짜의 Task 목록 조회용
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TaskListResponse {
    
    private List<TaskInfo> tasks;
    
    @Builder
    public TaskListResponse(List<TaskInfo> tasks) {
        this.tasks = tasks;
    }
    
    /**
     * Entity 리스트를 DTO로 변환
     */
    public static TaskListResponse from(List<Task> tasks) {
        List<TaskInfo> taskInfos = tasks.stream()
                .map(TaskInfo::from)
                .collect(Collectors.toList());
        
        return TaskListResponse.builder()
                .tasks(taskInfos)
                .build();
    }
}
