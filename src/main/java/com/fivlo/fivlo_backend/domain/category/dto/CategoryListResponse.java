package com.fivlo.fivlo_backend.domain.category.dto;

import com.fivlo.fivlo_backend.domain.category.entity.Category;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 카테고리 응답 DTO 클래스
 * API 응답에 사용되는 카테고리 정보 전송 객체들
 */

/**
 * 카테고리 목록 조회 응답 DTO
 * API 12: 카테고리 목록 조회용
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CategoryListResponse {
    
    private List<CategoryInfo> categories;
    
    @Builder
    public CategoryListResponse(List<CategoryInfo> categories) {
        this.categories = categories;
    }
    
    /**
     * Entity 리스트를 DTO로 변환
     */
    public static CategoryListResponse from(List<Category> categories) {
        List<CategoryInfo> categoryInfos = categories.stream()
                .map(CategoryInfo::from)
                .collect(Collectors.toList());
        
        return CategoryListResponse.builder()
                .categories(categoryInfos)
                .build();
    }
}

