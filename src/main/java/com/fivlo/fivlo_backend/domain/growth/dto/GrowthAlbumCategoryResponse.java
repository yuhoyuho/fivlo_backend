package com.fivlo.fivlo_backend.domain.growth.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 성장앨범 카테고리별 보기 응답 DTO
 * API 20: 성장앨범 카테고리별 보기 조회용
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GrowthAlbumCategoryResponse {
    
    private List<CategoryGroup> categories;
    
    @Builder
    public GrowthAlbumCategoryResponse(List<CategoryGroup> categories) {
        this.categories = categories;
    }
    
    /**
     * 카테고리별 그룹 정보
     */
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class CategoryGroup {
        
        private Long id;
        private String name;
        private List<AlbumPhoto> albumPhotos;
        
        @Builder
        public CategoryGroup(Long id, String name, List<AlbumPhoto> albumPhotos) {
            this.id = id;
            this.name = name;
            this.albumPhotos = albumPhotos;
        }
    }
    
    /**
     * 앨범 사진 정보
     */
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class AlbumPhoto {
        
        private String thumbnailUrl;
        private Long albumId;
        
        @Builder
        public AlbumPhoto(String thumbnailUrl, Long albumId) {
            this.thumbnailUrl = thumbnailUrl;
            this.albumId = albumId;
        }
    }
}
