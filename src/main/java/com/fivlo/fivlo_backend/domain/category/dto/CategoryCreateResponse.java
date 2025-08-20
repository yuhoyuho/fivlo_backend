package com.fivlo.fivlo_backend.domain.category.dto;

import com.fivlo.fivlo_backend.domain.category.entity.Category;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 카테고리 생성 응답 DTO
 * API 13: 새로운 카테고리 생성 응답용
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CategoryCreateResponse {
    
    private Long id;
    private String message;
    
    @Builder
    public CategoryCreateResponse(Long id, String message) {
        this.id = id;
        this.message = message;
    }
    
    /**
     * 카테고리 생성 성공 응답 생성
     */
    public static CategoryCreateResponse success(Category category) {
        return CategoryCreateResponse.builder()
                .id(category.getId())
                .message("카테고리가 성공적으로 생성되었습니다.")
                .build();
    }
}
