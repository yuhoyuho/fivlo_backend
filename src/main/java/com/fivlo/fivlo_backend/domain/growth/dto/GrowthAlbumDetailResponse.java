package com.fivlo.fivlo_backend.domain.growth.dto;

import com.fivlo.fivlo_backend.domain.growth.entity.GrowthAlbum;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 성장앨범 상세 조회 응답 DTO
 * API 21: 특정 성장앨범 항목 상세 조회용
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GrowthAlbumDetailResponse {
    
    private Long id;
    private String photoUrl;
    private String memo;
    private LocalDateTime createdAt;
    
    @Builder
    public GrowthAlbumDetailResponse(Long id, String photoUrl, String memo, LocalDateTime createdAt) {
        this.id = id;
        this.photoUrl = photoUrl;
        this.memo = memo;
        this.createdAt = createdAt;
    }
    
    /**
     * Entity를 DTO로 변환
     */
    public static GrowthAlbumDetailResponse from(GrowthAlbum growthAlbum) {
        return GrowthAlbumDetailResponse.builder()
                .id(growthAlbum.getId())
                .photoUrl(growthAlbum.getPhotoUrl())
                .memo(growthAlbum.getMemo())
                .createdAt(growthAlbum.getCreatedAt())
                .build();
    }
}
