package com.fivlo.fivlo_backend.domain.task.service;

import com.fivlo.fivlo_backend.domain.category.entity.Category;
import com.fivlo.fivlo_backend.domain.category.repository.CategoryRepository;
import com.fivlo.fivlo_backend.domain.task.dto.*;
import com.fivlo.fivlo_backend.domain.task.entity.Task;
import com.fivlo.fivlo_backend.domain.task.repository.TaskRepository;
import com.fivlo.fivlo_backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Task 서비스
 * Task 관리에 관한 비즈니스 로직 처리
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepository taskRepository;
    private final CategoryRepository categoryRepository;

    /**
     * API 7: 특정 날짜의 Task 목록 조회
     * 특정 날짜에 해당하는 모든 Task 목록을 조회합니다.
     */
    public TaskListResponse getTasksByDate(User user, LocalDate date) {
        log.info("특정 날짜 Task 목록 조회 시작 - userId: {}, date: {}", user.getId(), date);
        
        List<Task> tasks = taskRepository.findByUserAndDueDateWithCategory(user, date);
        
        log.info("특정 날짜 Task 목록 조회 완료 - userId: {}, date: {}, Task 수: {}", 
                user.getId(), date, tasks.size());
        
        return TaskListResponse.from(tasks);
    }

    /**
     * API 8: 새로운 Task 생성
     * 새로운 Task를 생성합니다.
     */
    @Transactional
    public TaskCreateResponse createTask(User user, TaskCreateRequest request) {
        log.info("Task 생성 시작 - userId: {}, content: {}, dueDate: {}", 
                user.getId(), request.getContent(), request.getDueDate());
        
        // 카테고리 확인 (선택 사항)
        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findByUserAndId(user, request.getCategoryId())
                    .orElseThrow(() -> {
                        log.warn("카테고리를 찾을 수 없음 - userId: {}, categoryId: {}", 
                                user.getId(), request.getCategoryId());
                        return new IllegalArgumentException("해당 카테고리를 찾을 수 없습니다.");
                    });
        }
        
        // Task 생성
        Task task = Task.builder()
                .user(user)
                .category(category)
                .content(request.getContent())
                .dueDate(request.getDueDate())
                .isCompleted(false)
                .repeatType(request.getRepeatTypeEnum())
                .endDate(request.getEndDate())
                .isLinkedToGrowthAlbum(request.getIsLinkedToGrowthAlbum())
                .build();
        
        Task savedTask = taskRepository.save(task);
        
        log.info("Task 생성 완료 - userId: {}, taskId: {}, content: {}", 
                user.getId(), savedTask.getId(), savedTask.getContent());
        
        return TaskCreateResponse.success(savedTask);
    }

    /**
     * API 9: Task 수정
     * 기존 Task의 내용을 수정합니다.
     */
    @Transactional
    public TaskMessageResponse updateTask(User user, Long taskId, TaskUpdateRequest request) {
        log.info("Task 수정 시작 - userId: {}, taskId: {}", user.getId(), taskId);
        
        // Task 존재 확인
        Task task = taskRepository.findByUserAndIdWithCategory(user, taskId)
                .orElseThrow(() -> {
                    log.warn("Task를 찾을 수 없음 - userId: {}, taskId: {}", user.getId(), taskId);
                    return new IllegalArgumentException("해당 Task를 찾을 수 없습니다.");
                });
        
        // 카테고리 변경 처리
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findByUserAndId(user, request.getCategoryId())
                    .orElseThrow(() -> {
                        log.warn("카테고리를 찾을 수 없음 - userId: {}, categoryId: {}", 
                                user.getId(), request.getCategoryId());
                        return new IllegalArgumentException("해당 카테고리를 찾을 수 없습니다.");
                    });
            task.updateCategory(category);
        }
        
        // Task 내용 수정
        if (request.getContent() != null) {
            task.updateContent(request.getContent());
        }
        
        // 반복 설정 수정
        if (request.getRepeatTypeEnum() != null) {
            task.updateRepeatSettings(request.getRepeatTypeEnum(), request.getEndDate());
        }
        
        // 성장앨범 연동 설정 수정
        if (request.getIsLinkedToGrowthAlbum() != null) {
            task.updateGrowthAlbumLink(request.getIsLinkedToGrowthAlbum());
        }
        
        log.info("Task 수정 완료 - userId: {}, taskId: {}", user.getId(), taskId);
        
        return TaskMessageResponse.updateSuccess(taskId);
    }

    /**
     * API 10: Task 완료 상태 변경
     * Task의 완료 상태(isCompleted)를 토글합니다.
     */
    @Transactional
    public TaskMessageResponse updateTaskCompletion(User user, Long taskId, TaskCompleteRequest request) {
        log.info("Task 완료 상태 변경 시작 - userId: {}, taskId: {}, isCompleted: {}", 
                user.getId(), taskId, request.getIsCompleted());
        
        // Task 존재 확인
        Task task = taskRepository.findByUserAndIdWithCategory(user, taskId)
                .orElseThrow(() -> {
                    log.warn("Task를 찾을 수 없음 - userId: {}, taskId: {}", user.getId(), taskId);
                    return new IllegalArgumentException("해당 Task를 찾을 수 없습니다.");
                });
        
        // 완료 상태 변경
        task.updateCompletionStatus(request.getIsCompleted());
        
        log.info("Task 완료 상태 변경 완료 - userId: {}, taskId: {}, isCompleted: {}", 
                user.getId(), taskId, request.getIsCompleted());
        
        return TaskMessageResponse.completeSuccess(taskId);
    }

    /**
     * API 11: Task 삭제
     * 특정 Task를 삭제합니다. 매일 반복되는 Task일 경우 미래 Task 삭제 여부를 선택할 수 있습니다.
     */
    @Transactional
    public TaskMessageResponse deleteTask(User user, Long taskId, TaskDeleteRequest request) {
        log.info("Task 삭제 시작 - userId: {}, taskId: {}, deleteFutureTasks: {}", 
                user.getId(), taskId, request.getDeleteFutureTasks());
        
        // Task 존재 확인
        Task task = taskRepository.findByUserAndIdWithCategory(user, taskId)
                .orElseThrow(() -> {
                    log.warn("Task를 찾을 수 없음 - userId: {}, taskId: {}", user.getId(), taskId);
                    return new IllegalArgumentException("해당 Task를 찾을 수 없습니다.");
                });
        
        // TODO: 매일 반복 Task의 미래 일정 삭제 처리는 향후 구현
        // 현재는 단일 Task 삭제만 처리
        
        taskRepository.delete(task);
        
        log.info("Task 삭제 완료 - userId: {}, taskId: {}", user.getId(), taskId);
        
        return TaskMessageResponse.deleteSuccess();
    }

    /**
     * 특정 날짜에 완료되지 않은 Task 개수 조회
     * 하루 Task 모두 완료 시 코인 지급 로직을 위한 헬퍼 메서드
     */
    public boolean areAllTasksCompletedForDate(User user, LocalDate date) {
        log.info("특정 날짜 Task 완료 상태 확인 - userId: {}, date: {}", user.getId(), date);
        
        long incompleteCount = taskRepository.countIncompleteTasksByUserAndDate(user, date);
        boolean allCompleted = incompleteCount == 0;
        
        log.info("특정 날짜 Task 완료 상태 확인 결과 - userId: {}, date: {}, 미완료 Task 수: {}, 모두 완료: {}", 
                user.getId(), date, incompleteCount, allCompleted);
        
        return allCompleted;
    }

    /**
     * 특정 날짜에 완료된 Task 개수 조회
     */
    public long getCompletedTasksCount(User user, LocalDate date) {
        return taskRepository.countCompletedTasksByUserAndDate(user, date);
    }
}

