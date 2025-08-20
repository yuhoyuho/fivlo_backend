package com.fivlo.fivlo_backend.domain.category.service;

import com.fivlo.fivlo_backend.domain.category.dto.*;
import com.fivlo.fivlo_backend.domain.category.entity.Category;
import com.fivlo.fivlo_backend.domain.category.repository.CategoryRepository;
import com.fivlo.fivlo_backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 카테고리 서비스
 * 카테고리 관리에 관한 비즈니스 로직 처리
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    
    // 기본 카테고리 상수
    private static final String DEFAULT_CATEGORY_NAME = "일상";
    private static final String DEFAULT_CATEGORY_COLOR = "#8B8B8B"; // 회색

    /**
     * API 12: 카테고리 목록 조회
     * 특정 사용자의 모든 카테고리를 조회합니다.
     */
    public CategoryListResponse getCategories(User user) {
        log.info("사용자 카테고리 목록 조회 시작 - userId: {}", user.getId());
        
        List<Category> categories = categoryRepository.findByUserOrderByCreatedAtAsc(user);
        
        log.info("사용자 카테고리 목록 조회 완료 - userId: {}, 카테고리 수: {}", 
                user.getId(), categories.size());
        
        return CategoryListResponse.from(categories);
    }

    /**
     * API 13: 새로운 카테고리 생성
     * 카테고리명 중복 체크 후 새로운 카테고리를 생성합니다.
     */
    @Transactional
    public CategoryCreateResponse createCategory(User user, CategoryCreateRequest request) {
        log.info("카테고리 생성 시작 - userId: {}, categoryName: {}", user.getId(), request.getName());
        
        // 카테고리명 중복 체크
        if (categoryRepository.existsByUserAndName(user, request.getName())) {
            log.warn("카테고리명 중복 - userId: {}, categoryName: {}", user.getId(), request.getName());
            throw new IllegalArgumentException("이미 같은 이름의 카테고리가 존재합니다.");
        }
        
        // 카테고리 생성
        Category category = Category.builder()
                .user(user)
                .name(request.getName())
                .color(request.getColor())
                .build();
        
        Category savedCategory = categoryRepository.save(category);
        
        log.info("카테고리 생성 완료 - userId: {}, categoryId: {}, categoryName: {}", 
                user.getId(), savedCategory.getId(), savedCategory.getName());
        
        return CategoryCreateResponse.success(savedCategory);
    }

    /**
     * API 14: 카테고리 수정
     * 기존 카테고리 정보를 수정합니다.
     */
    @Transactional
    public CategoryMessageResponse updateCategory(User user, Long categoryId, CategoryUpdateRequest request) {
        log.info("카테고리 수정 시작 - userId: {}, categoryId: {}", user.getId(), categoryId);
        
        // 카테고리 존재 확인
        Category category = categoryRepository.findByUserAndId(user, categoryId)
                .orElseThrow(() -> {
                    log.warn("카테고리를 찾을 수 없음 - userId: {}, categoryId: {}", user.getId(), categoryId);
                    return new IllegalArgumentException("해당 카테고리를 찾을 수 없습니다.");
                });
        
        // 카테고리명 중복 체크 (이름이 변경되는 경우만)
        if (request.getName() != null && !request.getName().equals(category.getName())) {
            if (categoryRepository.existsByUserAndNameAndIdNot(user, request.getName(), categoryId)) {
                log.warn("카테고리명 중복 - userId: {}, categoryName: {}", user.getId(), request.getName());
                throw new IllegalArgumentException("이미 같은 이름의 카테고리가 존재합니다.");
            }
        }
        
        // 카테고리 정보 업데이트
        category.update(request.getName(), request.getColor());
        
        log.info("카테고리 수정 완료 - userId: {}, categoryId: {}", user.getId(), categoryId);
        
        return CategoryMessageResponse.updateSuccess();
    }

    /**
     * API 15: 카테고리 삭제
     * 특정 카테고리를 삭제합니다.
     */
    @Transactional
    public CategoryMessageResponse deleteCategory(User user, Long categoryId) {
        log.info("카테고리 삭제 시작 - userId: {}, categoryId: {}", user.getId(), categoryId);
        
        // 카테고리 존재 확인
        Category category = categoryRepository.findByUserAndId(user, categoryId)
                .orElseThrow(() -> {
                    log.warn("카테고리를 찾을 수 없음 - userId: {}, categoryId: {}", user.getId(), categoryId);
                    return new IllegalArgumentException("해당 카테고리를 찾을 수 없습니다.");
                });
        
        // 기본 카테고리 삭제 방지
        if (DEFAULT_CATEGORY_NAME.equals(category.getName())) {
            log.warn("기본 카테고리 삭제 시도 - userId: {}, categoryId: {}", user.getId(), categoryId);
            throw new IllegalArgumentException("기본 카테고리는 삭제할 수 없습니다.");
        }
        
        categoryRepository.delete(category);
        
        log.info("카테고리 삭제 완료 - userId: {}, categoryId: {}", user.getId(), categoryId);
        
        return CategoryMessageResponse.deleteSuccess();
    }

    /**
     * 사용자 가입 시 기본 카테고리 생성
     * 사용자가 처음 가입할 때 '일상' 카테고리를 자동으로 생성합니다.
     */
    @Transactional
    public void createDefaultCategory(User user) {
        log.info("기본 카테고리 생성 시작 - userId: {}", user.getId());
        
        Category defaultCategory = Category.builder()
                .user(user)
                .name(DEFAULT_CATEGORY_NAME)
                .color(DEFAULT_CATEGORY_COLOR)
                .build();
        
        categoryRepository.save(defaultCategory);
        
        log.info("기본 카테고리 생성 완료 - userId: {}, categoryName: {}", user.getId(), DEFAULT_CATEGORY_NAME);
    }
}

