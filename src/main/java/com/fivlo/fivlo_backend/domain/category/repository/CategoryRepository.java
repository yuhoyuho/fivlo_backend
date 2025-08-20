package com.fivlo.fivlo_backend.domain.category.repository;

import com.fivlo.fivlo_backend.domain.category.entity.Category;
import com.fivlo.fivlo_backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 카테고리 리포지토리
 * 사용자별 카테고리 관리를 위한 데이터 접근 인터페이스
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * 특정 사용자의 모든 카테고리 조회 (생성일순 정렬)
     */
    @Query("SELECT c FROM Category c WHERE c.user = :user ORDER BY c.createdAt ASC")
    List<Category> findByUserOrderByCreatedAtAsc(@Param("user") User user);

    /**
     * 특정 사용자의 특정 카테고리 조회
     */
    @Query("SELECT c FROM Category c WHERE c.user = :user AND c.id = :categoryId")
    Optional<Category> findByUserAndId(@Param("user") User user, @Param("categoryId") Long categoryId);

    /**
     * 특정 사용자의 카테고리명 중복 체크
     */
    @Query("SELECT COUNT(c) > 0 FROM Category c WHERE c.user = :user AND c.name = :name")
    boolean existsByUserAndName(@Param("user") User user, @Param("name") String name);

    /**
     * 특정 사용자의 카테고리명 중복 체크 (수정 시 - 자기 자신 제외)
     */
    @Query("SELECT COUNT(c) > 0 FROM Category c WHERE c.user = :user AND c.name = :name AND c.id != :categoryId")
    boolean existsByUserAndNameAndIdNot(@Param("user") User user, @Param("name") String name, @Param("categoryId") Long categoryId);
}

