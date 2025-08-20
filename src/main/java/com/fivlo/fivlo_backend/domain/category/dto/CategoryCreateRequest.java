package com.fivlo.fivlo_backend.domain.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 카테고리 생성 요청 DTO
 * API 13: 새로운 카테고리 생성용
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CategoryCreateRequest {
    
    @NotBlank(message = "카테고리 이름은 필수입니다")
    @Size(max = 100, message = "카테고리 이름은 100자를 넘을 수 없습니다")
    private String name;
    
    @NotBlank(message = "카테고리 색상은 필수입니다")
    @Size(max = 20, message = "색상 코드는 20자를 넘을 수 없습니다")
    private String color;
    
    @Builder
    public CategoryCreateRequest(String name, String color) {
        this.name = name;
        this.color = color;
    }
}
