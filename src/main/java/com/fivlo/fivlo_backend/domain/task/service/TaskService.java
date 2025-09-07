package com.fivlo.fivlo_backend.domain.task.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fivlo.fivlo_backend.common.ai.GeminiService;
import com.fivlo.fivlo_backend.common.ai.dto.AITaskDto;
import com.fivlo.fivlo_backend.common.ai.dto.AddAITaskRequestDto;
import com.fivlo.fivlo_backend.common.ai.dto.GoalAnalysisRequestDto;
import com.fivlo.fivlo_backend.common.ai.dto.GoalAnalysisResponseDto;
import com.fivlo.fivlo_backend.domain.category.entity.Category;
import com.fivlo.fivlo_backend.domain.category.repository.CategoryRepository;
import com.fivlo.fivlo_backend.domain.task.dto.*;
import com.fivlo.fivlo_backend.domain.task.entity.Task;
import com.fivlo.fivlo_backend.domain.task.repository.TaskRepository;
import com.fivlo.fivlo_backend.domain.user.entity.User;
import com.fivlo.fivlo_backend.domain.user.repository.UserRepository;
import com.fivlo.fivlo_backend.domain.user.service.CoinTransactionService;

import com.fivlo.fivlo_backend.security.CustomUserDetails;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;

import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

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
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;
    private final CoinTransactionService coinTransactionService;

    private final UserRepository userRepository;

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
        
        // 반복 Task 삭제 로직 구현
        int deletedCount = 1; // 현재 Task 포함
        
        // 매일 반복 Task이고 미래 Task도 삭제하라는 요청인 경우
        if (task.getRepeatType() == Task.RepeatType.DAILY && 
            Boolean.TRUE.equals(request.getDeleteFutureTasks())) {
            
            log.info("매일 반복 Task의 미래 일정 삭제 시작 - userId: {}, taskId: {}, currentDueDate: {}", 
                     user.getId(), taskId, task.getDueDate());
            
            // 미래 반복 Task 조회
            List<Task> futureTasks = taskRepository.findFutureRepeatTasks(
                user, Task.RepeatType.DAILY, task.getDueDate());
            
            if (!futureTasks.isEmpty()) {
                log.info("삭제할 미래 반복 Task 개수: {}", futureTasks.size());
                
                // 미래 반복 Task 일괄 삭제
                taskRepository.deleteAll(futureTasks);
                deletedCount += futureTasks.size();
                
                log.info("미래 반복 Task 삭제 완료 - 삭제된 개수: {}", futureTasks.size());
            } else {
                log.info("삭제할 미래 반복 Task가 없음");
            }
        }
        
        // 현재 Task 삭제 (항상 실행)
        taskRepository.delete(task);
        
        log.info("Task 삭제 완료 - userId: {}, taskId: {}, 총 삭제된 Task 개수: {}", 
                 user.getId(), taskId, deletedCount);
        
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

    /**
     * API 16 / 사용자의 목표를 분석하고 AI가 추천하는 Task 목록을 반환
     */
    @Transactional(readOnly = true)
    public GoalAnalysisResponseDto analyzeAndRecommendTasks(GoalAnalysisRequestDto requestDto, String languageCode) {
        String jsonResponse = geminiService.analyzeGoalAndRecommendTasks(
                requestDto.getGoalContent(),
                requestDto.getGoalType(),
                requestDto.getStartDate(),
                requestDto.getEndDate(),
                languageCode  // 언어 코드 추가
        );
        log.info("AI 응답 (JSON 문자열, language={}): {}", languageCode, jsonResponse);

        try {
            return objectMapper.readValue(jsonResponse, GoalAnalysisResponseDto.class);
        } catch (JsonProcessingException e) {
            log.error("AI 응답 JSON 파싱 중 오류 발생", e);
            throw new RuntimeException("AI 응답을 처리하는 중 오류가 발생했습니다.", e);
        }
    }
    
    /**
     * API 16 / 기존 메서드 호환성 유지 (기본값: 한국어)
     */
    @Transactional(readOnly = true)
    public GoalAnalysisResponseDto analyzeAndRecommendTasks(GoalAnalysisRequestDto requestDto) {
        return analyzeAndRecommendTasks(requestDto, "ko");
    }
        

    /**
     * API 17 / AI가 추천한 Task 목록을 사용자의 Task 목록에 일괄 추가
     */
    @Transactional
    public String addAiRecommendedTasks(Long userId, AddAITaskRequestDto requestDto) {
        if (requestDto.getTasks() == null || requestDto.getTasks().isEmpty()) {
            throw new NoSuchElementException("Task를 찾을 수 없습니다.");
        }

        // 1. userId를 사용하여 DB에서 User 엔티티를 조회합니다.
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId));

        // 2. 조회된 user 객체를 사용하여 Task 엔티티 리스트를 생성합니다.
        List<Task> tasksToSave = requestDto.getTasks().stream()
                .map(aiTaskDto -> convertToTaskEntity(user, aiTaskDto))
                .collect(Collectors.toList());

        taskRepository.saveAll(tasksToSave);

        // 3. 로그를 수정하고, 명세에 맞는 메시지를 반환합니다.
        log.info("사용자 ID {}를 위해 {}개의 AI 추천 Task를 저장했습니다.", userId, tasksToSave.size());
        return "AI 추천 Task가 성공적으로 추가되었습니다.";
    }

    /**
     * AiTaskDto를 Task 엔티티로 변환하는 헬퍼 메서드
     */
    private Task convertToTaskEntity(User user, AITaskDto dto) {
        // 문자열 날짜를 LocalDate 객체로 변환 (null 체크 포함)
        LocalDate dueDate = (dto.getDueDate() != null) ? LocalDate.parse(dto.getDueDate()) : null;
        LocalDate endDate = (dto.getEndDate() != null) ? LocalDate.parse(dto.getEndDate()) : null;

        // 문자열 repeatType을 Enum으로 변환
        Task.RepeatType repeatType = Task.RepeatType.valueOf(dto.getRepeatType());

        return Task.builder()
                .user(user)
                .content(dto.getContent())
                .dueDate(dueDate)
                .repeatType(repeatType)
                .endDate(endDate)
                .isLinkedToGrowthAlbum(dto.getIsLinkedToGrowthAlbum())
                .isCompleted(false) // 기본값은 미완료
                .build();
    }

    /**
     * Task 완료 코인 지급
     * 프리미엄 사용자가 Task를 완료했을 때 코인을 지급합니다.
     */
    @Transactional
    public TaskCoinResponse earnTaskCoin(Long userId, TaskCoinRequest dto) {
        log.info("Task 코인 지급 요청 - userId: {}, taskId: {}", userId, dto.taskId());

        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다."));

        // 2. Task 조회 및 권한 확인
        Task task = taskRepository.findById(dto.taskId())
                .orElseThrow(() -> new NoSuchElementException("해당 Task를 찾을 수 없습니다."));

        if (!task.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("자신의 Task만 코인을 받을 수 있습니다.");
        }

        // 3. Task 완료 여부 확인
        if (!task.getIsCompleted()) {
            return new TaskCoinResponse(user.getTotalCoins(), "Task를 먼저 완료해주세요.");
        }

        // 4. 마지막 코인 지급일 확인 (일일 제한)
        LocalDate today = LocalDate.now();
        LocalDate lastCoinDate = user.getLastTaskCoinDate();

        if (lastCoinDate != null && lastCoinDate.isEqual(today)) {
            return new TaskCoinResponse(user.getTotalCoins(), "오늘은 이미 Task 코인을 지급받았습니다.");
        }

        // 5. 프리미엄 사용자 확인 및 코인 지급
        if (user.getIsPremium()) {
            user.addCoins(1);
            user.updateLastTaskCoinDate(today);
            coinTransactionService.logTransaction(user, 1);

            log.info("Task 코인 지급 완료 - userId: {}, taskId: {}, totalCoins: {}", userId, dto.taskId(), user.getTotalCoins());
            return new TaskCoinResponse(user.getTotalCoins(), "Task 완료! 오분이가 코인을 드려요 🎉");
        } else {
            return new TaskCoinResponse(user.getTotalCoins(), "프리미엄 회원만 코인을 받을 수 있습니다.");
        }
    }

}

