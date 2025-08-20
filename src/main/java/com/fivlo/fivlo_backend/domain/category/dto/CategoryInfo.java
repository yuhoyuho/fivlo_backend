package com.fivlo.fivlo_backend.domain.category.dto;

import com.fivlo.fivlo_backend.domain.category.entity.Category;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 카테고리 기본 정보 DTO
 * 카테고리 응답에 공통으로 사용되는 기본 정보
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CategoryInfo {
    
    private Long id;
    private String name;
    private String color;
    
    @Builder
    public CategoryInfo(Long id, String name, String color) {
        this.id = id;
        this.name = name;
        this.color = color;
    }
    
    /**
     * Entity를 DTO로 변환
     */
    public static CategoryInfo from(Category category) {
        return CategoryInfo.builder()
                .id(category.getId())
                .name(category.getName())
                .color(category.getColor())
                .build();
    }
}
