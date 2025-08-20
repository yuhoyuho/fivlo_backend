package com.fivlo.fivlo_backend.domain.growth.controller;

import com.fivlo.fivlo_backend.common.Routes;
import com.fivlo.fivlo_backend.domain.growth.dto.*;
import com.fivlo.fivlo_backend.domain.growth.service.GrowthAlbumService;
import com.fivlo.fivlo_backend.domain.growth.service.S3FileUploadService;
import com.fivlo.fivlo_backend.domain.user.entity.User;
import com.fivlo.fivlo_backend.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 성장앨범 컨트롤러
 * 성장앨범 관리 관련 API 엔드포인트 처리
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class GrowthAlbumController {

    private final GrowthAlbumService growthAlbumService;
    private final S3FileUploadService s3FileUploadService;

    /**
     * PreSigned URL 생성 API
     * 파일 업로드를 위한 S3 PreSigned URL 생성
     */
    @PostMapping(Routes.GROWTH_ALBUM_UPLOAD)
    public ResponseEntity<PresignedUrlResponse> generatePresignedUrl(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PresignedUrlRequest request) {

        log.info("PreSigned URL 생성 요청 - userId: {}, fileName: {}", 
                userDetails.getUser().getId(), request.getFileName());

        PresignedUrlResponse response = s3FileUploadService.generatePresignedUrl(request);

        log.info("PreSigned URL 생성 응답 완료 - userId: {}, s3ObjectKey: {}", 
                userDetails.getUser().getId(), response.getS3ObjectKey());

        return ResponseEntity.ok(response);
    }

    /**
     * API 18: Task 완료 후 성장앨범 항목 생성
     * 성장앨범 연동 Task 완료 시 사진과 메모를 저장합니다.
     */
    @PostMapping(Routes.TASKS_BASE + "/{taskId}/growth-album")
    public ResponseEntity<GrowthAlbumCreateResponse> createGrowthAlbum(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long taskId,
            @Valid @RequestBody GrowthAlbumCreateRequest request) {

        log.info("성장앨범 생성 요청 - userId: {}, taskId: {}, photoUrl: {}", 
                userDetails.getUser().getId(), taskId, request.getPhotoUrl());

        User user = userDetails.getUser();
        GrowthAlbumCreateResponse response = growthAlbumService.createGrowthAlbum(user, taskId, request);

        log.info("성장앨범 생성 응답 완료 - userId: {}, taskId: {}, albumId: {}", 
                userDetails.getUser().getId(), taskId, response.getGrowthAlbumId());

        return ResponseEntity.ok(response);
    }

    /**
     * API 19: 성장앨범 캘린더 보기 조회
     * 특정 월의 성장앨범을 캘린더 형식으로 조회합니다.
     */
    @GetMapping(Routes.GROWTH_ALBUM_CALENDAR)
    public ResponseEntity<GrowthAlbumCalendarResponse> getGrowthAlbumCalendar(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam int year,
            @RequestParam int month) {

        log.info("성장앨범 캘린더 조회 요청 - userId: {}, year: {}, month: {}", 
                userDetails.getUser().getId(), year, month);

        User user = userDetails.getUser();
        GrowthAlbumCalendarResponse response = growthAlbumService.getGrowthAlbumCalendar(user, year, month);

        log.info("성장앨범 캘린더 조회 응답 완료 - userId: {}, year: {}, month: {}, 항목 수: {}", 
                userDetails.getUser().getId(), year, month, response.getAlbumEntries().size());

        return ResponseEntity.ok(response);
    }

    /**
     * API 20: 성장앨범 카테고리별 보기 조회
     * 성장앨범 항목들을 카테고리별로 그룹화하여 조회합니다.
     */
    @GetMapping(Routes.GROWTH_ALBUM_CATEGORIES)
    public ResponseEntity<GrowthAlbumCategoryResponse> getGrowthAlbumByCategories(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("성장앨범 카테고리별 조회 요청 - userId: {}", userDetails.getUser().getId());

        User user = userDetails.getUser();
        GrowthAlbumCategoryResponse response = growthAlbumService.getGrowthAlbumByCategories(user);

        log.info("성장앨범 카테고리별 조회 응답 완료 - userId: {}, 카테고리 수: {}", 
                userDetails.getUser().getId(), response.getCategories().size());

        return ResponseEntity.ok(response);
    }

    /**
     * API 21: 특정 성장앨범 항목 상세 조회
     * 사용자가 사진을 클릭했을 때, 원본 사진과 메모 등 상세 정보를 조회합니다.
     */
    @GetMapping(Routes.GROWTH_ALBUM_BASE + "/{albumId}")
    public ResponseEntity<GrowthAlbumDetailResponse> getGrowthAlbumDetail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long albumId) {

        log.info("성장앨범 상세 조회 요청 - userId: {}, albumId: {}", 
                userDetails.getUser().getId(), albumId);

        User user = userDetails.getUser();
        GrowthAlbumDetailResponse response = growthAlbumService.getGrowthAlbumDetail(user, albumId);

        log.info("성장앨범 상세 조회 응답 완료 - userId: {}, albumId: {}", 
                userDetails.getUser().getId(), albumId);

        return ResponseEntity.ok(response);
    }
}

