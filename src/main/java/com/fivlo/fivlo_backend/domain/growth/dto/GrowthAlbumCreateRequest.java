package com.fivlo.fivlo_backend.domain.growth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 성장앨범 생성 요청 DTO
 * API 18: Task 완료 후 성장앨범 항목 생성용 (S3 URL 방식)
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GrowthAlbumCreateRequest {
    
    @NotBlank(message = "사진 URL은 필수입니다")
    private String photoUrl;
    
    private String memo;  // 선택사항
    
    @Builder
    public GrowthAlbumCreateRequest(String photoUrl, String memo) {
        this.photoUrl = photoUrl;
        this.memo = memo;
    }
}
