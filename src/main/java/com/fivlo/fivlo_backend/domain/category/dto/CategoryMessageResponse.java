package com.fivlo.fivlo_backend.domain.category.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 공통 메시지 응답 DTO
 * API 14, 15: 카테고리 수정/삭제 응답용
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CategoryMessageResponse {
    
    private String message;
    
    @Builder
    public CategoryMessageResponse(String message) {
        this.message = message;
    }
    
    /**
     * 수정 성공 응답 생성
     */
    public static CategoryMessageResponse updateSuccess() {
        return CategoryMessageResponse.builder()
                .message("카테고리가 성공적으로 수정되었습니다.")
                .build();
    }
    
    /**
     * 삭제 성공 응답 생성
     */
    public static CategoryMessageResponse deleteSuccess() {
        return CategoryMessageResponse.builder()
                .message("카테고리가 성공적으로 삭제되었습니다.")
                .build();
    }
}
