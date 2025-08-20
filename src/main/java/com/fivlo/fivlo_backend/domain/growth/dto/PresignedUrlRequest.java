package com.fivlo.fivlo_backend.domain.growth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * PreSigned URL 생성 요청 DTO
 * 파일 업로드를 위한 S3 PreSigned URL 생성용
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PresignedUrlRequest {
    
    @NotBlank(message = "파일명은 필수입니다")
    private String fileName;
    
    @NotBlank(message = "파일 타입은 필수입니다")
    @Pattern(regexp = "^image/(jpeg|jpg|png|gif|webp)$", 
             message = "지원되는 이미지 형식: JPEG, PNG, GIF, WebP")
    private String contentType;
    
    @Builder
    public PresignedUrlRequest(String fileName, String contentType) {
        this.fileName = fileName;
        this.contentType = contentType;
    }
}
