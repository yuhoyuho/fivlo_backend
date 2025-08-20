package com.fivlo.fivlo_backend.domain.category.dto;

import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 카테고리 수정 요청 DTO
 * API 14: 카테고리 수정용
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CategoryUpdateRequest {
    
    @Size(max = 100, message = "카테고리 이름은 100자를 넘을 수 없습니다")
    private String name;
    
    @Size(max = 20, message = "색상 코드는 20자를 넘을 수 없습니다")
    private String color;
    
    @Builder
    public CategoryUpdateRequest(String name, String color) {
        this.name = name;
        this.color = color;
    }
}
