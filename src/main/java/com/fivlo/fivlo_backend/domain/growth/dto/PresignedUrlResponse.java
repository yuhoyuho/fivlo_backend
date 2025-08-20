package com.fivlo.fivlo_backend.domain.growth.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * PreSigned URL 생성 응답 DTO
 * S3 파일 업로드를 위한 정보 반환
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PresignedUrlResponse {
    
    private String presignedUrl;      // 파일 업로드용 PreSigned URL
    private String s3ObjectUrl;      // 업로드 완료 후 파일 접근 URL
    private String s3ObjectKey;      // S3 객체 키
    private LocalDateTime expirationTime;  // URL 만료 시간
    
    @Builder
    public PresignedUrlResponse(String presignedUrl, String s3ObjectUrl, 
                               String s3ObjectKey, LocalDateTime expirationTime) {
        this.presignedUrl = presignedUrl;
        this.s3ObjectUrl = s3ObjectUrl;
        this.s3ObjectKey = s3ObjectKey;
        this.expirationTime = expirationTime;
    }
}
