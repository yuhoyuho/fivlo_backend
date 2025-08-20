package com.fivlo.fivlo_backend.domain.growth.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * 성장앨범 캘린더 보기 응답 DTO
 * API 19: 성장앨범 캘린더 보기 조회용
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GrowthAlbumCalendarResponse {
    
    private List<CalendarEntry> albumEntries;
    
    @Builder
    public GrowthAlbumCalendarResponse(List<CalendarEntry> albumEntries) {
        this.albumEntries = albumEntries;
    }
    
    /**
     * 캘린더 항목 정보
     */
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class CalendarEntry {
        
        private LocalDate date;
        private String thumbnailUrl;
        private Long albumId;
        
        @Builder
        public CalendarEntry(LocalDate date, String thumbnailUrl, Long albumId) {
            this.date = date;
            this.thumbnailUrl = thumbnailUrl;
            this.albumId = albumId;
        }
    }
}
