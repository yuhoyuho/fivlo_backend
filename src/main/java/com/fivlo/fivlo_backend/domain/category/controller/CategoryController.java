package com.fivlo.fivlo_backend.domain.category.controller;

import com.fivlo.fivlo_backend.common.Routes;
import com.fivlo.fivlo_backend.domain.category.dto.*;
import com.fivlo.fivlo_backend.domain.category.service.CategoryService;
import com.fivlo.fivlo_backend.domain.user.entity.User;
import com.fivlo.fivlo_backend.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 카테고리 컨트롤러
 * 카테고리 관리 관련 API 엔드포인트 처리
 * API 12-15: 카테고리 CRUD 기능 제공
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * API 12: 카테고리 목록 조회
     * GET /api/v1/categories
     * 사용자의 모든 카테고리 목록을 조회합니다.
     */
    @GetMapping(Routes.CATEGORIES_BASE)
    public ResponseEntity<CategoryListResponse> getCategories(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        
        log.info("카테고리 목록 조회 요청 - userId: {}", userDetails.getUser().getId());
        
        User user = userDetails.getUser();
        CategoryListResponse response = categoryService.getCategories(user);
        
        log.info("카테고리 목록 조회 응답 완료 - userId: {}, 카테고리 수: {}", 
                userDetails.getUser().getId(), response.getCategories().size());
        
        return ResponseEntity.ok(response);
    }

    /**
     * API 13: 새로운 카테고리 생성
     * POST /api/v1/categories
     * 새로운 카테고리를 생성합니다.
     */
    @PostMapping(Routes.CATEGORIES_BASE)
    public ResponseEntity<CategoryCreateResponse> createCategory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CategoryCreateRequest request) {
        
        log.info("카테고리 생성 요청 - userId: {}, categoryName: {}", 
                userDetails.getUser().getId(), request.getName());
        
        User user = userDetails.getUser();
        CategoryCreateResponse response = categoryService.createCategory(user, request);
        
        log.info("카테고리 생성 응답 완료 - userId: {}, categoryId: {}", 
                userDetails.getUser().getId(), response.getId());
        
        return ResponseEntity.ok(response);
    }

    /**
     * API 14: 카테고리 수정
     * PATCH /api/v1/categories/{categoryId}
     * 기존 카테고리 정보를 수정합니다.
     */
    @PatchMapping(Routes.CATEGORIES_BY_ID)
    public ResponseEntity<CategoryMessageResponse> updateCategory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long categoryId,
            @Valid @RequestBody CategoryUpdateRequest request) {
        
        log.info("카테고리 수정 요청 - userId: {}, categoryId: {}", 
                userDetails.getUser().getId(), categoryId);
        
        User user = userDetails.getUser();
        CategoryMessageResponse response = categoryService.updateCategory(user, categoryId, request);
        
        log.info("카테고리 수정 응답 완료 - userId: {}, categoryId: {}", 
                userDetails.getUser().getId(), categoryId);
        
        return ResponseEntity.ok(response);
    }

    /**
     * API 15: 카테고리 삭제
     * DELETE /api/v1/categories/{categoryId}
     * 특정 카테고리를 삭제합니다.
     */
    @DeleteMapping(Routes.CATEGORIES_BY_ID)
    public ResponseEntity<CategoryMessageResponse> deleteCategory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long categoryId) {
        
        log.info("카테고리 삭제 요청 - userId: {}, categoryId: {}", 
                userDetails.getUser().getId(), categoryId);
        
        User user = userDetails.getUser();
        CategoryMessageResponse response = categoryService.deleteCategory(user, categoryId);
        
        log.info("카테고리 삭제 응답 완료 - userId: {}, categoryId: {}", 
                userDetails.getUser().getId(), categoryId);
        
        return ResponseEntity.ok(response);
    }
}

