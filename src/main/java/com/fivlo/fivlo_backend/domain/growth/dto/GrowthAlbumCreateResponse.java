package com.fivlo.fivlo_backend.domain.growth.dto;

import com.fivlo.fivlo_backend.domain.growth.entity.GrowthAlbum;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 성장앨범 생성 응답 DTO
 * API 18: Task 완료 후 성장앨범 항목 생성 응답용
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GrowthAlbumCreateResponse {
    
    private Long growthAlbumId;
    private String photoUrl;
    private String message;
    
    @Builder
    public GrowthAlbumCreateResponse(Long growthAlbumId, String photoUrl, String message) {
        this.growthAlbumId = growthAlbumId;
        this.photoUrl = photoUrl;
        this.message = message;
    }
    
    /**
     * 성장앨범 생성 성공 응답 생성
     */
    public static GrowthAlbumCreateResponse success(GrowthAlbum growthAlbum) {
        return GrowthAlbumCreateResponse.builder()
                .growthAlbumId(growthAlbum.getId())
                .photoUrl(growthAlbum.getPhotoUrl())
                .message("성장앨범에 성공적으로 저장되었습니다.")
                .build();
    }
}
