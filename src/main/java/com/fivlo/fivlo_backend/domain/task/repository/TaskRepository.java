package com.fivlo.fivlo_backend.domain.task.repository;

import com.fivlo.fivlo_backend.domain.task.entity.Task;
import com.fivlo.fivlo_backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Task 리포지토리
 * 사용자의 일정 관리를 위한 데이터 접근 인터페이스
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    /**
     * 특정 사용자의 특정 날짜 Task 목록 조회
     * API 7: 특정 날짜의 Task 목록 조회용
     */
    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.category " +
           "WHERE t.user = :user AND t.dueDate = :date " +
           "ORDER BY t.createdAt ASC")
    List<Task> findByUserAndDueDateWithCategory(@Param("user") User user, @Param("date") LocalDate date);

    /**
     * 특정 사용자의 특정 Task 조회
     */
    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.category " +
           "WHERE t.user = :user AND t.id = :taskId")
    Optional<Task> findByUserAndIdWithCategory(@Param("user") User user, @Param("taskId") Long taskId);

    /**
     * 특정 사용자의 완료되지 않은 Task 개수 조회 (특정 날짜)
     */
    @Query("SELECT COUNT(t) FROM Task t WHERE t.user = :user AND t.dueDate = :date AND t.isCompleted = false")
    long countIncompleteTasksByUserAndDate(@Param("user") User user, @Param("date") LocalDate date);

    /**
     * 특정 사용자의 특정 날짜 Task 중 완료된 Task 개수 조회
     */
    @Query("SELECT COUNT(t) FROM Task t WHERE t.user = :user AND t.dueDate = :date AND t.isCompleted = true")
    long countCompletedTasksByUserAndDate(@Param("user") User user, @Param("date") LocalDate date);
}
